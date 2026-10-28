package com.example.keyboard

enum class KeyboardLayoutMode {
    BANGLA,
    BANGLA_SHIFT,
    ENGLISH_LOWER,
    ENGLISH_UPPER,
    NUMBERS_SYMBOLS
}

sealed interface TranslationState {
    data object Idle : TranslationState
    data object Translating : TranslationState
    data class Success(val message: String) : TranslationState
    data class Error(val errorMessage: String) : TranslationState
}
