package com.example.ui.lawyer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConsultationEntity
import com.example.data.local.LawyerEntity
import com.example.ui.components.BackgroundGradientCanvas
import com.example.ui.components.GlassCard
import com.example.ui.components.rememberLawyerImageResource
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreenPrimary
import com.example.ui.theme.PriceTagGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmOrangeAccent

@Composable
fun LawyerDashboardScreen(
    lawyerProfile: LawyerEntity?,
    consultations: List<ConsultationEntity>,
    onUpdateConsultationStatus: (Long, String) -> Unit,
    onSwitchToClient: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val avatarResId = rememberLawyerImageResource(
        context,
        lawyerProfile?.avatarResourceName ?: "img_lawyer_michale_1786371723292"
    )

    BackgroundGradientCanvas(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lawyer Portal",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rexis AI Lead Matching Engine",
                        color = PriceTagGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row {
                    IconButton(
                        onClick = { onOpenProfile?.invoke() ?: onOpenSettings() },
                        modifier = Modifier.testTag("lawyer_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = NeonGreenPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onSwitchToClient,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreenPrimary),
                        modifier = Modifier.testTag("switch_to_client_button")
                    ) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Client View")
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("lawyer_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lawyer Profile Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lawyer_profile_card"),
                cornerRadius = 24.dp,
                backgroundColor = Color(0xF0141D17),
                borderColor = Color(0x3D4ADE80)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = avatarResId),
                        contentDescription = "Lawyer Profile Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lawyerProfile?.name ?: "Michale Chan, Esq.",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = lawyerProfile?.practiceArea ?: "Employment & Labor Law",
                            color = PriceTagGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rate: ${lawyerProfile?.currencySymbol ?: "€"}${lawyerProfile?.hourlyRate?.toInt() ?: 50}/hr  •  ${lawyerProfile?.location ?: "Los Angeles, CA"}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section Title: Incoming Client Leads & Appointments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Client Requests & AI Matches",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${consultations.size} Total",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (consultations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No pending consultation requests yet.",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Switch to Client mode to submit a test legal query!",
                            color = PriceTagGreen,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(consultations, key = { it.id }) { consultation ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("consultation_item_${consultation.id}"),
                            cornerRadius = 16.dp,
                            backgroundColor = DarkSurfaceVariant,
                            borderColor = Color(0x22FFFFFF)
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
                                        text = consultation.clientName,
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (consultation.status == "Confirmed") PriceTagGreen.copy(alpha = 0.2f)
                                                else WarmOrangeAccent.copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = consultation.status,
                                            color = if (consultation.status == "Confirmed") PriceTagGreen else WarmOrangeAccent,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Contact: ${consultation.clientContact}",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Notes: ${consultation.issueSummary}",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (consultation.status == "Pending") {
                                        Button(
                                            onClick = { onUpdateConsultationStatus(consultation.id, "Confirmed") },
                                            colors = ButtonDefaults.buttonColors(containerColor = PriceTagGreen),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Accept & Confirm", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PriceTagGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Confirmed for ${consultation.appointmentDate}", color = PriceTagGreen, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
