package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ChatEntity
import com.example.data.local.ConsultationEntity
import com.example.data.local.LawyerEntity
import com.example.data.local.LegalNoteEntity
import com.example.data.remote.AiLegalService
import com.example.data.remote.AiMatchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LawyerRepository(private val database: AppDatabase) {

    private val lawyerDao = database.lawyerDao()
    private val chatDao = database.chatDao()
    private val consultationDao = database.consultationDao()
    private val noteDao = database.noteDao()
    private val aiService = AiLegalService()

    val allLawyers: Flow<List<LawyerEntity>> = lawyerDao.getAllLawyers()
    val savedLawyers: Flow<List<LawyerEntity>> = lawyerDao.getSavedLawyers()
    val allChatMessages: Flow<List<ChatEntity>> = chatDao.getAllChatMessages()
    fun getChatHistoryForThread(threadId: String): Flow<List<ChatEntity>> = chatDao.getChatHistory(threadId)
    val allConsultations: Flow<List<ConsultationEntity>> = consultationDao.getAllConsultations()
    val allNotes: Flow<List<LegalNoteEntity>> = noteDao.getAllNotes()

    suspend fun seedInitialDataIfEmpty() {
        val existing = lawyerDao.getAllLawyers().first()
        if (existing.isEmpty()) {
            val initialLawyers = listOf(
                LawyerEntity(
                    id = "lawyer_1",
                    name = "Michale Chan",
                    practiceArea = "Employment Lawyer",
                    hourlyRate = 50.0,
                    currencySymbol = "€",
                    location = "Los Angeles, CA",
                    rating = 4.8,
                    reviewCount = 102,
                    experienceYears = 10,
                    casesHandled = 219,
                    bio = "Specializing in workplace harassment, wrongful termination, and severance negotiations. Over $12M recovered for employees.",
                    avatarResourceName = "img_lawyer_michale_1786371723292",
                    isSaved = true
                ),
                LawyerEntity(
                    id = "lawyer_2",
                    name = "Sarah Jenkins",
                    practiceArea = "Family & Custody",
                    hourlyRate = 65.0,
                    currencySymbol = "€",
                    location = "New York, NY",
                    rating = 4.9,
                    reviewCount = 184,
                    experienceYears = 12,
                    casesHandled = 310,
                    bio = "Dedicated family law practitioner focusing on child custody disputes, divorce mediation, and marital property division.",
                    avatarResourceName = "img_lawyer_sarah_1786371743752",
                    isSaved = false
                ),
                LawyerEntity(
                    id = "lawyer_3",
                    name = "David Vance",
                    practiceArea = "Real Estate & Tenant",
                    hourlyRate = 80.0,
                    currencySymbol = "€",
                    location = "San Francisco, CA",
                    rating = 4.7,
                    reviewCount = 95,
                    experienceYears = 15,
                    casesHandled = 180,
                    bio = "Expert in landlord-tenant disputes, security deposit recovery, commercial leases, and real estate litigation.",
                    avatarResourceName = "img_lawyer_david_1786371766939",
                    isSaved = false
                ),
                LawyerEntity(
                    id = "lawyer_4",
                    name = "Elena Rostova",
                    practiceArea = "Personal Injury",
                    hourlyRate = 55.0,
                    currencySymbol = "€",
                    location = "Chicago, IL",
                    rating = 4.9,
                    reviewCount = 240,
                    experienceYears = 9,
                    casesHandled = 340,
                    bio = "Aggressive advocate for personal injury, auto accidents, and medical malpractice victims. No fee unless we win.",
                    avatarResourceName = "img_lawyer_sarah_1786371743752",
                    isSaved = false
                ),
                LawyerEntity(
                    id = "lawyer_5",
                    name = "Marcus Thorne",
                    practiceArea = "Corporate & Contracts",
                    hourlyRate = 95.0,
                    currencySymbol = "€",
                    location = "Austin, TX",
                    rating = 4.8,
                    reviewCount = 112,
                    experienceYears = 14,
                    casesHandled = 205,
                    bio = "Advisor to startups and corporations on IP protection, contract reviews, NDAs, and corporate compliance.",
                    avatarResourceName = "img_lawyer_david_1786371766939",
                    isSaved = false
                )
            )
            lawyerDao.insertLawyers(initialLawyers)
        }
    }

    suspend fun processUserLegalQuery(
        threadId: String = "default_thread",
        queryText: String,
        geminiKey: String = "",
        groqKey: String = "",
        provider: String = "gemini",
        targetLanguage: String = "English"
    ): AiMatchResult {
        // 1. Save user query in chat
        chatDao.insertMessage(
            ChatEntity(
                threadId = threadId,
                senderRole = "user",
                text = queryText
            )
        )

        // 2. Fetch lawyers for context
        val currentLawyers = lawyerDao.getAllLawyers().first()
        val summary = currentLawyers.joinToString("\n") {
            "ID: ${it.id}, Name: ${it.name}, Area: ${it.practiceArea}, Rate: ${it.currencySymbol}${it.hourlyRate}/hr, Exp: ${it.experienceYears}yr, Cases: ${it.casesHandled}, Rating: ${it.rating}"
        }

        // 3. Call AI Service
        val matchResult = aiService.generateLegalAnalysisAndMatches(
            userQuery = queryText,
            availableLawyerSummary = summary,
            customGeminiKey = geminiKey,
            customGroqKey = groqKey,
            preferredProvider = provider,
            targetLanguage = targetLanguage
        )

        // 4. Save AI response in chat
        chatDao.insertMessage(
            ChatEntity(
                threadId = threadId,
                senderRole = "assistant",
                text = matchResult.aiResponseText,
                matchedLawyerIds = matchResult.relevantLawyerIds.joinToString(",")
            )
        )

        return matchResult
    }

    suspend fun toggleSaveLawyer(lawyer: LawyerEntity) {
        lawyerDao.updateLawyer(lawyer.copy(isSaved = !lawyer.isSaved))
    }

    suspend fun bookConsultation(
        lawyerId: String,
        lawyerName: String,
        clientName: String,
        clientContact: String,
        issueSummary: String,
        date: String
    ): Long {
        return consultationDao.insertConsultation(
            ConsultationEntity(
                lawyerId = lawyerId,
                lawyerName = lawyerName,
                clientName = clientName,
                clientContact = clientContact,
                issueSummary = issueSummary,
                appointmentDate = date,
                status = "Pending"
            )
        )
    }

    suspend fun updateConsultationStatus(id: Long, newStatus: String) {
        consultationDao.updateStatus(id, newStatus)
    }

    suspend fun deleteChatThread(threadId: String) {
        chatDao.deleteThread(threadId)
    }

    suspend fun clearChatHistory() {
        chatDao.clearHistory()
    }

    suspend fun saveNote(title: String, content: String, category: String = "General Legal", id: Long = 0): Long {
        return noteDao.insertNote(
            LegalNoteEntity(
                id = id,
                title = title,
                content = content,
                category = category,
                isEncrypted = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNote(id: Long) {
        noteDao.deleteNoteById(id)
    }
}
