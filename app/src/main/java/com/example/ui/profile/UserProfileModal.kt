package com.example.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.History
import com.example.data.local.ChatSessionSummary
import com.example.data.local.ConsultationEntity
import com.example.data.local.LawyerEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.components.GlassCard
import com.example.ui.components.rememberLawyerImageResource
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreenPrimary
import com.example.ui.theme.PriceTagGreen
import com.example.ui.theme.StarGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileModal(
    currentRole: UserRole?,
    consultations: List<ConsultationEntity>,
    savedLawyers: List<LawyerEntity>,
    chatSessions: List<ChatSessionSummary> = emptyList(),
    activeThreadId: String = "",
    geminiKey: String,
    groqKey: String,
    brevoKey: String = "",
    preferredProvider: String,
    selectedLanguage: String,
    availableLanguages: List<String>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSaveSettings: (geminiKey: String, groqKey: String, provider: String, brevoKey: String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onBookLawyer: (LawyerEntity) -> Unit,
    onRemoveSavedLawyer: (LawyerEntity) -> Unit,
    onUpdateConsultationStatus: (id: Long, status: String) -> Unit,
    onSelectSession: (String) -> Unit = {},
    onStartNewSession: () -> Unit = {},
    onDeleteSession: (String) -> Unit = {},
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Consultations, 1: Saved Lawyers, 2: History, 3: AI Settings

    var tempGeminiKey by remember { mutableStateOf(geminiKey) }
    var tempGroqKey by remember { mutableStateOf(groqKey) }
    var tempBrevoKey by remember { mutableStateOf(brevoKey) }
    var tempProvider by remember { mutableStateOf(preferredProvider) }
    var tempLang by remember { mutableStateOf(selectedLanguage) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        contentColor = TextPrimary,
        modifier = modifier.testTag("user_profile_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Profile & Legal Hub",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            // User Info Banner Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                cornerRadius = 20.dp,
                backgroundColor = DarkSurface,
                borderColor = DarkCardBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(2.dp, NeonGreenPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = NeonGreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentRole == UserRole.LAWYER) "Attorney Alex Morgan" else "Alex Morgan",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "alex.morgan@law.ai",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Role Badge Chip
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E2922))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (currentRole == UserRole.LAWYER) "Verified Attorney Portal" else "Client Account",
                                    color = NeonGreenPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Switch",
                                color = PriceTagGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        val newRole = if (currentRole == UserRole.LAWYER) UserRole.CLIENT else UserRole.LAWYER
                                        onRoleChange(newRole)
                                    }
                                    .testTag("switch_role_text_button")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Navigation (Consultations, Saved Lawyers, History, Assistant Settings)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkBackground,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonGreenPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .testTag("profile_tab_row")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bookings (${consultations.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    selectedContentColor = NeonGreenPrimary,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_bookings")
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Saved (${savedLawyers.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    selectedContentColor = NeonGreenPrimary,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_saved_lawyers")
                )

                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "History (${chatSessions.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    selectedContentColor = NeonGreenPrimary,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_history")
                )

                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI Config",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    selectedContentColor = NeonGreenPrimary,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_ai_config")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 0: Consultations List
                        if (consultations.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Booked Consultations Yet",
                                        color = TextSecondary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Search legal issues to match with attorneys and schedule consultations.",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            consultations.forEach { consultation ->
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    cornerRadius = 16.dp,
                                    backgroundColor = DarkSurface,
                                    borderColor = DarkCardBorder
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = consultation.lawyerName,
                                                color = TextPrimary,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            val statusBg = when (consultation.status) {
                                                "Confirmed" -> Color(0xFF166534)
                                                "Completed" -> Color(0xFF1E293B)
                                                else -> Color(0xFF854D0E)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(statusBg)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = consultation.status,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                tint = NeonGreenPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = consultation.appointmentDate,
                                                color = TextSecondary,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Client: ${consultation.clientName} (${consultation.clientContact})",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )

                                        if (consultation.issueSummary.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Notes: \"${consultation.issueSummary}\"",
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Text(
                                                    text = "Trigger Alert",
                                                    color = PriceTagGreen,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .clickable {
                                                            com.example.util.NotificationUtils.triggerConsultationReminder(
                                                                context,
                                                                consultation.lawyerName,
                                                                consultation.appointmentDate
                                                            )
                                                        }
                                                        .padding(vertical = 4.dp)
                                                        .testTag("trigger_reminder_${consultation.id}")
                                                )

                                                Text(
                                                    text = "Brevo Email",
                                                    color = NeonGreenPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .clickable {
                                                            com.example.util.NotificationUtils.triggerBrevoEmailNotification(
                                                                context = context,
                                                                lawyerName = consultation.lawyerName,
                                                                appointmentTime = consultation.appointmentDate,
                                                                clientName = consultation.clientName,
                                                                clientEmail = if (consultation.clientContact.contains("@")) consultation.clientContact else "client@example.com",
                                                                notes = consultation.issueSummary,
                                                                customBrevoKey = tempBrevoKey
                                                            )
                                                        }
                                                        .padding(vertical = 4.dp)
                                                        .testTag("send_brevo_email_${consultation.id}")
                                                )
                                            }

                                            if (consultation.status == "Pending Approval") {
                                                Text(
                                                    text = "Cancel Booking",
                                                    color = Color(0xFFEF4444),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .clickable {
                                                            onUpdateConsultationStatus(consultation.id, "Cancelled")
                                                        }
                                                        .padding(vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: Saved Lawyers
                        if (savedLawyers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Saved Attorneys",
                                        color = TextSecondary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Tap the bookmark icon on any attorney profile to save them for quick consultation.",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            savedLawyers.forEach { lawyer ->
                                val resId = rememberLawyerImageResource(context, lawyer.avatarResourceName)
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    cornerRadius = 16.dp,
                                    backgroundColor = DarkSurface,
                                    borderColor = DarkCardBorder
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = resId),
                                            contentDescription = lawyer.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                        )

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = lawyer.name,
                                                color = TextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${lawyer.practiceArea} • ${lawyer.location}",
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = StarGold,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${lawyer.rating} (${lawyer.reviewCount}) • ${lawyer.currencySymbol}${lawyer.hourlyRate.toInt()}/hr",
                                                    color = PriceTagGreen,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(
                                            onClick = { onRemoveSavedLawyer(lawyer) },
                                            modifier = Modifier.testTag("remove_saved_${lawyer.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = TextMuted
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                onDismiss()
                                                onBookLawyer(lawyer)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PriceTagGreen,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("book_saved_${lawyer.id}")
                                        ) {
                                            Text("Book", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: Past Advice Sessions (Stored Locally)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Past Legal Queries",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = {
                                        onStartNewSession()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonGreenPrimary,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("modal_start_new_chat")
                                ) {
                                    Text("+ New Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (chatSessions.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No Saved Advice Sessions",
                                            color = TextSecondary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Your legal queries and AI attorney recommendations will be saved locally in Room.",
                                            color = TextMuted,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            } else {
                                val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
                                chatSessions.forEach { session ->
                                    val isCurrent = session.threadId == activeThreadId
                                    GlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clickable {
                                                onSelectSession(session.threadId)
                                                onDismiss()
                                            }
                                            .testTag("session_item_${session.threadId}"),
                                        cornerRadius = 16.dp,
                                        backgroundColor = if (isCurrent) Color(0xFF1E2B20) else DarkSurface,
                                        borderColor = if (isCurrent) NeonGreenPrimary else DarkCardBorder
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ChatBubble,
                                                        contentDescription = null,
                                                        tint = if (isCurrent) NeonGreenPrimary else PriceTagGreen,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = session.title,
                                                        color = TextPrimary,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { onDeleteSession(session.threadId) },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .testTag("delete_session_${session.threadId}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Session",
                                                        tint = TextMuted,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = session.lastMessageText,
                                                color = TextSecondary,
                                                fontSize = 12.sp,
                                                maxLines = 2
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = dateFormat.format(Date(session.timestamp)),
                                                    color = TextMuted,
                                                    fontSize = 11.sp
                                                )

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (session.matchedLawyerIds.isNotEmpty()) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(Color(0xFF1B3828))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "${session.matchedLawyerIds.size} Matched Attorneys",
                                                                color = PriceTagGreen,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }

                                                    Text(
                                                        text = "${session.messageCount} msgs",
                                                        color = TextSecondary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: AI Config & Assistant Settings
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Preferred AI Engine",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = tempProvider == "gemini",
                                    onClick = {
                                        tempProvider = "gemini"
                                        onSaveSettings(tempGeminiKey, tempGroqKey, "gemini", tempBrevoKey)
                                    },
                                    label = { Text("Gemini 1.5 Flash (Default)", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PriceTagGreen,
                                        selectedLabelColor = Color.Black,
                                        containerColor = DarkSurfaceVariant,
                                        labelColor = TextPrimary
                                    ),
                                    modifier = Modifier.testTag("profile_provider_gemini")
                                )

                                FilterChip(
                                    selected = tempProvider == "groq",
                                    onClick = {
                                        tempProvider = "groq"
                                        onSaveSettings(tempGeminiKey, tempGroqKey, "groq", tempBrevoKey)
                                    },
                                    label = { Text("Groq (Llama-3)", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PriceTagGreen,
                                        selectedLabelColor = Color.Black,
                                        containerColor = DarkSurfaceVariant,
                                        labelColor = TextPrimary
                                    ),
                                    modifier = Modifier.testTag("profile_provider_groq")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Assistant Response Language",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableLanguages.take(4).forEach { lang ->
                                    FilterChip(
                                        selected = tempLang == lang,
                                        onClick = {
                                            tempLang = lang
                                            onLanguageChange(lang)
                                        },
                                        label = { Text(lang, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonGreenPrimary,
                                            selectedLabelColor = Color.Black,
                                            containerColor = DarkSurfaceVariant,
                                            labelColor = TextPrimary
                                        )
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableLanguages.drop(4).forEach { lang ->
                                    FilterChip(
                                        selected = tempLang == lang,
                                        onClick = {
                                            tempLang = lang
                                            onLanguageChange(lang)
                                        },
                                        label = { Text(lang, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonGreenPrimary,
                                            selectedLabelColor = Color.Black,
                                            containerColor = DarkSurfaceVariant,
                                            labelColor = TextPrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Custom API Keys (Optional)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = tempGeminiKey,
                                onValueChange = {
                                    tempGeminiKey = it
                                    onSaveSettings(tempGeminiKey, tempGroqKey, tempProvider, tempBrevoKey)
                                },
                                label = { Text("Gemini API Key", color = TextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreenPrimary,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_gemini_key_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = tempGroqKey,
                                onValueChange = {
                                    tempGroqKey = it
                                    onSaveSettings(tempGeminiKey, tempGroqKey, tempProvider, tempBrevoKey)
                                },
                                label = { Text("Groq API Key", color = TextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreenPrimary,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_groq_key_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = tempBrevoKey,
                                onValueChange = {
                                    tempBrevoKey = it
                                    onSaveSettings(tempGeminiKey, tempGroqKey, tempProvider, tempBrevoKey)
                                },
                                label = { Text("Brevo API Key (Email Alerts)", color = TextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreenPrimary,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_brevo_key_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Clear History Button
                            Button(
                                onClick = onClearChat,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2D1E1E),
                                    contentColor = Color(0xFFEF4444)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("clear_chat_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Clear Chat & Matching History")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Privacy Disclaimer
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 12.dp,
                                backgroundColor = DarkSurfaceVariant,
                                borderColor = DarkCardBorder
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = PriceTagGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Rexis AI strictly preserves your legal privacy. Queries are processed anonymously for attorney matching.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
