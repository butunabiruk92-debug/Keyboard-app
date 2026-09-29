package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChordInfo
import com.example.model.SoundEffectType
import com.example.ui.ConsoleColors
import com.example.ui.components.LocalSoundFeedback
import com.example.viewmodel.ConsoleDisplayScreen

@Composable
fun CenterTouchscreen(
    currentScreen: ConsoleDisplayScreen,
    onNavigate: (ConsoleDisplayScreen) -> Unit,
    styleName: String,
    tempo: Int,
    timeSignature: String,
    measure: Int,
    beat: Int,
    chord: ChordInfo,
    transpose: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val soundFeedback = LocalSoundFeedback.current
    // Large beveled glass touchscreen housing
    Box(
        modifier = modifier
            .testTag("center_touchscreen")
            .clip(RoundedCornerShape(8.dp))
            .background(ConsoleColors.ScreenBezel)
            .border(2.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(8.dp))
            .padding(4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(ConsoleColors.ScreenBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP STATUS / HEADER BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Color(0xFF0F1522))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Style and Section badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "STYLE: ",
                        color = ConsoleColors.TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = styleName,
                        color = ConsoleColors.LedCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tempo and Time Sig
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$tempo BPM",
                        color = ConsoleColors.LedOrange,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeSignature,
                        color = ConsoleColors.TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Measure : Beat counter
                val mStr = String.format("%03d", measure)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BAR ",
                        color = ConsoleColors.TextSecondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$mStr : $beat",
                        color = ConsoleColors.LedGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Detected Chord Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E2638))
                        .border(1.dp, ConsoleColors.LedCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = chord.displayName.ifEmpty { "C" },
                        color = ConsoleColors.LedCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Transpose indicator
                Text(
                    text = if (transpose >= 0) "KEY: +$transpose" else "KEY: $transpose",
                    color = if (transpose != 0) ConsoleColors.LedOrange else ConsoleColors.TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2. NAVIGATION TABS ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .background(Color(0xFF141A26))
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ConsoleDisplayScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (isSelected) ConsoleColors.ScreenPanel else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) ConsoleColors.LedCyan.copy(alpha = 0.6f) else Color.Transparent,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                            .clickable {
                                soundFeedback(SoundEffectType.BUTTON_CLICK)
                                onNavigate(screen)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = screen.title.uppercase(),
                            color = if (isSelected) ConsoleColors.LedCyan else ConsoleColors.TextSecondary,
                            fontSize = 8.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // 3. MAIN TOUCHSCREEN VIEWPORT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ConsoleColors.ScreenPanel)
                    .padding(6.dp)
            ) {
                content()
            }
        }
    }
}
