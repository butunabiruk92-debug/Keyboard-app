package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.ConsoleColors

private val ConsoleDarkColorScheme = darkColorScheme(
    primary = ConsoleColors.LedCyan,
    onPrimary = Color.Black,
    secondary = ConsoleColors.LedOrange,
    onSecondary = Color.Black,
    tertiary = ConsoleColors.LedGreen,
    onTertiary = Color.Black,
    background = ConsoleColors.ChassisDark,
    onBackground = ConsoleColors.TextPrimary,
    surface = ConsoleColors.ChassisPanel,
    onSurface = ConsoleColors.TextPrimary,
    surfaceVariant = ConsoleColors.ChassisPanelLight,
    onSurfaceVariant = ConsoleColors.TextSecondary,
    outline = ConsoleColors.ChassisBorder
)

@Composable
fun ArrangerProTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ConsoleDarkColorScheme,
        typography = Typography,
        content = content
    )
}
