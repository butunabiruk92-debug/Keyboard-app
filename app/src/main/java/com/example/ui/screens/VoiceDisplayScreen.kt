package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardPart
import com.example.model.Voice
import com.example.model.VoiceCategory
import com.example.model.VoicePresets
import com.example.ui.ConsoleColors

@Composable
fun VoiceDisplayScreen(
    currentVoiceRight1: Voice,
    currentVoiceRight2: Voice,
    currentVoiceLeft: Voice,
    onSelectVoiceRight1: (Voice) -> Unit,
    onSelectVoiceRight2: (Voice) -> Unit,
    onSelectVoiceLeft: (Voice) -> Unit
) {
    var selectedPart by remember { mutableStateOf(KeyboardPart.RIGHT1) }
    var selectedCategory by remember { mutableStateOf(VoiceCategory.PIANO) }

    val activeVoiceForPart = when (selectedPart) {
        KeyboardPart.RIGHT1 -> currentVoiceRight1
        KeyboardPart.RIGHT2 -> currentVoiceRight2
        KeyboardPart.LEFT -> currentVoiceLeft
        else -> currentVoiceRight1
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .testTag("voice_display_screen"),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // COLUMN 1: Target Part Selector (RIGHT 1, RIGHT 2, LEFT)
        Column(
            modifier = Modifier
                .width(85.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "ASSIGN TO PART",
                color = ConsoleColors.TextSecondary,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold
            )

            listOf(
                Triple(KeyboardPart.RIGHT1, "RIGHT 1", currentVoiceRight1.name),
                Triple(KeyboardPart.RIGHT2, "RIGHT 2", currentVoiceRight2.name),
                Triple(KeyboardPart.LEFT, "LEFT", currentVoiceLeft.name)
            ).forEach { (part, label, activeName) ->
                val isSelected = selectedPart == part
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) ConsoleColors.LedCyan.copy(alpha = 0.2f) else ConsoleColors.ScreenCard)
                        .border(
                            1.dp,
                            if (isSelected) ConsoleColors.LedCyan else ConsoleColors.ScreenCardBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedPart = part }
                        .padding(4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = label,
                            color = if (isSelected) ConsoleColors.LedCyan else ConsoleColors.TextPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeName,
                            color = ConsoleColors.TextSecondary,
                            fontSize = 7.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // COLUMN 2: Voice Categories (PIANO, E.PIANO, ORGAN, STRINGS, BRASS, WOODWIND, PAD, SYNTH, BASS, DRUMS)
        Column(
            modifier = Modifier
                .width(95.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "CATEGORY",
                color = ConsoleColors.TextSecondary,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold
            )

            VoiceCategory.values().forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) ConsoleColors.LedBlue else Color(0xFF141924))
                        .border(
                            0.5.dp,
                            if (isSelected) ConsoleColors.LedCyan else ConsoleColors.ChassisBorder,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = cat.displayName,
                        color = if (isSelected) Color.White else ConsoleColors.TextSecondary,
                        fontSize = 8.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // COLUMN 3: Voice Grid for Selected Category
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(ConsoleColors.ScreenCard)
                .border(1.dp, ConsoleColors.ScreenCardBorder, RoundedCornerShape(6.dp))
                .padding(6.dp)
        ) {
            Text(
                text = "${selectedCategory.displayName.uppercase()} VOICES",
                color = ConsoleColors.TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            val categoryVoices = VoicePresets.ALL_VOICES.filter { it.category == selectedCategory }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(categoryVoices) { voice ->
                    val isCurrent = activeVoiceForPart.id == voice.id
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCurrent) Color(0xFF1C3448) else Color(0xFF131822))
                            .border(
                                1.dp,
                                if (isCurrent) ConsoleColors.LedCyan else ConsoleColors.ChassisBorder,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                when (selectedPart) {
                                    KeyboardPart.RIGHT1 -> onSelectVoiceRight1(voice)
                                    KeyboardPart.RIGHT2 -> onSelectVoiceRight2(voice)
                                    KeyboardPart.LEFT -> onSelectVoiceLeft(voice)
                                    else -> onSelectVoiceRight1(voice)
                                }
                            }
                            .padding(6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = voice.name,
                                    color = if (isCurrent) ConsoleColors.LedCyan else ConsoleColors.TextPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                if (isCurrent) {
                                    Text("✓ ACTIVE", color = ConsoleColors.LedCyan, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Wave: ${voice.waveform.name} • Att: ${voice.attackMs.toInt()}ms",
                                color = ConsoleColors.TextDisabled,
                                fontSize = 7.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
