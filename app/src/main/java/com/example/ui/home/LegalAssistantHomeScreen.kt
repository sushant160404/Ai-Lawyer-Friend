package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AudioWaveformInputBar
import com.example.ui.components.BackgroundGradientCanvas
import com.example.ui.components.GlassCard
import com.example.ui.components.rememberLawyerImageResource
import com.example.ui.theme.NeonGreenPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class SuggestedTopic(
    val title: String,
    val prompt: String,
    val iconEmoji: String
)

val suggestedTopicsList = listOf(
    SuggestedTopic("Divorce & custody dispute.", "I need legal help with divorce proceedings and child custody rights.", "⚖️"),
    SuggestedTopic("Family custody matters.", "My ex-spouse is violating child visitation agreements.", "👨‍👩‍👧"),
    SuggestedTopic("Landlord kept my deposit.", "My landlord refused to refund my $2,000 security deposit after I moved out.", "🔑"),
    SuggestedTopic("Workplace harassment.", "I reported workplace harassment a week before I was fired. Show me the best available lawyer.", "💼"),
    SuggestedTopic("Contract & NDA review.", "I need an attorney to review an employment non-compete contract.", "📝"),
    SuggestedTopic("Personal injury claim.", "I was injured in a car accident and the insurance company is denying fault.", "🚗")
)

@Composable
fun LegalAssistantHomeScreen(
    queryText: String,
    onQueryChange: (String) -> Unit,
    onSendQuery: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rexisLogoResId = rememberLawyerImageResource(context, "img_rexis_logo_1786371708060")

    BackgroundGradientCanvas(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onOpenProfile?.invoke() ?: onOpenSettings() },
                    modifier = Modifier.testTag("profile_top_button")
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

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary
                    )
                }
            }

            // Scrollable Hero Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Brand Logo Graphic
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = rexisLogoResId),
                        contentDescription = "Rexis AI Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title: Tell Rexis AI Your Legal Issue
                Text(
                    text = buildAnnotatedString {
                        append("Tell ")
                        withStyle(style = SpanStyle(color = NeonGreenPrimary, fontWeight = FontWeight.Bold)) {
                            append("Rexis AI")
                        }
                        append(" Your Legal Issue")
                    },
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Describe your issue. We'll match you with\nthe right lawyer.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Suggested Topics Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Suggested Topics",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Suggested Topics Horizontal Carousel
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("suggested_topics_carousel")
                ) {
                    items(suggestedTopicsList) { topic ->
                        GlassCard(
                            modifier = Modifier
                                .width(150.dp)
                                .height(120.dp)
                                .clickable { onSendQuery(topic.prompt) }
                                .testTag("topic_card_${topic.title}"),
                            cornerRadius = 20.dp,
                            backgroundColor = Color(0xF0151E18),
                            borderColor = Color(0x284ADE80)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = topic.iconEmoji,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = topic.title,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 17.sp,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Bottom Waveform & Text Input Bar
            AudioWaveformInputBar(
                textValue = queryText,
                onTextChange = onQueryChange,
                onSendClick = { onSendQuery(queryText) }
            )
        }
    }
}
