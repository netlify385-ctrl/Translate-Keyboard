package com.example.data

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.Log
import com.example.data.entity.TranslationHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class TranslationRepository(
    private val context: Context,
    private val preferencesManager: PreferencesManager = PreferencesManager(context),
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun translate(
        text: String,
        fromLang: String? = null,
        toLang: String? = null,
        isIncoming: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.success("")
        }

        val sourceLang = fromLang ?: preferencesManager.fromLang.value
        val targetLang = toLang ?: preferencesManager.toLang.value
        val engine = preferencesManager.engine.value

        try {
            val translated = when (engine) {
                "deepl" -> {
                    val key = preferencesManager.deeplKey.value
                    if (key.isNotBlank()) {
                        translateDeepL(trimmed, sourceLang, targetLang, key)
                    } else {
                        // Fallback to MyMemory if key missing
                        translateMyMemory(trimmed, sourceLang, targetLang)
                    }
                }
                "google" -> {
                    val key = preferencesManager.googleKey.value
                    if (key.isNotBlank()) {
                        translateGoogle(trimmed, sourceLang, targetLang, key)
                    } else {
                        // Fallback to MyMemory if key missing
                        translateMyMemory(trimmed, sourceLang, targetLang)
                    }
                }
                else -> translateMyMemory(trimmed, sourceLang, targetLang)
            }

            // Save to Room DB history
            val historyItem = TranslationHistoryItem(
                sourceText = trimmed,
                translatedText = translated,
                fromLang = sourceLang,
                toLang = targetLang,
                timestamp = System.currentTimeMillis(),
                engine = if (engine == "deepl" && preferencesManager.deeplKey.value.isNotBlank()) "DeepL"
                         else if (engine == "google" && preferencesManager.googleKey.value.isNotBlank()) "Google"
                         else "MyMemory",
                isIncoming = isIncoming
            )
            database.historyDao().insert(historyItem)
            database.historyDao().trimToLimit(50)

            Result.success(translated)
        } catch (e: Exception) {
            Log.e(TAG, "Translation error", e)
            Result.failure(e)
        }
    }

    /**
     * Translates using MyMemory Free API:
     * https://api.mymemory.translated.net/get?q=...&langpair=bn|en
     */
    private fun translateMyMemory(text: String, from: String, to: String): String {
        val encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        val url = "https://api.mymemory.translated.net/get?q=$encodedText&langpair=$from|$to"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "AutoLingo-Android-Translator")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("MyMemory HTTP ${response.code}: ${response.message}")
            }
            val bodyString = response.body?.string().orEmpty()
            val json = JSONObject(bodyString)
            val responseStatus = json.optInt("responseStatus", 200)

            val rawResult = if (responseStatus == 200 || responseStatus == 201) {
                json.getJSONObject("responseData").getString("translatedText")
            } else {
                val matches = json.optJSONArray("matches")
                if (matches != null && matches.length() > 0) {
                    matches.getJSONObject(0).getString("translation")
                } else {
                    json.optString("responseDetails", "Translation failed")
                }
            }

            return unescapeHtml(rawResult)
        }
    }

    /**
     * Translates using DeepL API.
     */
    private fun translateDeepL(text: String, from: String, to: String, apiKey: String): String {
        val baseUrl = if (apiKey.endsWith(":fx")) {
            "https://api-free.deepl.com/v2/translate"
        } else {
            "https://api.deepl.com/v2/translate"
        }

        val jsonBody = JSONObject().apply {
            put("text", org.json.JSONArray().put(text))
            put("target_lang", to.uppercase())
            if (from.isNotEmpty() && !from.equals("auto", ignoreCase = true)) {
                put("source_lang", from.uppercase())
            }
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(baseUrl)
            .header("Authorization", "DeepL-Auth-Key $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("DeepL error ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val translations = json.getJSONArray("translations")
            return translations.getJSONObject(0).getString("text")
        }
    }

    /**
     * Translates using Google Cloud Translation API v2.
     */
    private fun translateGoogle(text: String, from: String, to: String, apiKey: String): String {
        val url = "https://translation.googleapis.com/language/translate/v2?key=$apiKey"
        val jsonBody = JSONObject().apply {
            put("q", org.json.JSONArray().put(text))
            put("target", to)
            if (from.isNotEmpty() && !from.equals("auto", ignoreCase = true)) {
                put("source", from)
            }
            put("format", "text")
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Google Translate error ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val translations = json.getJSONObject("data").getJSONArray("translations")
            return unescapeHtml(translations.getJSONObject(0).getString("translatedText"))
        }
    }

    private fun unescapeHtml(html: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html).toString()
        }
    }

    companion object {
        private const val TAG = "TranslationRepo"
    }
}
