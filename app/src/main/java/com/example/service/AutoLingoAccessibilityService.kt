package com.example.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.PreferencesManager
import com.example.data.TranslationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Accessibility service that detects incoming messages in messaging apps
 * and triggers the floating translation bubble in Bangla.
 */
class AutoLingoAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var translationRepository: TranslationRepository
    private lateinit var bubbleManager: FloatingBubbleManager

    private var lastProcessedText: String = ""
    private var lastProcessedTime: Long = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        preferencesManager = PreferencesManager(this)
        translationRepository = TranslationRepository(this, preferencesManager)
        bubbleManager = FloatingBubbleManager(this)
        Log.d(TAG, "AutoLingoAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!preferencesManager.showBubble.value) return

        val packageName = event.packageName?.toString().orEmpty()
        val isChatApp = targetChatPackages.any { packageName.startsWith(it) }
        if (!isChatApp) return

        // Extract latest text from event
        val extractedText = when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                event.text.joinToString(" ").trim()
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                extractLatestIncomingText(event.source)
            }
            else -> null
        }

        if (extractedText.isNullOrBlank() || extractedText.length < 3) return
        val now = System.currentTimeMillis()

        // Debounce same message within 4 seconds
        if (extractedText == lastProcessedText && (now - lastProcessedTime) < 4000) {
            return
        }

        lastProcessedText = extractedText
        lastProcessedTime = now

        // Translate incoming text to Bangla
        serviceScope.launch {
            try {
                val result = translationRepository.translate(
                    text = extractedText,
                    fromLang = "auto",
                    toLang = "bn",
                    isIncoming = true
                )
                if (result.isSuccess) {
                    val banglaTranslation = result.getOrNull().orEmpty()
                    if (banglaTranslation.isNotBlank() && banglaTranslation != extractedText) {
                        bubbleManager.showBubble(extractedText, banglaTranslation)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to translate incoming message", e)
            }
        }
    }

    private fun extractLatestIncomingText(rootNode: AccessibilityNodeInfo?): String? {
        if (rootNode == null) return null
        val texts = mutableListOf<String>()
        collectTextNodes(rootNode, texts)
        // Look for the last non-empty chat bubble message text
        return texts.lastOrNull { text ->
            text.length >= 3 &&
            !isCommonUiLabel(text)
        }
    }

    private fun collectTextNodes(node: AccessibilityNodeInfo?, list: MutableList<String>) {
        if (node == null) return
        val text = node.text?.toString()?.trim()
        if (!text.isNullOrEmpty()) {
            list.add(text)
        }
        for (i in 0 until node.childCount) {
            collectTextNodes(node.getChild(i), list)
        }
    }

    private fun isCommonUiLabel(text: String): Boolean {
        val lower = text.lowercase()
        return lower in listOf(
            "online", "typing...", "last seen", "message", "send", "chat", "search",
            "calls", "status", "settings", "yesterday", "today", "delivered", "read"
        )
    }

    override fun onInterrupt() {
        Log.d(TAG, "AutoLingoAccessibilityService interrupted")
    }

    override fun onDestroy() {
        bubbleManager.dismissBubble()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AutoLingoA11y"
        private val targetChatPackages = listOf(
            "com.whatsapp",
            "org.telegram.messenger",
            "com.facebook.orca",
            "com.instagram.android",
            "com.google.android.apps.messaging"
        )
    }
}
