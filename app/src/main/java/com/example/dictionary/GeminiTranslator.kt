package com.example.dictionary

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TranslationResult(
    val word: String,
    val translation: String,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val definition: String? = null,
    val contextTranslation: String? = null
)

object GeminiTranslator {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun translateWord(
        word: String,
        contextSentence: String? = null
    ): TranslationResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // Try offline dictionary first if offline or API key absent
        val offlineEntry = OfflineDictionary.lookup(word)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext TranslationResult(
                word = word,
                translation = offlineEntry?.translation ?: "ترجمه در دسترس نیست (بدون کلید AI)",
                phonetic = offlineEntry?.phonetic,
                partOfSpeech = offlineEntry?.partOfSpeech,
                definition = offlineEntry?.definition,
                contextTranslation = contextSentence?.let { "متن نمونه: $it" }
            )
        }

        try {
            val prompt = """
                You are a professional lexicographer and translator.
                Analyze the English word "$word".
                ${if (!contextSentence.isNull_or_blank()) "Context sentence: \"$contextSentence\"" else ""}
                
                Respond ONLY with a raw JSON object (no markdown, no backticks) with this structure:
                {
                  "word": "$word",
                  "translation": "Persian translation of the word",
                  "phonetic": "IPA phonetic pronunciation like /.../",
                  "partOfSpeech": "Noun/Verb/Adjective/Adverb...",
                  "definition": "Simple English definition",
                  "contextTranslation": "Persian translation of the context sentence"
                }
            """.trimIndent()

            val jsonPayload = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNull_or_empty()) {
                val jsonResp = JSONObject(responseBody)
                val candidates = jsonResp.optJSONArray("candidates")
                val textResponse = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (!textResponse.isNull_or_empty()) {
                    val cleanJsonStr = textResponse!!.replace(Regex("""```json\s*|\s*```"""), "").trim()
                    val resultObj = JSONObject(cleanJsonStr)

                    return@withContext TranslationResult(
                        word = word,
                        translation = resultObj.optString("translation", offlineEntry?.translation ?: "ترجمه‌نشده"),
                        phonetic = resultObj.optString("phonetic", offlineEntry?.phonetic ?: ""),
                        partOfSpeech = resultObj.optString("partOfSpeech", offlineEntry?.partOfSpeech ?: ""),
                        definition = resultObj.optString("definition", offlineEntry?.definition ?: ""),
                        contextTranslation = resultObj.optString("contextTranslation", "")
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback to offline
        return@withContext TranslationResult(
            word = word,
            translation = offlineEntry?.translation ?: "ترجمه آفلاین موجود نیست",
            phonetic = offlineEntry?.phonetic,
            partOfSpeech = offlineEntry?.partOfSpeech,
            definition = offlineEntry?.definition,
            contextTranslation = contextSentence
        )
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()
    private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
}
