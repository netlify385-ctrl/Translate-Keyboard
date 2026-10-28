package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("autolingo_prefs", Context.MODE_PRIVATE)

    private val _fromLang = MutableStateFlow(prefs.getString(KEY_FROM_LANG, "bn") ?: "bn")
    val fromLang: StateFlow<String> = _fromLang.asStateFlow()

    private val _toLang = MutableStateFlow(prefs.getString(KEY_TO_LANG, "en") ?: "en")
    val toLang: StateFlow<String> = _toLang.asStateFlow()

    private val _autoTranslate = MutableStateFlow(prefs.getBoolean(KEY_AUTO_TRANSLATE, true))
    val autoTranslate: StateFlow<Boolean> = _autoTranslate.asStateFlow()

    private val _engine = MutableStateFlow(prefs.getString(KEY_ENGINE, "mymemory") ?: "mymemory")
    val engine: StateFlow<String> = _engine.asStateFlow()

    private val _deeplKey = MutableStateFlow(prefs.getString(KEY_DEEPL_API_KEY, "") ?: "")
    val deeplKey: StateFlow<String> = _deeplKey.asStateFlow()

    private val _googleKey = MutableStateFlow(prefs.getString(KEY_GOOGLE_API_KEY, "") ?: "")
    val googleKey: StateFlow<String> = _googleKey.asStateFlow()

    private val _tone = MutableStateFlow(prefs.getString(KEY_TONE, "default") ?: "default")
    val tone: StateFlow<String> = _tone.asStateFlow()

    private val _showBubble = MutableStateFlow(prefs.getBoolean(KEY_SHOW_BUBBLE, true))
    val showBubble: StateFlow<Boolean> = _showBubble.asStateFlow()

    private val _vibrate = MutableStateFlow(prefs.getBoolean(KEY_VIBRATE, true))
    val vibrate: StateFlow<Boolean> = _vibrate.asStateFlow()

    fun setFromLang(lang: String) {
        prefs.edit().putString(KEY_FROM_LANG, lang).apply()
        _fromLang.value = lang
    }

    fun setToLang(lang: String) {
        prefs.edit().putString(KEY_TO_LANG, lang).apply()
        _toLang.value = lang
    }

    fun swapLanguages() {
        val currentFrom = _fromLang.value
        val currentTo = _toLang.value
        setFromLang(currentTo)
        setToLang(currentFrom)
    }

    fun setAutoTranslate(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_TRANSLATE, enabled).apply()
        _autoTranslate.value = enabled
    }

    fun setEngine(engine: String) {
        prefs.edit().putString(KEY_ENGINE, engine).apply()
        _engine.value = engine
    }

    fun setDeeplKey(key: String) {
        prefs.edit().putString(KEY_DEEPL_API_KEY, key.trim()).apply()
        _deeplKey.value = key.trim()
    }

    fun setGoogleKey(key: String) {
        prefs.edit().putString(KEY_GOOGLE_API_KEY, key.trim()).apply()
        _googleKey.value = key.trim()
    }

    fun setTone(tone: String) {
        prefs.edit().putString(KEY_TONE, tone).apply()
        _tone.value = tone
    }

    fun setShowBubble(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_BUBBLE, enabled).apply()
        _showBubble.value = enabled
    }

    fun setVibrate(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATE, enabled).apply()
        _vibrate.value = enabled
    }

    companion object {
        private const val KEY_FROM_LANG = "key_from_lang"
        private const val KEY_TO_LANG = "key_to_lang"
        private const val KEY_AUTO_TRANSLATE = "key_auto_translate"
        private const val KEY_ENGINE = "key_engine"
        private const val KEY_DEEPL_API_KEY = "key_deepl_api_key"
        private const val KEY_GOOGLE_API_KEY = "key_google_api_key"
        private const val KEY_TONE = "key_tone"
        private const val KEY_SHOW_BUBBLE = "key_show_bubble"
        private const val KEY_VIBRATE = "key_vibrate"
    }
}
