package com.example.keyboard

object KeyboardLayouts {

    val englishRow1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val englishRow2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val englishRow3 = listOf("z", "x", "c", "v", "b", "n", "m")

    // Dedicated Bangla Kar / Matra row
    val banglaKarRow = listOf("া", "ি", "ী", "ু", "ূ", "ৃ", "ে", "ৈ", "ো", "ৌ", "্", "ং", "ঃ", "ঁ", "।")

    // Normal Bangla Consonant & Vowel layout
    val banglaNormalRow1 = listOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ")
    val banglaNormalRow2 = listOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন")
    val banglaNormalRow3 = listOf("প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "স", "হ")

    // Shifted Bangla (Swaroborno & Special letters)
    val banglaShiftRow1 = listOf("অ", "আ", "ই", "ঈ", "উ", "ঊ", "ঋ", "এ", "ঐ", "ও")
    val banglaShiftRow2 = listOf("ঔ", "ড়", "ঢ়", "য়", "ৎ", "ক্ষ", "জ্ঞ", "্র", "্য", "্ব")
    val banglaShiftRow3 = listOf("ষ", "হ", "ং", "ঃ", "ঁ", "্", "ৗ", "।", "!", "?")

    // Numbers & Symbols
    val numbersRow1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val symbolsRow2 = listOf("@", "#", "$", "%", "&", "*", "-", "+", "(", ")")
    val symbolsRow3 = listOf("!", "\"", "'", ":", ";", "/", "?", ",", ".", "~")

    val banglaDigits = listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০")

    val quickBanglaPhrases = listOf(
        "কেমন আছেন?",
        "আমি ভালো আছি",
        "ধন্যবাদ",
        "কোথায় আছেন?",
        "কি খবর?",
        "দেখা হবে",
        "শুভ সকাল",
        "শুভ রাত্রি",
        "পরে কথা বলছি"
    )
}
