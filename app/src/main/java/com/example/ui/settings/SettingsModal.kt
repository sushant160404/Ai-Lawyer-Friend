package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreenPrimary
import com.example.ui.theme.PriceTagGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModal(
    geminiKey: String,
    groqKey: String,
    preferredProvider: String,
    currentRole: UserRole?,
    selectedLanguage: String = "English",
    availableLanguages: List<String> = listOf("English", "Español", "Français", "Deutsch", "हिन्दी", "中文", "Auto-Detect"),
    onSaveSettings: (geminiKey: String, groqKey: String, provider: String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    var tempGeminiKey by remember { mutableStateOf(geminiKey) }
    var tempGroqKey by remember { mutableStateOf(groqKey) }
    var tempProvider by remember { mutableStateOf(preferredProvider) }
    var tempLang by remember { mutableStateOf(selectedLanguage) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = null
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("settings_modal")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = NeonGreenPrimary
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "AI Legal Settings & Model Config",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_settings_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Provider Selector
            Text(
                text = "Primary AI Engine Provider",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = tempProvider == "gemini",
                    onClick = { tempProvider = "gemini" },
                    label = { Text("Gemini API (gemini-2.5-flash)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PriceTagGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextPrimary
                    ),
                    modifier = Modifier.testTag("select_gemini_provider")
                )

                FilterChip(
                    selected = tempProvider == "groq",
                    onClick = { tempProvider = "groq" },
                    label = { Text("Groq API (llama-3.3-70b)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PriceTagGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextPrimary
                    ),
                    modifier = Modifier.testTag("select_groq_provider")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Gemini Key
            OutlinedTextField(
                value = tempGeminiKey,
                onValueChange = { tempGeminiKey = it },
                label = { Text("Gemini API Key (Optional Override)", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreenPrimary,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_key_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Groq Key
            OutlinedTextField(
                value = tempGroqKey,
                onValueChange = { tempGroqKey = it },
                label = { Text("Groq API Key (Optional Override)", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreenPrimary,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("groq_key_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Language Selection Section
            Text(
                text = "Assistant Response Language",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableLanguages.take(4).forEach { lang ->
                    FilterChip(
                        selected = tempLang == lang,
                        onClick = {
                            tempLang = lang
                            onLanguageChange(lang)
                        },
                        label = { Text(lang, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PriceTagGreen,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextPrimary
                        ),
                        modifier = Modifier.testTag("lang_chip_$lang")
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableLanguages.drop(4).forEach { lang ->
                    FilterChip(
                        selected = tempLang == lang,
                        onClick = {
                            tempLang = lang
                            onLanguageChange(lang)
                        },
                        label = { Text(lang, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PriceTagGreen,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextPrimary
                        ),
                        modifier = Modifier.testTag("lang_chip_$lang")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Role Switch Section
            Text(
                text = "Account Role",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = currentRole == UserRole.CLIENT,
                    onClick = { onRoleChange(UserRole.CLIENT) },
                    label = { Text("Client View") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PriceTagGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextPrimary
                    ),
                    modifier = Modifier.testTag("switch_role_client")
                )

                FilterChip(
                    selected = currentRole == UserRole.LAWYER,
                    onClick = { onRoleChange(UserRole.LAWYER) },
                    label = { Text("Lawyer Portal") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PriceTagGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextPrimary
                    ),
                    modifier = Modifier.testTag("switch_role_lawyer")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save & Apply Button
            Button(
                onClick = {
                    onSaveSettings(tempGeminiKey, tempGroqKey, tempProvider)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreenPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button")
            ) {
                Text("Save Configuration", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
