package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing persistent history of sent and received translations.
 */
@Entity(tableName = "translation_history")
data class TranslationHistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceText: String,
    val translatedText: String,
    val fromLang: String,
    val toLang: String,
    val timestamp: Long = System.currentTimeMillis(),
    val engine: String = "MyMemory",
    val isIncoming: Boolean = false
)
