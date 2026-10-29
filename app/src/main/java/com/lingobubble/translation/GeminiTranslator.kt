package com.lingobubble.translation

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.Log
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.entity.TranslationHistoryItem
import com.lingobubble.utils.LanguageList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val detectedSourceLang: String,
    val targetLangCode: String,
    val targetLangName: String,
    val engine: String
)

class GeminiTranslator(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .build()

    private val database = AppDatabase.getInstance(context)

    /**
     * Translates captured screen text to the target language.
     * Uses Gemini API if API key is provided, or seamlessly falls back to MyMemory free API.
     */
    suspend fun translate(
        capturedText: String,
        targetLangCode: String,
        apiKey: String? = null,
        preferredEngine: String = "gemini"
    ): Result<TranslationResult> = withContext(Dispatchers.IO) {
        val trimmed = capturedText.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Empty text"))
        }

        val targetLangItem = LanguageList.getByCode(targetLangCode)
        val targetLangName = targetLangItem.name

        // Check apiKey from parameter or BuildConfig
        val geminiKey = when {
            !apiKey.isNullOrBlank() -> apiKey.trim()
            BuildConfig.BUILD_TYPE == "debug" -> "" // fallback if not injected
            else -> ""
        }

        try {
            val result: TranslationResult = if (preferredEngine == "gemini" && geminiKey.isNotBlank()) {
                translateWithGemini(trimmed, targetLangCode, targetLangName, geminiKey)
            } else {
                // Free instant translation
                translateWithMyMemory(trimmed, targetLangCode, targetLangName)
            }

            // Save to persistent Room Database history
            try {
                database.historyDao().insert(
                    TranslationHistoryItem(
                        sourceText = result.originalText,
                        translatedText = result.translatedText,
                        fromLang = result.detectedSourceLang,
                        toLang = result.targetLangCode,
                        timestamp = System.currentTimeMillis(),
                        engine = result.engine,
                        isIncoming = true
                    )
                )
                database.historyDao().trimToLimit(50)
            } catch (e: Exception) {
                Log.w(TAG, "Failed saving history: ${e.message}")
            }

            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Translation error with engine $preferredEngine: ${e.message}", e)
            // If primary failed, try MyMemory fallback before returning error
            if (preferredEngine == "gemini") {
                try {
                    val fallback = translateWithMyMemory(trimmed, targetLangCode, targetLangName)
                    return@withContext Result.success(fallback)
                } catch (fallbackError: Exception) {
                    return@withContext Result.failure(fallbackError)
                }
            }
            Result.failure(e)
        }
    }

    /**
     * Calls Google Gemini API for high-quality natural language translation.
     */
    fun translateWithGemini(
        capturedText: String,
        targetLangCode: String,
        targetLangName: String,
        apiKey: String
    ): TranslationResult {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

        val prompt = "Detect language of this text and translate to $targetLangName ($targetLangCode). " +
                "Text: \"$capturedText\". " +
                "Only return translated text, no explanation."

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)

            // Generation config for deterministic concise translation
            val genConfig = JSONObject().apply {
                put("temperature", 0.2)
                put("maxOutputTokens", 1024)
            }
            put("generationConfig", genConfig)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw Exception("Gemini API error ${response.code}: $errorBody")
            }

            val body = response.body?.string().orEmpty()
            val root = JSONObject(body)
            val candidates = root.getJSONArray("candidates")
            if (candidates.length() == 0) throw Exception("No translation candidate returned")

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val translatedText = parts.getJSONObject(0).getString("text").trim()

            return TranslationResult(
                originalText = capturedText,
                translatedText = translatedText,
                detectedSourceLang = "Auto",
                targetLangCode = targetLangCode,
                targetLangName = targetLangName,
                engine = "Gemini 1.5 Flash"
            )
        }
    }

    /**
     * Free translation fallback with MyMemory API.
     */
    private fun translateWithMyMemory(
        capturedText: String,
        targetLangCode: String,
        targetLangName: String
    ): TranslationResult {
        val encodedText = URLEncoder.encode(capturedText, StandardCharsets.UTF_8.toString())
        val url = "https://api.mymemory.translated.net/get?q=$encodedText&langpair=autodetect|$targetLangCode"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "LingoBubble-Android-App")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("MyMemory error ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val responseData = json.optJSONObject("responseData")
            val translatedRaw = responseData?.optString("translatedText")
                ?: json.optJSONArray("matches")?.optJSONObject(0)?.optString("translation")
                ?: throw Exception("No translation found")

            val clean = unescapeHtml(translatedRaw)
            return TranslationResult(
                originalText = capturedText,
                translatedText = clean,
                detectedSourceLang = "Auto",
                targetLangCode = targetLangCode,
                targetLangName = targetLangName,
                engine = "MyMemory (Free)"
            )
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
        private const val TAG = "GeminiTranslator"
    }
}
