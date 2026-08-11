package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.chat.LegalChatMatchingScreen
import com.example.ui.home.LegalAssistantHomeScreen
import com.example.ui.lawyer.LawyerDashboardScreen
import com.example.ui.role.RoleSelectionScreen
import com.example.ui.settings.SettingsModal
import com.example.ui.profile.UserProfileModal
import com.example.ui.components.LawyerDetailModal
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.UserRole

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    RexisLegalAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RexisLegalAppContent(viewModel: MainViewModel) {
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val queryText by viewModel.currentQueryText.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatHistory.collectAsStateWithLifecycle()
    val matchedLawyers by viewModel.matchedLawyers.collectAsStateWithLifecycle()
    val savedLawyers by viewModel.savedLawyers.collectAsStateWithLifecycle()
    val allLawyers by viewModel.allLawyers.collectAsStateWithLifecycle()
    val selectedLawyer by viewModel.selectedLawyerForDetail.collectAsStateWithLifecycle()
    val consultations by viewModel.consultations.collectAsStateWithLifecycle()

    val geminiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val groqKey by viewModel.groqApiKey.collectAsStateWithLifecycle()
    val brevoKey by viewModel.brevoApiKey.collectAsStateWithLifecycle()
    val preferredProvider by viewModel.preferredProvider.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val chatSessions by viewModel.chatSessions.collectAsStateWithLifecycle()
    val activeThreadId by viewModel.activeThreadId.collectAsStateWithLifecycle()
    val legalNotes by viewModel.legalNotes.collectAsStateWithLifecycle()
    val attachedNote by viewModel.attachedNote.collectAsStateWithLifecycle()
    val availableLanguages = viewModel.availableLanguages

    var isSettingsOpen by remember { mutableStateOf(false) }
    var isProfileOpen by remember { mutableStateOf(false) }

    val lawyerDetailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val profileSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentScreen = when {
        userRole == null -> "role_selection"
        userRole == UserRole.LAWYER -> "lawyer_dashboard"
        chatMessages.isEmpty() -> "client_home"
        else -> "client_chat"
    }

    when (currentScreen) {
        "role_selection" -> {
            RoleSelectionScreen(
                onRoleConfirmed = { role ->
                    viewModel.selectRole(role)
                }
            )
        }

        "client_home" -> {
            LegalAssistantHomeScreen(
                queryText = queryText,
                onQueryChange = { viewModel.updateQueryText(it) },
                onSendQuery = { query ->
                    viewModel.sendLegalQuery(query)
                },
                onOpenSettings = { isSettingsOpen = true },
                onOpenProfile = { isProfileOpen = true }
            )
        }

        "client_chat" -> {
            LegalChatMatchingScreen(
                chatMessages = chatMessages,
                matchedLawyers = matchedLawyers,
                isAiLoading = isAiLoading,
                queryText = queryText,
                onQueryChange = { viewModel.updateQueryText(it) },
                onSendQuery = { query ->
                    viewModel.sendLegalQuery(query)
                },
                onBookLawyer = { lawyer ->
                    viewModel.selectLawyerForDetail(lawyer)
                },
                onSaveLawyer = { lawyer ->
                    viewModel.toggleSaveLawyer(lawyer)
                },
                onLawyerClick = { lawyer ->
                    viewModel.selectLawyerForDetail(lawyer)
                },
                onClearChat = { viewModel.clearChat() },
                onStartNewSession = { viewModel.startNewChatSession() },
                attachedNote = attachedNote,
                legalNotes = legalNotes,
                onAttachNote = { note -> viewModel.attachNoteToQuery(note) },
                onSaveNote = { title, content, category, id -> viewModel.saveLegalNote(title, content, category, id) },
                onDeleteNote = { id -> viewModel.deleteLegalNote(id) },
                onOpenSettings = { isSettingsOpen = true },
                onOpenProfile = { isProfileOpen = true }
            )
        }

        "lawyer_dashboard" -> {
            val defaultLawyer = allLawyers.firstOrNull()
            LawyerDashboardScreen(
                lawyerProfile = defaultLawyer,
                consultations = consultations,
                onUpdateConsultationStatus = { id, status ->
                    viewModel.updateConsultationStatus(id, status)
                },
                onSwitchToClient = {
                    viewModel.selectRole(UserRole.CLIENT)
                },
                onOpenSettings = { isSettingsOpen = true },
                onOpenProfile = { isProfileOpen = true }
            )
        }
    }

    // Lawyer Detail Bottom Sheet
    if (selectedLawyer != null) {
        LawyerDetailModal(
            lawyer = selectedLawyer,
            sheetState = lawyerDetailSheetState,
            onDismiss = { viewModel.selectLawyerForDetail(null) },
            onConfirmBooking = { id, name, cName, contact, notes, date ->
                viewModel.bookConsultation(id, name, cName, contact, notes, date)
            }
        )
    }

    // Settings Bottom Sheet
    if (isSettingsOpen) {
        SettingsModal(
            geminiKey = geminiKey,
            groqKey = groqKey,
            preferredProvider = preferredProvider,
            currentRole = userRole,
            selectedLanguage = selectedLanguage,
            availableLanguages = availableLanguages,
            sheetState = settingsSheetState,
            onSaveSettings = { gKey, grKey, prov ->
                viewModel.updateApiKeys(gKey, grKey, prov)
            },
            onLanguageChange = { lang ->
                viewModel.updateLanguage(lang)
            },
            onRoleChange = { role ->
                viewModel.selectRole(role)
            },
            onDismiss = { isSettingsOpen = false }
        )
    }

    // User Profile & Legal Hub Sheet
    if (isProfileOpen) {
        UserProfileModal(
            currentRole = userRole,
            consultations = consultations,
            savedLawyers = savedLawyers,
            chatSessions = chatSessions,
            activeThreadId = activeThreadId,
            geminiKey = geminiKey,
            groqKey = groqKey,
            brevoKey = brevoKey,
            preferredProvider = preferredProvider,
            selectedLanguage = selectedLanguage,
            availableLanguages = availableLanguages,
            sheetState = profileSheetState,
            onDismiss = { isProfileOpen = false },
            onSaveSettings = { gKey, grKey, brKey, prov ->
                viewModel.updateApiKeys(gKey, grKey, brKey, prov)
            },
            onLanguageChange = { lang ->
                viewModel.updateLanguage(lang)
            },
            onRoleChange = { role ->
                viewModel.selectRole(role)
            },
            onBookLawyer = { lawyer ->
                viewModel.selectLawyerForDetail(lawyer)
            },
            onRemoveSavedLawyer = { lawyer ->
                viewModel.toggleSaveLawyer(lawyer)
            },
            onUpdateConsultationStatus = { id, status ->
                viewModel.updateConsultationStatus(id, status)
            },
            onSelectSession = { threadId ->
                viewModel.selectChatSession(threadId)
            },
            onStartNewSession = {
                viewModel.startNewChatSession()
            },
            onDeleteSession = { threadId ->
                viewModel.deleteChatSession(threadId)
            },
            onClearChat = { viewModel.clearChat() }
        )
    }
}
