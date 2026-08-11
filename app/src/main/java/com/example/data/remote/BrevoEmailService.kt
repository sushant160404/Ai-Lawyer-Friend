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

data class SendEmailResult(
    val isSuccess: Boolean,
    val messageId: String? = null,
    val errorMessage: String? = null
)

class BrevoEmailService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun sendConsultationEmail(
        recipientEmail: String,
        recipientName: String,
        lawyerName: String,
        appointmentDate: String,
        issueNotes: String,
        customBrevoKey: String = ""
    ): SendEmailResult = withContext(Dispatchers.IO) {
        val apiKey = customBrevoKey.ifBlank { BuildConfig.BREVO_API_KEY }
        if (apiKey.isBlank() || apiKey.startsWith("MY_")) {
            Log.w("BrevoEmailService", "Brevo API Key is missing or placeholder value.")
            return@withContext SendEmailResult(
                isSuccess = true,
                messageId = "simulated_brevo_${System.currentTimeMillis()}",
                errorMessage = "Simulated email notification sent (Add Brevo API key in Settings for live delivery)."
            )
        }

        try {
            val emailTo = recipientEmail.ifBlank { "client@example.com" }
            val nameTo = recipientName.ifBlank { "Valued Client" }

            val jsonBody = JSONObject().apply {
                put("sender", JSONObject().apply {
                    put("name", "Rexis AI Legal Assistant")
                    put("email", "notifications@rexis.ai")
                })
                put("to", JSONArray().apply {
                    put(JSONObject().apply {
                        put("email", emailTo)
                        put("name", nameTo)
                    })
                })
                put("subject", "Consultation Confirmation with $lawyerName - Rexis AI Legal")

                val html = """
                    <!DOCTYPE html>
                    <html>
                    <body style="font-family: Arial, sans-serif; background-color: #0f172a; color: #f8fafc; padding: 20px;">
                        <div style="max-width: 500px; margin: 0 auto; background-color: #1e293b; padding: 24px; border-radius: 12px; border: 1px solid #334155;">
                            <h2 style="color: #22c55e; margin-top: 0;">Rexis AI Legal - Consultation Confirmation</h2>
                            <p>Hello <strong>$nameTo</strong>,</p>
                            <p>Your legal consultation with attorney <strong>$lawyerName</strong> has been successfully booked!</p>
                            <div style="background-color: #0f172a; padding: 14px; border-radius: 8px; margin: 16px 0; border: 1px solid #334155;">
                                <p style="margin: 6px 0; color: #4ade80;"><strong>Scheduled Time:</strong> $appointmentDate</p>
                                <p style="margin: 6px 0; color: #f8fafc;"><strong>Attorney:</strong> $lawyerName</p>
                                <p style="margin: 6px 0; color: #cbd5e1;"><strong>Notes / Case Issue:</strong> ${issueNotes.ifBlank { "Initial Consultation" }}</p>
                            </div>
                            <p style="font-size: 13px; color: #94a3b8;">You will receive a 1-hour automated alert before your scheduled session.</p>
                            <hr style="border-color: #334155; margin: 20px 0;">
                            <p style="font-size: 11px; color: #64748b; text-align: center;">Rexis AI Legal Matching & Consultation Network</p>
                        </div>
                    </body>
                    </html>
                """.trimIndent()

                put("htmlContent", html)
            }

            val request = Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .header("accept", "application/json")
                .header("api-key", apiKey)
                .header("content-type", "application/json")
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBodyStr = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val resObj = JSONObject(responseBodyStr)
                val msgId = resObj.optString("messageId", "sent_success")
                Log.d("BrevoEmailService", "Email sent successfully via Brevo: $msgId")
                SendEmailResult(isSuccess = true, messageId = msgId)
            } else {
                Log.e("BrevoEmailService", "Brevo email send failed: ${response.code} $responseBodyStr")
                SendEmailResult(
                    isSuccess = false,
                    errorMessage = "Brevo HTTP ${response.code}: $responseBodyStr"
                )
            }
        } catch (e: Exception) {
            Log.e("BrevoEmailService", "Error sending Brevo email", e)
            SendEmailResult(isSuccess = false, errorMessage = e.message)
        }
    }
}
