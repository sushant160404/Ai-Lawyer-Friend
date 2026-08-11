package com.example.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.local.ChatEntity
import com.example.data.local.LawyerEntity
import com.example.data.local.LegalNoteEntity
import com.example.ui.components.AudioWaveformInputBar
import com.example.ui.components.BackgroundGradientCanvas
import com.example.ui.components.GlassCard
import com.example.ui.components.LawyerCardCarousel
import com.example.ui.components.SecureNotesVaultModal
import com.example.ui.components.rememberLawyerImageResource
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreenPrimary
import com.example.ui.theme.PriceTagGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmOrangeAccent

@Composable
fun LegalChatMatchingScreen(
    chatMessages: List<ChatEntity>,
    matchedLawyers: List<LawyerEntity>,
    isAiLoading: Boolean,
    queryText: String,
    onQueryChange: (String) -> Unit,
    onSendQuery: (String) -> Unit,
    onBookLawyer: (LawyerEntity) -> Unit,
    onSaveLawyer: (LawyerEntity) -> Unit,
    onLawyerClick: (LawyerEntity) -> Unit,
    onClearChat: () -> Unit,
    onStartNewSession: () -> Unit = {},
    attachedNote: LegalNoteEntity? = null,
    legalNotes: List<LegalNoteEntity> = emptyList(),
    onAttachNote: (LegalNoteEntity?) -> Unit = {},
    onSaveNote: (title: String, content: String, category: String, id: Long) -> Unit = { _, _, _, _ -> },
    onDeleteNote: (Long) -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rexisLogoResId = rememberLawyerImageResource(context, "img_rexis_logo_1786371708060")
    val userAvatarResId = rememberLawyerImageResource(context, "img_lawyer_sarah_1786371743752")

    val listState = rememberLazyListState()
    var isVaultOpen by remember { mutableStateOf(false) }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    BackgroundGradientCanvas(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onOpenProfile?.invoke() ?: onOpenSettings() },
                    modifier = Modifier.testTag("chat_profile_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = NeonGreenPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Legal Assistant",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row {
                    IconButton(
                        onClick = { isVaultOpen = true },
                        modifier = Modifier.testTag("open_notes_vault_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Notes Vault",
                            tint = PriceTagGreen
                        )
                    }
                    IconButton(
                        onClick = onStartNewSession,
                        modifier = Modifier.testTag("start_new_session_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Session",
                            tint = NeonGreenPrimary
                        )
                    }
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear Current Session",
                            tint = TextMuted
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("chat_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                }
            }

            // Main Chat Log Area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(chatMessages, key = { it.id }) { message ->
                    if (message.senderRole == "user") {
                        // User Query Bubble (Right aligned)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.Top
                        ) {
                            GlassCard(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .testTag("user_message_${message.id}"),
                                cornerRadius = 20.dp,
                                backgroundColor = Color(0xE6252C24),
                                borderColor = Color(0x384ADE80)
                            ) {
                                Text(
                                    text = message.text,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = userAvatarResId),
                                    contentDescription = "User",
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        // AI Assistant Response Bubble (Left aligned)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B2B1F))
                                ) {
                                    Image(
                                        painter = painterResource(id = rexisLogoResId),
                                        contentDescription = "Rexis AI",
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                GlassCard(
                                    modifier = Modifier
                                        .widthIn(max = 290.dp)
                                        .testTag("assistant_message_${message.id}"),
                                    cornerRadius = 20.dp,
                                    backgroundColor = Color(0xF2121A15),
                                    borderColor = Color(0x2BFFFFFF)
                                ) {
                                    Text(
                                        text = message.text,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }

                            // Show Matched Lawyer Cards Carousel if this is the assistant message
                            Spacer(modifier = Modifier.height(16.dp))
                            LawyerCardCarousel(
                                lawyers = matchedLawyers,
                                onBookClick = onBookLawyer,
                                onSaveClick = onSaveLawyer,
                                onLawyerClick = onLawyerClick
                            )
                        }
                    }
                }

                // Loading Indicator Item
                if (isAiLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = NeonGreenPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Rexis AI is analyzing your legal issue and matching top lawyers...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Attached Note Banner
            if (attachedNote != null) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("attached_note_banner"),
                    cornerRadius = 16.dp,
                    backgroundColor = Color(0xFF1E2B20),
                    borderColor = NeonGreenPrimary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = NeonGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Attached: ${attachedNote.title}",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${attachedNote.category} • AES Local Vault",
                                    color = PriceTagGreen,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { onAttachNote(null) },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("remove_attached_note")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove attachment",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Waveform Bar
            AudioWaveformInputBar(
                textValue = queryText,
                onTextChange = onQueryChange,
                onSendClick = { onSendQuery(queryText) },
                onOpenVaultClick = { isVaultOpen = true },
                placeholderText = if (attachedNote != null) "Ask AI assistant about your attached summary..." else "What's next...."
            )
        }

        // Secure Vault Modal Dialog
        SecureNotesVaultModal(
            isOpen = isVaultOpen,
            notes = legalNotes,
            currentlyAttachedNoteId = attachedNote?.id,
            onDismiss = { isVaultOpen = false },
            onAttachNote = { note ->
                onAttachNote(note)
                isVaultOpen = false
            },
            onSaveNote = { title, content, category, id ->
                onSaveNote(title, content, category, id)
            },
            onDeleteNote = { id ->
                onDeleteNote(id)
            }
        )
    }
}
