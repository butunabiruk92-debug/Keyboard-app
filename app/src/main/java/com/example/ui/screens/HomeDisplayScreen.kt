package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.model.*
import com.example.ui.ConsoleColors
import com.example.viewmodel.ConsoleDisplayScreen

@Composable
fun HomeDisplayScreen(
    voiceRight1: Voice,
    voiceRight2: Voice,
    voiceLeft: Voice,
    isSplit: Boolean,
    isLayer: Boolean,
    splitPoint: Int,
    currentStyle: Style,
    currentSection: StyleSection,
    detectedChord: ChordInfo,
    masterVolume: Float,
    balance: Float,
    onNavigate: (ConsoleDisplayScreen) -> Unit,
    onSectionSelect: (StyleSection) -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    onBalanceChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_display_screen"),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // LEFT COLUMN: Style & Arranger Section Status
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(ConsoleColors.ScreenCard)
                .border(1.dp, ConsoleColors.ScreenCardBorder, RoundedCornerShape(6.dp))
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STYLE OVERVIEW",
                    color = ConsoleColors.TextSecondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentStyle.category.displayName.uppercase(),
                    color = ConsoleColors.LedCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = currentStyle.name,
                color = ConsoleColors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Variations Grid
            Text(
                text = "ARRANGER SECTIONS",
                color = ConsoleColors.TextSecondary,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf(StyleSection.MAIN_A, StyleSection.MAIN_B, StyleSection.MAIN_C, StyleSection.MAIN_D).forEach { sec ->
                    val isActive = currentSection == sec
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isActive) ConsoleColors.LedBlue else Color(0xFF141924))
                            .border(1.dp, if (isActive) ConsoleColors.LedCyan else ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
                            .clickable { onSectionSelect(sec) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sec.displayName.replace("Main ", ""),
                            color = if (isActive) Color.White else ConsoleColors.TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf(StyleSection.INTRO, StyleSection.FILL_IN, StyleSection.BREAK, StyleSection.ENDING).forEach { sec ->
                    val isActive = currentSection == sec
                    val col = when (sec) {
                        StyleSection.FILL_IN -> ConsoleColors.LedOrange
                        StyleSection.BREAK -> ConsoleColors.LedRed
                        StyleSection.INTRO -> ConsoleColors.LedGreen
                        StyleSection.ENDING -> Color(0xFF9C27B0)
                        else -> ConsoleColors.LedBlue
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isActive) col else Color(0xFF141924))
                            .border(1.dp, if (isActive) Color.White else ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
                            .clickable { onSectionSelect(sec) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sec.displayName.uppercase(),
                            color = if (isActive) Color.White else ConsoleColors.TextSecondary,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Detected Chord & Voicing Notes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF101520))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("HARMONY", color = ConsoleColors.TextSecondary, fontSize = 7.sp)
                    Text(
                        detectedChord.displayName,
                        color = ConsoleColors.LedCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                val notesStr = detectedChord.getVoicingNotes(3).joinToString(" - ") {
                    ChordInfo.NOTE_NAMES[it % 12]
                }
                Text(
                    text = notesStr,
                    color = ConsoleColors.TextSecondary,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // CENTER COLUMN: Keyboard Parts (Left, Right 1, Right 2)
        Column(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Right 1 Card
            VoicePartCard(
                partName = "RIGHT 1",
                voice = voiceRight1,
                isActive = true,
                badge = "LEAD",
                badgeColor = ConsoleColors.LedCyan,
                onClick = { onNavigate(ConsoleDisplayScreen.VOICE) }
            )

            // Right 2 Card (Layer)
            VoicePartCard(
                partName = "RIGHT 2",
                voice = voiceRight2,
                isActive = isLayer,
                badge = if (isLayer) "LAYER ON" else "LAYER OFF",
                badgeColor = if (isLayer) ConsoleColors.LedOrange else ConsoleColors.TextDisabled,
                onClick = { onNavigate(ConsoleDisplayScreen.VOICE) }
            )

            // Left Card (Split)
            val splitNoteName = ChordInfo.NOTE_NAMES[splitPoint % 12]
            val splitOctave = (splitPoint / 12) - 1
            VoicePartCard(
                partName = "LEFT",
                voice = voiceLeft,
                isActive = isSplit,
                badge = if (isSplit) "SPLIT ($splitNoteName$splitOctave)" else "SPLIT OFF",
                badgeColor = if (isSplit) ConsoleColors.SplitMarker else ConsoleColors.TextDisabled,
                onClick = { onNavigate(ConsoleDisplayScreen.VOICE) }
            )
        }

        // RIGHT COLUMN: Quick Performance Macro Sliders
        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(ConsoleColors.ScreenCard)
                .border(1.dp, ConsoleColors.ScreenCardBorder, RoundedCornerShape(6.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MACRO CONTROLS",
                color = ConsoleColors.TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )

            // Master Volume Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("MASTER", color = ConsoleColors.TextSecondary, fontSize = 7.5.sp)
                    Text("${(masterVolume * 100).toInt()}%", color = ConsoleColors.LedCyan, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = masterVolume,
                    onValueChange = onMasterVolumeChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = ConsoleColors.LedCyan,
                        activeTrackColor = ConsoleColors.LedCyan,
                        inactiveTrackColor = ConsoleColors.ChassisBorder
                    )
                )
            }

            // Balance Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("STYLE / KEY", color = ConsoleColors.TextSecondary, fontSize = 7.5.sp)
                    val balVal = ((balance - 0.5f) * 200).toInt()
                    val balStr = if (balVal < 0) "STY ${-balVal}%" else if (balVal > 0) "KEY ${balVal}%" else "CENTER"
                    Text(balStr, color = ConsoleColors.LedOrange, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = balance,
                    onValueChange = onBalanceChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = ConsoleColors.LedOrange,
                        activeTrackColor = ConsoleColors.LedOrange,
                        inactiveTrackColor = ConsoleColors.ChassisBorder
                    )
                )
            }

            // Quick Jump to Mixer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1B2334))
                    .border(0.5.dp, ConsoleColors.LedCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .clickable { onNavigate(ConsoleDisplayScreen.MIXER) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OPEN FULL MIXER ▶",
                    color = ConsoleColors.LedCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun VoicePartCard(
    partName: String,
    voice: Voice,
    isActive: Boolean,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ConsoleColors.ScreenCard)
            .border(
                1.dp,
                if (isActive) badgeColor.copy(alpha = 0.6f) else ConsoleColors.ScreenCardBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = partName,
                        color = ConsoleColors.TextSecondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = voice.category.displayName,
                        color = ConsoleColors.TextDisabled,
                        fontSize = 7.5.sp
                    )
                }
                Text(
                    text = voice.name,
                    color = if (isActive) ConsoleColors.TextPrimary else ConsoleColors.TextDisabled,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(badgeColor.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    color = badgeColor,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
