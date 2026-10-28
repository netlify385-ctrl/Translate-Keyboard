package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.TranslationHistoryItem
import com.example.model.LanguageCatalog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AutoLingo", appName)
    }

    @Test
    fun testLanguageCatalogLookup() {
        val bn = LanguageCatalog.findByCode("bn")
        assertEquals("Bangla", bn.name)
        assertEquals("🇧🇩", bn.flag)

        val en = LanguageCatalog.findByCode("en")
        assertEquals("English", en.name)
        assertEquals("🇺🇸", en.flag)

        val es = LanguageCatalog.findByCode("es")
        assertEquals("Spanish", es.name)
    }

    @Test
    fun testRoomHistoryInsertAndRetrieve() = runBlocking {
        val item = TranslationHistoryItem(
            sourceText = "আপনি কেমন আছেন?",
            translatedText = "How are you?",
            fromLang = "bn",
            toLang = "en",
            engine = "MyMemory",
            isIncoming = false
        )
        val id = db.historyDao().insert(item)
        assertTrue(id > 0)

        val history = db.historyDao().getAllHistory().first()
        assertEquals(1, history.size)
        assertEquals("আপনি কেমন আছেন?", history[0].sourceText)
        assertEquals("How are you?", history[0].translatedText)
        assertEquals("bn", history[0].fromLang)
        assertEquals("en", history[0].toLang)
    }
}
