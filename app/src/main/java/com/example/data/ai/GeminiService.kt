package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Service class that uses 'GEMINI_API_KEY' to interact with the Google AI / Gemini API.
 */
class GeminiService(
    private val model: String = "gemini-3.5-flash"
) {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val requestAdapter = moshi.adapter(GeminiGenerateRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiGenerateResponse::class.java)

    /**
     * Retrieves the API key safely from BuildConfig.
     */
    fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isBlank() || key == "MY_GEMINI_API_KEY") "" else key
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Checks if a valid GEMINI_API_KEY is configured.
     */
    fun isApiKeyConfigured(): Boolean {
        return getApiKey().isNotBlank()
    }

    /**
     * Generates text or structured content using the Gemini model.
     */
    suspend fun generateContent(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            throw IllegalStateException("GEMINI_API_KEY is not configured in BuildConfig")
        }

        val requestBody = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            )
        )
        val jsonString = requestAdapter.toJson(requestBody)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
            .post(jsonString.toRequestBody(mediaType))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Gemini API Error: HTTP ${response.code} ${response.message}")
        }
        val bodyString = response.body?.string() ?: throw Exception("Empty response body from Gemini API")
        val parsed = responseAdapter.fromJson(bodyString)
        parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
    }
}
