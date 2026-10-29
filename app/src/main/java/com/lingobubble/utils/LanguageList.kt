package com.lingobubble.utils

data class LanguageItem(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String
) {
    val displayName: String
        get() = "$flag $name"
}

object LanguageList {
    val languages: List<LanguageItem> = listOf(
        // Major / Most popular
        LanguageItem("en", "English", "English", "🇺🇸"),
        LanguageItem("bn", "Bangla", "বাংলা", "🇧🇩"),
        LanguageItem("hi", "Hindi", "हिन्दी", "🇮🇳"),
        LanguageItem("es", "Spanish", "Español", "🇪🇸"),
        LanguageItem("ar", "Arabic", "العربية", "🇸🇦"),
        LanguageItem("pt", "Portuguese", "Português", "🇧🇷"),
        LanguageItem("fr", "French", "Français", "🇫🇷"),
        LanguageItem("de", "German", "Deutsch", "🇩🇪"),
        LanguageItem("tr", "Turkish", "Türkçe", "🇹🇷"),
        LanguageItem("ur", "Urdu", "اردو", "🇵🇰"),
        LanguageItem("ru", "Russian", "Русский", "🇷🇺"),
        LanguageItem("zh", "Chinese", "中文", "🇨🇳"),
        LanguageItem("ja", "Japanese", "日本語", "🇯🇵"),
        LanguageItem("ko", "Korean", "한국어", "🇰🇷"),
        LanguageItem("it", "Italian", "Italiano", "🇮🇹"),
        LanguageItem("ms", "Malay", "Bahasa Melayu", "🇲🇾"),
        LanguageItem("id", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
        LanguageItem("th", "Thai", "ไทย", "🇹🇭"),
        LanguageItem("vi", "Vietnamese", "Tiếng Việt", "🇻🇳"),
        LanguageItem("nl", "Dutch", "Nederlands", "🇳🇱"),
        LanguageItem("pl", "Polish", "Polski", "🇵🇱"),
        LanguageItem("sv", "Swedish", "Svenska", "🇸🇪"),
        LanguageItem("fa", "Persian", "فارسی", "🇮🇷"),
        LanguageItem("he", "Hebrew", "עברית", "🇮🇱"),
        LanguageItem("el", "Greek", "Ελληνικά", "🇬🇷"),
        LanguageItem("cs", "Czech", "Čeština", "🇨🇿"),
        LanguageItem("ro", "Romanian", "Română", "🇷🇴"),
        LanguageItem("hu", "Hungarian", "Magyar", "🇭🇺"),
        LanguageItem("da", "Danish", "Dansk", "🇩🇰"),
        LanguageItem("fi", "Finnish", "Suomi", "🇫🇮"),
        LanguageItem("no", "Norwegian", "Norsk", "🇳🇴"),
        LanguageItem("sk", "Slovak", "Slovenčina", "🇸🇰"),
        LanguageItem("uk", "Ukrainian", "Українська", "🇺🇦"),
        LanguageItem("bg", "Bulgarian", "Български", "🇧🇬"),
        LanguageItem("hr", "Croatian", "Hrvatski", "🇭🇷"),
        LanguageItem("lt", "Lithuanian", "Lietuvių", "🇱🇹"),
        LanguageItem("sl", "Slovenian", "Slovenščina", "🇸🇮"),
        LanguageItem("et", "Estonian", "Eesti", "🇪🇪"),
        LanguageItem("lv", "Latvian", "Latviešu", "🇱🇻"),
        LanguageItem("sw", "Swahili", "Kiswahili", "🇰🇪"),
        LanguageItem("ta", "Tamil", "தமிழ்", "🇮🇳"),
        LanguageItem("te", "Telugu", "తెలుగు", "🇮🇳"),
        LanguageItem("mr", "Marathi", "मराठी", "🇮🇳"),
        LanguageItem("gu", "Gujarati", "ગુજરાતી", "🇮🇳"),
        LanguageItem("kn", "Kannada", "ಕನ್ನಡ", "🇮🇳"),
        LanguageItem("ml", "Malayalam", "മലയാളം", "🇮🇳"),
        LanguageItem("pa", "Punjabi", "ਪੰਜਾਬੀ", "🇮🇳"),
        LanguageItem("ne", "Nepali", "नेपाली", "🇳🇵"),
        LanguageItem("si", "Sinhala", "සිංහල", "🇱🇰"),
        LanguageItem("my", "Burmese", "မြန်မာ", "🇲🇲"),
        LanguageItem("km", "Khmer", "ខ្មែរ", "🇰🇭"),
        LanguageItem("lo", "Lao", "ລາວ", "🇱🇦"),
        LanguageItem("am", "Amharic", "አማርኛ", "🇪🇹"),
        LanguageItem("so", "Somali", "Soomaali", "🇸🇴"),
        LanguageItem("ha", "Hausa", "Hausa", "🇳🇬"),
        LanguageItem("yo", "Yoruba", "Èdè Yorùbá", "🇳🇬"),
        LanguageItem("ig", "Igbo", "Asụsụ Igbo", "🇳🇬"),
        LanguageItem("zu", "Zulu", "isiZulu", "🇿🇦"),
        LanguageItem("af", "Afrikaans", "Afrikaans", "🇿🇦"),
        LanguageItem("sq", "Albanian", "Shqip", "🇦🇱"),
        LanguageItem("az", "Azerbaijani", "Azərbaycan", "🇦🇿"),
        LanguageItem("eu", "Basque", "Euskara", "🇪🇸"),
        LanguageItem("be", "Belarusian", "Беларуская", "🇧🇾"),
        LanguageItem("bs", "Bosnian", "Bosanski", "🇧🇦"),
        LanguageItem("ca", "Catalan", "Català", "🇦🇩"),
        LanguageItem("ceb", "Cebuano", "Sinugboanon", "🇵🇭"),
        LanguageItem("co", "Corsican", "Corsu", "🇫🇷"),
        LanguageItem("cy", "Welsh", "Cymraeg", "🇬🇧"),
        LanguageItem("eo", "Esperanto", "Esperanto", "🌐"),
        LanguageItem("tl", "Filipino", "Tagalog", "🇵🇭"),
        LanguageItem("fy", "Frisian", "Frysk", "🇳🇱"),
        LanguageItem("gl", "Galician", "Galego", "🇪🇸"),
        LanguageItem("ka", "Georgian", "ქართული", "🇬🇪"),
        LanguageItem("ht", "Haitian Creole", "Kreyòl ayisyen", "🇭🇹"),
        LanguageItem("haw", "Hawaiian", "ʻŌlelo Hawaiʻi", "🇺🇸"),
        LanguageItem("is", "Icelandic", "Íslenska", "🇮🇸"),
        LanguageItem("ga", "Irish", "Gaeilge", "🇮🇪"),
        LanguageItem("jw", "Javanese", "Basa Jawa", "🇮🇩"),
        LanguageItem("kk", "Kazakh", "Қазақ тілі", "🇰🇿"),
        LanguageItem("ku", "Kurdish", "Kurdî", "🇮🇶"),
        LanguageItem("ky", "Kyrgyz", "Кыргызча", "🇰🇬"),
        LanguageItem("la", "Latin", "Latina", "🇻🇦"),
        LanguageItem("lb", "Luxembourgish", "Lëtzebuergesch", "🇱🇺"),
        LanguageItem("mg", "Malagasy", "Malagasy", "🇲🇬"),
        LanguageItem("mt", "Maltese", "Malti", "🇲🇹"),
        LanguageItem("mi", "Maori", "Te Reo Māori", "🇳🇿"),
        LanguageItem("mn", "Mongolian", "Монгол", "🇲🇳"),
        LanguageItem("ny", "Nyanja", "Chichewa", "🇲🇼"),
        LanguageItem("ps", "Pashto", "پښتو", "🇦🇫"),
        LanguageItem("sm", "Samoan", "Gagana Sāmoa", "🇼🇸"),
        LanguageItem("gd", "Scots Gaelic", "Gàidhlig", "🏴󠁧󠁢󠁳󠁣󠁴󠁿"),
        LanguageItem("st", "Sesotho", "Sesotho", "🇱🇸"),
        LanguageItem("sn", "Shona", "chiShona", "🇿🇼"),
        LanguageItem("sd", "Sindhi", "سنڌي", "🇵🇰"),
        LanguageItem("su", "Sundanese", "Basa Sunda", "🇮🇩"),
        LanguageItem("tg", "Tajik", "Тоҷикӣ", "🇹🇯"),
        LanguageItem("tt", "Tatar", "Татар теле", "🇷🇺"),
        LanguageItem("tk", "Turkmen", "Türkmençe", "🇹🇲"),
        LanguageItem("ug", "Uyghur", "ئۇيغۇرچە", "🇨🇳"),
        LanguageItem("uz", "Uzbek", "Oʻzbekcha", "🇺🇿"),
        LanguageItem("xh", "Xhosa", "isiXhosa", "🇿🇦"),
        LanguageItem("yi", "Yiddish", "ייִדיש", "🇮🇱")
    )

    val quickFavorites = listOf(
        getByCode("en"),
        getByCode("bn"),
        getByCode("hi"),
        getByCode("es"),
        getByCode("ar"),
        getByCode("fr"),
        getByCode("de"),
        getByCode("pt"),
        getByCode("ja"),
        getByCode("zh")
    )

    fun getByCode(code: String): LanguageItem {
        return languages.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: LanguageItem(code, code.uppercase(), code.uppercase(), "🌐")
    }

    fun search(query: String): List<LanguageItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return languages
        return languages.filter {
            it.name.lowercase().contains(q) ||
            it.nativeName.lowercase().contains(q) ||
            it.code.lowercase().contains(q)
        }
    }
}
