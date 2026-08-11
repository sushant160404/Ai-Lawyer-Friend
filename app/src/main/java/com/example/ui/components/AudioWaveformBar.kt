package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrightOrangeAccent
import com.example.ui.theme.NeonGreenPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarmOrangeAccent
import kotlin.random.Random

@Composable
fun AudioWaveformInputBar(
    textValue: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onOpenVaultClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    placeholderText: String = "Describe your legal issue..."
) {
    var isRecording by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        cornerRadius = 28.dp,
        backgroundColor = Color(0xF2121A15),
        borderColor = Color(0x384ADE80)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Attachment Plus / Vault button
            IconButton(
                onClick = { onOpenVaultClick?.invoke() },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("add_attachment_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Attach Secure Note",
                    tint = NeonGreenPrimary
                )
            }

            // Mic toggle button
            IconButton(
                onClick = {
                    isRecording = !isRecording
                    if (isRecording && textValue.isEmpty()) {
                        onTextChange("I reported workplace harassment a week before I was fired. Show me the best available lawyer.")
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("mic_toggle_button")
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice dictation",
                    tint = if (isRecording) BrightOrangeAccent else TextMuted
                )
            }

            // Input field & waveform
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (textValue.isEmpty() && !isRecording) {
                    Text(
                        text = placeholderText,
                        style = TextStyle(
                            color = TextMuted,
                            fontSize = 15.sp
                        )
                    )
                }

                if (isRecording) {
                    AnimatedWaveformIndicator()
                } else {
                    BasicTextField(
                        value = textValue,
                        onValueChange = onTextChange,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 15.sp
                        ),
                        cursorBrush = SolidColor(NeonGreenPrimary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("legal_issue_input")
                    )
                }
            }

            // Glowing Send Button with Gradient
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                WarmOrangeAccent,
                                BrightOrangeAccent,
                                NeonGreenPrimary
                            )
                        )
                    )
                    .clickable {
                        onSendClick()
                    }
                    .testTag("send_legal_query_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Legal Query",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedWaveformIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 20
        for (i in 0 until barCount) {
            val animHeight = remember { Animatable(10f) }
            LaunchedEffect(Unit) {
                animHeight.animateTo(
                    targetValue = Random.nextInt(8, 22).toFloat(),
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = Random.nextInt(300, 700),
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(animHeight.value.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (i % 2 == 0) NeonGreenPrimary else WarmOrangeAccent
                    )
            )
        }
    }
}
