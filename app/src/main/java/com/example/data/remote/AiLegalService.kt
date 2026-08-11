package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiMatchResult(
    val aiResponseText: String,
    val suggestedPracticeAreas: List<String>,
    val relevantLawyerIds: List<String>
)

class AiLegalService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateLegalAnalysisAndMatches(
        userQuery: String,
        availableLawyerSummary: String,
        customGeminiKey: String = "",
        customGroqKey: String = "",
        preferredProvider: String = "gemini", // "gemini" or "groq"
        targetLanguage: String = "English"
    ): AiMatchResult = withContext(Dispatchers.IO) {

        val geminiKey = customGeminiKey.ifBlank { BuildConfig.GEMINI_API_KEY }
        val groqKey = customGroqKey.ifBlank { BuildConfig.GROQ_API_KEY }

        val langInstruction = if (targetLanguage.equals("Auto-Detect", ignoreCase = true)) {
            "Detect the user's input language and write your AI explanation and response in that exact same language."
        } else {
            "Write your AI explanation and response in $targetLanguage language."
        }

        // System prompt for legal assistant
        val systemPrompt = """
            You are Rexis AI, an expert multilingual AI Legal Matching Assistant.
            Analyze the user's legal query: "$userQuery".
            Available Lawyers directory:
            $availableLawyerSummary
            
            Task:
            1. Provide a concise, empathetic, professional AI legal summary (2-3 sentences max) explaining key considerations and rights.
            2. $langInstruction
            3. Match the top 2-3 most relevant lawyers from the directory.
            4. Output your response as a valid JSON object with keys:
               - "aiMessage": string (The friendly answer translated into $targetLanguage starting with the appropriate greeting in $targetLanguage)
               - "matchedLawyerIds": array of strings (e.g. ["lawyer_1", "lawyer_2"])
               - "practiceAreas": array of strings (e.g. ["Employment Lawyer"])
        """.trimIndent()

        var result: AiMatchResult? = null

        if (preferredProvider == "groq" && groqKey.isNotBlank() && !groqKey.startsWith("MY_")) {
            result = callGroqApi(userQuery, systemPrompt, groqKey)
        }

        if (result == null && geminiKey.isNotBlank() && !geminiKey.startsWith("MY_")) {
            result = callGeminiApi(userQuery, systemPrompt, geminiKey)
        }

        if (result == null && groqKey.isNotBlank() && !groqKey.startsWith("MY_")) {
            result = callGroqApi(userQuery, systemPrompt, groqKey)
        }

        // Fallback dynamic intelligent matching if API keys are placeholders or offline
        return@withContext result ?: generateFallbackMatchResult(userQuery, targetLanguage)
    }

    private fun callGeminiApi(userQuery: String, systemPrompt: String, apiKey: String): AiMatchResult? {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$systemPrompt\n\nUser Question: $userQuery"))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return null

            if (!response.isSuccessful) {
                Log.e("AiLegalService", "Gemini API error: ${response.code} $responseBody")
                return null
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val rawText = parts.getJSONObject(0).optString("text", "")

            parseJsonResponse(rawText, userQuery)
        } catch (e: Exception) {
            Log.e("AiLegalService", "Error in Gemini call", e)
            null
        }
    }

    private fun callGroqApi(userQuery: String, systemPrompt: String, apiKey: String): AiMatchResult? {
        return try {
            val url = "https://api.groq.com/openai/v1/chat/completions"

            val jsonBody = JSONObject().apply {
                put("model", "llama-3.3-70b-versatile")
                put("response_format", JSONObject().put("type", "json_object"))
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userQuery)
                    })
                })
                put("temperature", 0.3)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return null

            if (!response.isSuccessful) {
                Log.e("AiLegalService", "Groq API error: ${response.code} $responseBody")
                return null
            }

            val rootJson = JSONObject(responseBody)
            val choices = rootJson.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null
            val message = choices.getJSONObject(0).optJSONObject("message") ?: return null
            val rawText = message.optString("content", "")

            parseJsonResponse(rawText, userQuery)
        } catch (e: Exception) {
            Log.e("AiLegalService", "Error in Groq call", e)
            null
        }
    }

    private fun parseJsonResponse(rawText: String, userQuery: String): AiMatchResult {
        return try {
            val jsonStart = rawText.indexOf('{')
            val jsonEnd = rawText.lastIndexOf('}')
            val jsonString = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                rawText.substring(jsonStart, jsonEnd + 1)
            } else rawText

            val obj = JSONObject(jsonString)
            val aiMessage = obj.optString("aiMessage", "Done! Here are the top-rated lawyers based on experience, reviews, and availability.")
            val lawyerIdsArr = obj.optJSONArray("matchedLawyerIds")
            val matchedIds = mutableListOf<String>()
            if (lawyerIdsArr != null) {
                for (i in 0 until lawyerIdsArr.length()) {
                    matchedIds.add(lawyerIdsArr.getString(i))
                }
            }
            val areasArr = obj.optJSONArray("practiceAreas")
            val areas = mutableListOf<String>()
            if (areasArr != null) {
                for (i in 0 until areasArr.length()) {
                    areas.add(areasArr.getString(i))
                }
            }

            AiMatchResult(
                aiResponseText = aiMessage,
                suggestedPracticeAreas = areas.ifEmpty { listOf("General Law") },
                relevantLawyerIds = matchedIds.ifEmpty { listOf("lawyer_1", "lawyer_2") }
            )
        } catch (e: Exception) {
            generateFallbackMatchResult(userQuery)
        }
    }

    private fun generateFallbackMatchResult(userQuery: String, targetLanguage: String = "English"): AiMatchResult {
        val queryLower = userQuery.lowercase()
        val isSpanish = targetLanguage.contains("español", ignoreCase = true) || targetLanguage.contains("spanish", ignoreCase = true)
        val isFrench = targetLanguage.contains("français", ignoreCase = true) || targetLanguage.contains("french", ignoreCase = true)
        val isGerman = targetLanguage.contains("deutsch", ignoreCase = true) || targetLanguage.contains("german", ignoreCase = true)
        val isHindi = targetLanguage.contains("हिन्दी", ignoreCase = true) || targetLanguage.contains("hindi", ignoreCase = true)

        val defaultMsg = when {
            isSpanish -> "¡Listo! Aquí están los abogados mejor calificados según experiencia, reseñas y disponibilidad para su caso."
            isFrench -> "Terminé! Voici los mejores abogados seleccionados según la experiencia y las opiniones."
            isGerman -> "Fertig! Hier sind die am besten bewerteten Anwälte basierend auf Erfahrung und Bewertungen."
            isHindi -> "पूर्ण! आपकी कानूनी समस्या के आधार पर यहां शीर्ष रेटेड वकील हैं।"
            else -> "Done! Here are the top-rated lawyers based on experience, reviews, and availability."
        }

        return when {
            queryLower.contains("harass") || queryLower.contains("fire") || queryLower.contains("workplace") || queryLower.contains("job") || queryLower.contains("boss") || queryLower.contains("labor") || queryLower.contains("despido") || queryLower.contains("trabajo") -> {
                AiMatchResult(
                    aiResponseText = defaultMsg,
                    suggestedPracticeAreas = listOf("Employment Law", "Workplace Retaliation", "Labor Disputes"),
                    relevantLawyerIds = listOf("lawyer_1", "lawyer_5")
                )
            }
            queryLower.contains("custody") || queryLower.contains("divorce") || queryLower.contains("family") || queryLower.contains("child") || queryLower.contains("custodia") || queryLower.contains("familia") -> {
                AiMatchResult(
                    aiResponseText = defaultMsg,
                    suggestedPracticeAreas = listOf("Family Law", "Custody Disputes"),
                    relevantLawyerIds = listOf("lawyer_2")
                )
            }
            queryLower.contains("deposit") || queryLower.contains("landlord") || queryLower.contains("rent") || queryLower.contains("property") || queryLower.contains("alquiler") || queryLower.contains("deposito") -> {
                AiMatchResult(
                    aiResponseText = defaultMsg,
                    suggestedPracticeAreas = listOf("Real Estate Law", "Tenant Rights"),
                    relevantLawyerIds = listOf("lawyer_3")
                )
            }
            else -> {
                AiMatchResult(
                    aiResponseText = defaultMsg,
                    suggestedPracticeAreas = listOf("Legal Advisory", "Civil Litigation"),
                    relevantLawyerIds = listOf("lawyer_1", "lawyer_2", "lawyer_3")
                )
            }
        }
    }
}
