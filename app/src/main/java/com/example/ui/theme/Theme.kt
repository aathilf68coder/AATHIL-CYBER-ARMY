package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CyberDarkColorScheme = darkColorScheme(
    primary = NeonCrimson,
    onPrimary = GhostWhite,
    primaryContainer = DeepCrimsonContainer,
    onPrimaryContainer = ScarletGlow,
    secondary = BloodRed,
    onSecondary = GhostWhite,
    secondaryContainer = CrimsonSurfaceElevated,
    onSecondaryContainer = GhostWhite,
    tertiary = ScarletGlow,
    onTertiary = PitchObsidian,
    background = PitchObsidian,
    onBackground = GhostWhite,
    surface = TerminalBlack,
    onSurface = GhostWhite,
    surfaceVariant = CrimsonSurface,
    onSurfaceVariant = AshMuted,
    surfaceContainerLowest = PitchObsidian,
    surfaceContainerLow = TerminalBlack,
    surfaceContainer = CrimsonSurface,
    surfaceContainerHigh = CrimsonSurfaceElevated,
    outline = CrimsonBorder,
    outlineVariant = SubtleDivider,
    error = NeonCrimson,
    onError = GhostWhite,
    errorContainer = DeepCrimsonContainer,
    onErrorContainer = ScarletGlow
)

private val CyberShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun AegisCyberTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberDarkColorScheme,
        typography = CyberTypography,
        shapes = CyberShapes,
        content = content
    )
}
