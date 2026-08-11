package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lawyers")
data class LawyerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val practiceArea: String, // e.g. "Employment Lawyer", "Family Lawyer", "Corporate Law"
    val hourlyRate: Double,
    val currencySymbol: String = "€",
    val location: String, // e.g. "Los Angeles, CA"
    val rating: Double, // e.g. 4.8
    val reviewCount: Int, // e.g. 102
    val experienceYears: Int, // e.g. 10
    val casesHandled: Int, // e.g. 219
    val bio: String,
    val avatarResourceName: String, // e.g. "img_lawyer_michale_1786371723292"
    val isSaved: Boolean = false,
    val availableHours: String = "Mon-Fri (9:00 - 18:00)",
    val specialitiesJson: String = "[]" // JSON string of tags
)

@Entity(tableName = "chat_messages")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadId: String = "default_thread",
    val senderRole: String, // "user" or "assistant"
    val text: String,
    val matchedLawyerIds: String = "", // Comma-separated lawyer IDs
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "consultations")
data class ConsultationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lawyerId: String,
    val lawyerName: String,
    val clientName: String,
    val clientContact: String,
    val issueSummary: String,
    val appointmentDate: String,
    val status: String = "Pending", // "Pending", "Confirmed", "Completed"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "legal_notes")
data class LegalNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "General Legal", // e.g. "Employment", "Contract", "Family", "Property"
    val isEncrypted: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ChatSessionSummary(
    val threadId: String,
    val title: String,
    val lastMessageText: String,
    val messageCount: Int,
    val matchedLawyerIds: List<String>,
    val timestamp: Long
)
