package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object NotificationUtils {

    const val CHANNEL_ID = "consultation_reminders_channel"
    private const val CHANNEL_NAME = "Consultation Reminders"
    private const val CHANNEL_DESC = "Alerts for upcoming attorney consultations 1 hour before scheduled time"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerConsultationReminder(context: Context, lawyerName: String, scheduledTime: String) {
        createNotificationChannel(context)

        val notificationId = (System.currentTimeMillis() % 10000).toInt()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Consultation Reminder (1 Hour Left)")
            .setContentText("Upcoming legal consultation with $lawyerName at $scheduledTime.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Reminder: Your consultation with $lawyerName is scheduled for $scheduledTime. Please prepare your relevant legal documents and case notes.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
            Toast.makeText(
                context,
                "1-Hour Reminder notification scheduled for $lawyerName ($scheduledTime)",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: SecurityException) {
            Toast.makeText(context, "Consultation reminder created for $scheduledTime", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerBrevoEmailNotification(
        context: Context,
        lawyerName: String,
        appointmentTime: String,
        clientName: String = "Valued Client",
        clientEmail: String = "client@example.com",
        notes: String = "Consultation Reminder",
        customBrevoKey: String = ""
    ) {
        val service = com.example.data.remote.BrevoEmailService()
        CoroutineScope(Dispatchers.IO).launch {
            val res = service.sendConsultationEmail(
                recipientEmail = clientEmail,
                recipientName = clientName,
                lawyerName = lawyerName,
                appointmentDate = appointmentTime,
                issueNotes = notes,
                customBrevoKey = customBrevoKey
            )
            withContext(Dispatchers.Main) {
                val msg = res.errorMessage ?: "Brevo Email Notification sent (ID: ${res.messageId})"
                Toast.makeText(context, "Brevo Email: $msg", Toast.LENGTH_LONG).show()
            }
        }
    }
}
