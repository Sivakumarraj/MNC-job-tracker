package com.example.service

import com.example.BuildConfig
import com.squareup.moshi.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiPart(
    @field:Json(name = "text") val text: String
)

data class GeminiContent(
    @field:Json(name = "parts") val parts: List<GeminiPart>
)

data class GeminiRequest(
    @field:Json(name = "contents") val contents: List<GeminiContent>
)

data class GeminiCandidate(
    @field:Json(name = "content") val content: GeminiContent?
)

data class GeminiResponse(
    @field:Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApiRawService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: GeminiApiRawService = retrofit.create(GeminiApiRawService::class.java)

    suspend fun promptGemini(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key Notice: To unlock full real-time Gemini AI resume analysis & cover letter generation, configure your GEMINI_API_KEY in Secrets."
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response from Gemini API."
        } catch (e: Exception) {
            "Gemini Analysis Note: Unable to complete AI call (${e.localizedMessage}). Using built-in offline smart matching heuristic."
        }
    }
}
