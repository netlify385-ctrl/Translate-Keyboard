package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.TranslationHistoryItem
import com.lingobubble.utils.LanguageList
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
        assertEquals("LingoBubble", appName)
    }

    @Test
    fun testLanguageListLookup() {
        val bn = LanguageList.getByCode("bn")
        assertEquals("Bangla", bn.name)
        assertEquals("🇧🇩", bn.flag)

        val en = LanguageList.getByCode("en")
        assertEquals("English", en.name)
        assertEquals("🇺🇸", en.flag)

        val es = LanguageList.getByCode("es")
        assertEquals("Spanish", es.name)
        assertEquals("🇪🇸", es.flag)

        val searchResults = LanguageList.search("spanish")
        assertTrue(searchResults.any { it.code == "es" })
        assertTrue(LanguageList.languages.size >= 100)
    }

    @Test
    fun testRoomHistoryInsertAndRetrieve() = runBlocking {
        val item = TranslationHistoryItem(
            sourceText = "Hello, how are you?",
            translatedText = "হ্যালো, আপনি কেমন আছেন?",
            fromLang = "en",
            toLang = "bn",
            engine = "Gemini 1.5 Flash",
            isIncoming = true
        )
        val id = db.historyDao().insert(item)
        assertTrue(id > 0)

        val history = db.historyDao().getAllHistory().first()
        assertEquals(1, history.size)
        assertEquals("Hello, how are you?", history[0].sourceText)
        assertEquals("হ্যালো, আপনি কেমন আছেন?", history[0].translatedText)
        assertEquals("en", history[0].fromLang)
        assertEquals("bn", history[0].toLang)
    }
}
