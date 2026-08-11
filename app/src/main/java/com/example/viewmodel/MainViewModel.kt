package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChatEntity
import com.example.data.local.ChatSessionSummary
import com.example.data.local.ConsultationEntity
import com.example.data.local.LawyerEntity
import com.example.data.local.LegalNoteEntity
import com.example.data.repository.LawyerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole {
    CLIENT, LAWYER
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LawyerRepository

    private val _userRole = MutableStateFlow<UserRole?>(null) // null = initial role selection screen
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    private val _currentQueryText = MutableStateFlow("")
    val currentQueryText: StateFlow<String> = _currentQueryText.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _selectedLawyerForDetail = MutableStateFlow<LawyerEntity?>(null)
    val selectedLawyerForDetail: StateFlow<LawyerEntity?> = _selectedLawyerForDetail.asStateFlow()

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _groqApiKey = MutableStateFlow("")
    val groqApiKey: StateFlow<String> = _groqApiKey.asStateFlow()

    private val _brevoApiKey = MutableStateFlow("")
    val brevoApiKey: StateFlow<String> = _brevoApiKey.asStateFlow()

    private val _preferredProvider = MutableStateFlow("gemini") // "gemini" or "groq"
    val preferredProvider: StateFlow<String> = _preferredProvider.asStateFlow()

    private val brevoEmailService = com.example.data.remote.BrevoEmailService()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    val availableLanguages = listOf("English", "Español", "Français", "Deutsch", "हिन्दी", "中文", "Auto-Detect")

    private val _lastMatchedLawyerIds = MutableStateFlow<List<String>>(emptyList())
    val lastMatchedLawyerIds: StateFlow<List<String>> = _lastMatchedLawyerIds.asStateFlow()

    private val _activeThreadId = MutableStateFlow("session_" + System.currentTimeMillis())
    val activeThreadId: StateFlow<String> = _activeThreadId.asStateFlow()

    private val _attachedNote = MutableStateFlow<LegalNoteEntity?>(null)
    val attachedNote: StateFlow<LegalNoteEntity?> = _attachedNote.asStateFlow()

    val allLawyers: StateFlow<List<LawyerEntity>>
    val savedLawyers: StateFlow<List<LawyerEntity>>
    val matchedLawyers: StateFlow<List<LawyerEntity>>
    val chatHistory: StateFlow<List<ChatEntity>>
    val chatSessions: StateFlow<List<ChatSessionSummary>>
    val consultations: StateFlow<List<ConsultationEntity>>
    val legalNotes: StateFlow<List<LegalNoteEntity>>

    init {
        val db = AppDatabase.getInstance(application)
        repository = LawyerRepository(db)

        allLawyers = repository.allLawyers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savedLawyers = repository.savedLawyers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        chatSessions = repository.allChatMessages
            .map { messages ->
                messages.groupBy { it.threadId }.map { (threadId, msgs) ->
                    val firstUserQuery = msgs.firstOrNull { it.senderRole == "user" }?.text
                        ?: msgs.firstOrNull()?.text ?: "Legal Advice Session"
                    val lastMsg = msgs.lastOrNull()?.text ?: ""
                    val matchedIds = msgs.mapNotNull { if (it.matchedLawyerIds.isNotBlank()) it.matchedLawyerIds.split(",") else null }
                        .flatten().distinct().filter { it.isNotBlank() }
                    ChatSessionSummary(
                        threadId = threadId,
                        title = if (firstUserQuery.length > 45) firstUserQuery.take(45) + "..." else firstUserQuery,
                        lastMessageText = if (lastMsg.length > 60) lastMsg.take(60) + "..." else lastMsg,
                        messageCount = msgs.size,
                        matchedLawyerIds = matchedIds,
                        timestamp = msgs.lastOrNull()?.timestamp ?: System.currentTimeMillis()
                    )
                }.sortedByDescending { it.timestamp }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        chatHistory = combine(repository.allChatMessages, _activeThreadId) { allMsgs, activeThread ->
            allMsgs.filter { it.threadId == activeThread }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        consultations = repository.allConsultations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        legalNotes = repository.allNotes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        matchedLawyers = combine(allLawyers, chatHistory, _lastMatchedLawyerIds) { lawyers, historyMsgs, manualIds ->
            if (manualIds.isNotEmpty()) {
                lawyers.filter { manualIds.contains(it.id) }.ifEmpty { lawyers.take(3) }
            } else {
                val threadMatchedIds = historyMsgs.filter { it.senderRole == "assistant" && it.matchedLawyerIds.isNotBlank() }
                    .flatMap { it.matchedLawyerIds.split(",") }
                    .distinct()
                    .filter { it.isNotBlank() }
                if (threadMatchedIds.isNotEmpty()) {
                    lawyers.filter { threadMatchedIds.contains(it.id) }
                } else {
                    lawyers.take(3)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun selectRole(role: UserRole) {
        _userRole.value = role
    }

    fun updateQueryText(text: String) {
        _currentQueryText.value = text
    }

    fun updateApiKeys(geminiKey: String, groqKey: String, provider: String = "gemini", brevoKey: String = "") {
        _geminiApiKey.value = geminiKey
        _groqApiKey.value = groqKey
        if (brevoKey.isNotBlank()) _brevoApiKey.value = brevoKey
        _preferredProvider.value = provider
    }

    fun sendBrevoEmailNotification(
        recipientEmail: String,
        recipientName: String,
        lawyerName: String,
        appointmentDate: String,
        notes: String,
        onResult: (isSuccess: Boolean, message: String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = brevoEmailService.sendConsultationEmail(
                recipientEmail = recipientEmail,
                recipientName = recipientName,
                lawyerName = lawyerName,
                appointmentDate = appointmentDate,
                issueNotes = notes,
                customBrevoKey = _brevoApiKey.value
            )
            if (result.isSuccess) {
                val feedback = if (result.errorMessage != null) {
                    result.errorMessage
                } else {
                    "Brevo email notification sent successfully (ID: ${result.messageId})"
                }
                onResult(true, feedback)
            } else {
                onResult(false, result.errorMessage ?: "Failed to send Brevo email notification")
            }
        }
    }

    fun updateLanguage(language: String) {
        _selectedLanguage.value = language
    }

    fun attachNoteToQuery(note: LegalNoteEntity?) {
        _attachedNote.value = note
    }

    fun saveLegalNote(title: String, content: String, category: String = "General Legal", id: Long = 0) {
        viewModelScope.launch {
            repository.saveNote(
                id = id,
                title = title,
                content = content,
                category = category
            )
        }
    }

    fun deleteLegalNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
            if (_attachedNote.value?.id == id) {
                _attachedNote.value = null
            }
        }
    }

    fun sendLegalQuery(queryOverride: String? = null) {
        val baseQuery = queryOverride ?: _currentQueryText.value
        if (baseQuery.isBlank() && _attachedNote.value == null) return

        val attached = _attachedNote.value
        val fullQueryText = if (attached != null) {
            val userText = if (baseQuery.isBlank()) "Please review my attached legal note and advise on next steps." else baseQuery
            "📌 [Attached Digital Legal Summary]\nTitle: ${attached.title}\nCategory: ${attached.category}\nCase Details & Notes:\n${attached.content}\n\nUser Question:\n$userText"
        } else {
            baseQuery
        }

        _isAiLoading.value = true
        if (queryOverride == null) {
            _currentQueryText.value = ""
        }
        _attachedNote.value = null // Reset attached note after sending

        viewModelScope.launch {
            val result = repository.processUserLegalQuery(
                threadId = _activeThreadId.value,
                queryText = fullQueryText,
                geminiKey = _geminiApiKey.value,
                groqKey = _groqApiKey.value,
                provider = _preferredProvider.value,
                targetLanguage = _selectedLanguage.value
            )
            _lastMatchedLawyerIds.value = result.relevantLawyerIds
            _isAiLoading.value = false
        }
    }

    fun startNewChatSession() {
        _activeThreadId.value = "session_" + System.currentTimeMillis()
        _lastMatchedLawyerIds.value = emptyList()
    }

    fun selectChatSession(threadId: String) {
        _activeThreadId.value = threadId
        _lastMatchedLawyerIds.value = emptyList()
    }

    fun deleteChatSession(threadId: String) {
        viewModelScope.launch {
            repository.deleteChatThread(threadId)
            if (_activeThreadId.value == threadId) {
                startNewChatSession()
            }
        }
    }

    fun selectLawyerForDetail(lawyer: LawyerEntity?) {
        _selectedLawyerForDetail.value = lawyer
    }

    fun toggleSaveLawyer(lawyer: LawyerEntity) {
        viewModelScope.launch {
            repository.toggleSaveLawyer(lawyer)
        }
    }

    fun bookConsultation(
        lawyerId: String,
        lawyerName: String,
        clientName: String,
        clientContact: String,
        notes: String,
        date: String
    ) {
        viewModelScope.launch {
            repository.bookConsultation(
                lawyerId = lawyerId,
                lawyerName = lawyerName,
                clientName = clientName,
                clientContact = clientContact,
                issueSummary = notes,
                date = date
            )
        }
    }

    fun updateConsultationStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateConsultationStatus(id, newStatus)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            _lastMatchedLawyerIds.value = emptyList()
        }
    }
}
