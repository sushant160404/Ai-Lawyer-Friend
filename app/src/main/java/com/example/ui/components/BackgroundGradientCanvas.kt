package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DarkBackground

@Composable
fun BackgroundGradientCanvas(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-center green glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3832D74B),
                        Color(0x1A1B4D2E),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.25f),
                    radius = width * 0.75f
                ),
                center = Offset(width * 0.5f, height * 0.25f),
                radius = width * 0.75f
            )

            // Middle right warm orange glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x28EA580C),
                        Color(0x103E1C0A),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.6f),
                    radius = width * 0.65f
                ),
                center = Offset(width * 0.85f, height * 0.6f),
                radius = width * 0.65f
            )

            // Bottom left emerald ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x2E22C55E),
                        Color(0x0F0F291B),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * 0.85f),
                    radius = width * 0.7f
                ),
                center = Offset(width * 0.15f, height * 0.85f),
                radius = width * 0.7f
            )
        }

        content()
    }
}
