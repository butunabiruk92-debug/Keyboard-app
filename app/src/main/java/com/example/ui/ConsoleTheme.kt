package com.example.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object ConsoleColors {
    // Hardware chassis
    val ChassisDark = Color(0xFF0F1116)
    val ChassisPanel = Color(0xFF161922)
    val ChassisPanelLight = Color(0xFF1E2330)
    val ChassisBorder = Color(0xFF2E3445)
    val ChassisBevelHighlight = Color(0xFF3B4358)
    val MetallicSilver = Color(0xFF8A93A6)
    val AluminumTrim = Color(0xFF5A6273)

    // Touchscreen Display
    val ScreenBezel = Color(0xFF0A0C10)
    val ScreenBackground = Color(0xFF0D121B)
    val ScreenPanel = Color(0xFF151C2A)
    val ScreenCard = Color(0xFF1C2436)
    val ScreenCardBorder = Color(0xFF2B3752)

    // LED Status Colors
    val LedCyan = Color(0xFF00E5FF)
    val LedCyanGlow = Color(0x6600E5FF)
    val LedOrange = Color(0xFFFF9100)
    val LedOrangeGlow = Color(0x66FF9100)
    val LedRed = Color(0xFFFF1744)
    val LedRedGlow = Color(0x66FF1744)
    val LedGreen = Color(0xFF00E676)
    val LedGreenGlow = Color(0x6600E676)
    val LedBlue = Color(0xFF2979FF)
    val LedOff = Color(0xFF1C2230)

    // Piano Keyboard
    val KeyWhite = Color(0xFFF7F8FA)
    val KeyWhitePressed = Color(0xFFD6DBE5)
    val KeyWhiteBorder = Color(0xFFB0B7C6)
    val KeyBlack = Color(0xFF15171C)
    val KeyBlackPressed = Color(0xFF282C35)
    val KeyBlackBorder = Color(0xFF0A0B0E)
    val SplitMarker = Color(0xFFFF9100)
    val NotePressGlow = Color(0x9900E5FF)

    // Text & Symbols
    val TextPrimary = Color(0xFFF0F4FC)
    val TextSecondary = Color(0xFF8E9BAE)
    val TextDisabled = Color(0xFF485264)
    val TextAccent = Color(0xFF00E5FF)

    // Hardware metal gradients
    val MetalBrushGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF222734),
            Color(0xFF181B24),
            Color(0xFF13151D)
        )
    )

    val ScreenGlowGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF121A28),
            Color(0xFF0B1019)
        )
    )

    val KnobMetalGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFF4A546A),
            Color(0xFF252A36),
            Color(0xFF12151C)
        )
    )
}
