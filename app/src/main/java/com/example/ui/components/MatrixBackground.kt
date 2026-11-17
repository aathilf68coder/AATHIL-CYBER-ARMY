package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BloodRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.PitchObsidian
import com.example.ui.theme.ScarletGlow
import kotlin.math.abs

private data class MatrixColumnSeed(
    val speedMultiplier: Float,
    val phaseOffset: Float,
    val trailLength: Int,
    val charOffset: Int
)

@Composable
fun MatrixBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val textMeasurer = rememberTextMeasurer(cacheSize = 32)
    val glyphs = remember {
        listOf("0", "1", "A", "F", "7", "9", "X", "E", "C", "4", "#", "$", "8", "D", "B", "2")
    }
    // Pre-measure all glyphs once so the Draw phase performs zero per-frame text layout
    val measuredGlyphs: List<TextLayoutResult> = remember(textMeasurer, glyphs) {
        val baseStyle = TextStyle(
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
        )
        glyphs.map { glyph ->
            textMeasurer.measure(text = glyph, style = baseStyle)
        }
    }

    val columnCount = 16
    val columnSeeds = remember {
        List(columnCount) { idx ->
            MatrixColumnSeed(
                speedMultiplier = 0.55f + ((idx * 37) % 10) * 0.08f,
                phaseOffset = ((idx * 73) % 100) / 100f,
                trailLength = 6 + (idx % 5),
                charOffset = (idx * 5) % glyphs.size
            )
        }
    }

    val elapsedSecState = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var startNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (startNanos == 0L) startNanos = nanos
                elapsedSecState.floatValue = (nanos - startNanos) / 1_000_000_000f
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val elapsedSec = elapsedSecState.floatValue

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    PitchObsidian,
                    Color(0xFF0A0306),
                    PitchObsidian
                )
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    BloodRed.copy(alpha = 0.14f * intensity),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.22f),
                radius = size.maxDimension * 0.65f
            ),
            radius = size.maxDimension * 0.65f,
            center = Offset(size.width * 0.5f, size.height * 0.22f)
        )

        val colWidth = size.width / columnCount.toFloat()
        val cellHeight = 32f
        val rowsInScreen = (size.height / cellHeight).toInt() + 2

        for (col in 0 until columnCount) {
            val seed = columnSeeds[col]
            val cycleRows = rowsInScreen + seed.trailLength + 6
            val headRowFloat = ((elapsedSec * 6.0f * seed.speedMultiplier) + seed.phaseOffset * cycleRows) % cycleRows
            val headRow = headRowFloat.toInt()
            val xPos = col * colWidth + colWidth * 0.25f

            for (step in 0 until seed.trailLength) {
                val row = headRow - step
                if (row in 0 until rowsInScreen) {
                    val yPos = row * cellHeight
                    if (yPos >= 0f && yPos <= size.height - cellHeight) {
                        val fade = (1f - step.toFloat() / seed.trailLength.toFloat()).coerceIn(0f, 1f)
                        val glyphIndex = abs(row * 3 + col * 7 + (elapsedSec * 2f).toInt() + seed.charOffset) % measuredGlyphs.size
                        val glyphColor = when (step) {
                            0 -> ScarletGlow.copy(alpha = (0.55f * intensity).coerceIn(0.05f, 0.85f))
                            1 -> NeonCrimson.copy(alpha = (0.38f * fade * intensity).coerceIn(0.04f, 0.65f))
                            else -> BloodRed.copy(alpha = (0.26f * fade * intensity).coerceIn(0.02f, 0.45f))
                        }
                        drawText(
                            textLayoutResult = measuredGlyphs[glyphIndex],
                            color = glyphColor,
                            topLeft = Offset(xPos, yPos)
                        )
                    }
                }
            }
        }

        val gridSpacing = 48f
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = NeonCrimson.copy(alpha = 0.025f * intensity),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += gridSpacing
        }
    }
}
