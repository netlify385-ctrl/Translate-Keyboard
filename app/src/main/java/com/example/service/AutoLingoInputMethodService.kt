package com.example.service

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.PreferencesManager
import com.example.data.TranslationRepository
import com.example.keyboard.AutoLingoKeyboardView
import com.example.keyboard.TranslationState
import com.example.ui.theme.AutoLingoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Custom InputMethodService providing the AutoLingo bilingual translator keyboard.
 */
class AutoLingoInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var translationRepository: TranslationRepository

    private var translationState by mutableStateOf<TranslationState>(TranslationState.Idle)

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        preferencesManager = PreferencesManager(this)
        translationRepository = TranslationRepository(this, preferencesManager)
    }

    override fun onCreateInputView(): View {
        window?.window?.decorView?.let { decor ->
            decor.setViewTreeLifecycleOwner(this)
            decor.setViewTreeViewModelStoreOwner(this)
            decor.setViewTreeSavedStateRegistryOwner(this)
        }

        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@AutoLingoInputMethodService))
            setContent {
                AutoLingoTheme {
                    val fromLang by preferencesManager.fromLang.collectAsState()
                    val toLang by preferencesManager.toLang.collectAsState()
                    val isAutoOn by preferencesManager.autoTranslate.collectAsState()
                    val isVibrateOn by preferencesManager.vibrate.collectAsState()

                    AutoLingoKeyboardView(
                        fromLangCode = fromLang,
                        toLangCode = toLang,
                        isAutoTranslateOn = isAutoOn,
                        translationState = translationState,
                        vibrateEnabled = isVibrateOn,
                        onKeyPress = { handleKeyPress(it) },
                        onBackspace = { handleBackspace() },
                        onSpace = { handleSpace() },
                        onEnter = { handleEnter() },
                        onTranslateAndSend = { handleTranslateAndSend() },
                        onToggleAutoTranslate = { preferencesManager.setAutoTranslate(!isAutoOn) },
                        onSwapLanguages = { preferencesManager.swapLanguages() },
                        onSelectFromLang = { preferencesManager.setFromLang(it) },
                        onSelectToLang = { preferencesManager.setToLang(it) },
                        onOpenSettings = { openSettingsScreen() }
                    )
                }
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        translationState = TranslationState.Idle
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun handleKeyPress(char: String) {
        currentInputConnection?.commitText(char, 1)

        // If Auto-Translate is active and punctuation was entered, auto-translate preceding sentence
        if (preferencesManager.autoTranslate.value && char in listOf(".", "?", "!", "।")) {
            handleAutoSentenceTranslate()
        }
    }

    private fun handleBackspace() {
        currentInputConnection?.let { ic ->
            val selectedText = ic.getSelectedText(0)
            if (!selectedText.isNullOrEmpty()) {
                ic.commitText("", 1)
            } else {
                ic.deleteSurroundingText(1, 0)
            }
        }
    }

    private fun handleSpace() {
        currentInputConnection?.commitText(" ", 1)
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        val currentAction = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        when (currentAction) {
            EditorInfo.IME_ACTION_SEND -> ic.performEditorAction(EditorInfo.IME_ACTION_SEND)
            EditorInfo.IME_ACTION_GO -> ic.performEditorAction(EditorInfo.IME_ACTION_GO)
            EditorInfo.IME_ACTION_SEARCH -> ic.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
            EditorInfo.IME_ACTION_DONE -> ic.performEditorAction(EditorInfo.IME_ACTION_DONE)
            else -> sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    /**
     * Outgoing translation: reads typed input text, translates, replaces and commits.
     */
    private fun handleTranslateAndSend() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(2000, 0)?.toString().orEmpty()
        val after = ic.getTextAfterCursor(2000, 0)?.toString().orEmpty()
        val fullText = (before + after).trim()

        if (fullText.isEmpty()) {
            translationState = TranslationState.Error("Please type some text first!")
            serviceScope.launch {
                delay(2000)
                translationState = TranslationState.Idle
            }
            return
        }

        translationState = TranslationState.Translating
        serviceScope.launch {
            val result = translationRepository.translate(
                text = fullText,
                fromLang = preferencesManager.fromLang.value,
                toLang = preferencesManager.toLang.value,
                isIncoming = false
            )

            if (result.isSuccess) {
                val translated = result.getOrNull().orEmpty()
                // Safely delete full typed text and replace with translation
                ic.deleteSurroundingText(before.length, after.length)
                ic.commitText(translated, 1)
                translationState = TranslationState.Success(translated)

                delay(1500)
                translationState = TranslationState.Idle
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Translation failed"
                translationState = TranslationState.Error(errorMsg)
                delay(3000)
                translationState = TranslationState.Idle
            }
        }
    }

    /**
     * Automatic sentence translator: triggers when sentence end punctuation is typed.
     */
    private fun handleAutoSentenceTranslate() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(500, 0)?.toString().orEmpty()
        if (before.length < 3) return

        // Find the sentence ending with punctuation
        val sentence = before.trim()
        if (sentence.isEmpty()) return

        serviceScope.launch {
            val result = translationRepository.translate(
                text = sentence,
                fromLang = preferencesManager.fromLang.value,
                toLang = preferencesManager.toLang.value,
                isIncoming = false
            )
            if (result.isSuccess) {
                val translated = result.getOrNull().orEmpty()
                if (translated.isNotBlank()) {
                    ic.deleteSurroundingText(before.length, 0)
                    ic.commitText("$translated ", 1)
                }
            }
        }
    }

    private fun openSettingsScreen() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }
}
