package com.example.model

/**
 * Representation of supported languages in AutoLingo with localization metadata.
 */
data class SupportedLanguage(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String
) {
    val displayName: String
        get() = "$flag $name ($nativeName)"
}

object LanguageCatalog {
    val languages = listOf(
        SupportedLanguage("bn", "Bangla", "বাংলা", "🇧🇩"),
        SupportedLanguage("en", "English", "English", "🇺🇸"),
        SupportedLanguage("es", "Spanish", "Español", "🇪🇸"),
        SupportedLanguage("ar", "Arabic", "العربية", "🇸🇦"),
        SupportedLanguage("pt", "Portuguese", "Português", "🇧🇷"),
        SupportedLanguage("hi", "Hindi", "हिन्दी", "🇮🇳"),
        SupportedLanguage("fr", "French", "Français", "🇫🇷"),
        SupportedLanguage("de", "German", "Deutsch", "🇩🇪"),
        SupportedLanguage("ja", "Japanese", "日本語", "🇯🇵"),
        SupportedLanguage("ru", "Russian", "Русский", "🇷🇺"),
        SupportedLanguage("it", "Italian", "Italiano", "🇮🇹"),
        SupportedLanguage("ur", "Urdu", "اردو", "🇵🇰"),
        SupportedLanguage("tr", "Turkish", "Türkçe", "🇹🇷"),
        SupportedLanguage("zh", "Chinese", "中文", "🇨🇳")
    )

    fun findByCode(code: String): SupportedLanguage {
        return languages.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: SupportedLanguage(code, code.uppercase(), code.uppercase(), "🌐")
    }
}
