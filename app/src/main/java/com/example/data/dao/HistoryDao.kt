package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.TranslationHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistory(): Flow<List<TranslationHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TranslationHistoryItem): Long

    @Query("DELETE FROM translation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM translation_history")
    suspend fun clearAll()

    @Query("DELETE FROM translation_history WHERE id NOT IN (SELECT id FROM translation_history ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun trimToLimit(limit: Int = 50)
}
