package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.ConsoleColors

@Composable
fun SettingsDisplayScreen(
    keyboardTotalKeys: Int,
    showNoteLabels: Boolean,
    chordDetectMode: String,
    splitPoint: Int,
    transpose: Int,
    octaveShift: Int,
    onSetTotalKeys: (Int) -> Unit,
    onToggleNoteLabels: () -> Unit,
    onSetChordMode: (String) -> Unit,
    onSetSplitPoint: (Int) -> Unit,
    onShiftTranspose: (Int) -> Unit,
    onShiftOctave: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_display_screen"),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // COLUMN 1: KEYBOARD CONFIGURATION
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(ConsoleColors.ScreenCard)
                .border(1.dp, ConsoleColors.ScreenCardBorder, RoundedCornerShape(6.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "KEYBOARD CONFIGURATION",
                color = ConsoleColors.TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )

            // Keys Range Selector
            Column {
                Text("KEYBOARD RANGE", color = ConsoleColors.TextSecondary, fontSize = 7.5.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    listOf(25, 37, 49, 61, 76, 88).forEach { keys ->
                        val isSelected = keyboardTotalKeys == keys
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) ConsoleColors.LedCyan else Color(0xFF141924))
                                .clickable { onSetTotalKeys(keys) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$keys KEYS",
                                color = if (isSelected) Color.Black else ConsoleColors.TextPrimary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Note Labels Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SHOW NOTE LABELS", color = ConsoleColors.TextPrimary, fontSize = 8.5.sp)
                Switch(
                    checked = showNoteLabels,
                    onCheckedChange = { onToggleNoteLabels() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ConsoleColors.LedCyan,
                        checkedTrackColor = ConsoleColors.LedCyan.copy(alpha = 0.5f)
                    )
                )
            }

            // Split Point
            Column {
                val splitNote = ChordInfo.NOTE_NAMES[splitPoint % 12]
                val splitOct = (splitPoint / 12) - 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SPLIT POINT", color = ConsoleColors.TextSecondary, fontSize = 7.5.sp)
                    Text("$splitNote$splitOct (MIDI $splitPoint)", color = ConsoleColors.SplitMarker, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(48 to "C2", 53 to "F2", 60 to "C3", 65 to "F3", 72 to "C4").forEach { (midi, label) ->
                        val isSel = splitPoint == midi
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSel) ConsoleColors.SplitMarker else Color(0xFF141924))
                                .clickable { onSetSplitPoint(midi) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isSel) Color.Black else ConsoleColors.TextPrimary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // COLUMN 2: CHORD DETECTION & TUNING
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(ConsoleColors.ScreenCard)
                .border(1.dp, ConsoleColors.ScreenCardBorder, RoundedCornerShape(6.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CHORD DETECTION & TUNING",
                color = ConsoleColors.TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )

            // Chord Detect Mode
            Column {
                Text("CHORD RECOGNITION MODE", color = ConsoleColors.TextSecondary, fontSize = 7.5.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    listOf(
                        "FINGERED" to "Fingered",
                        "SINGLE_FINGER" to "Single Finger",
                        "FULL" to "Full Keyboard"
                    ).forEach { (modeKey, modeTitle) ->
                        val isSel = chordDetectMode == modeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) ConsoleColors.LedOrange else Color(0xFF141924))
                                .clickable { onSetChordMode(modeKey) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modeTitle,
                                color = if (isSel) Color.Black else ConsoleColors.TextPrimary,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Transpose Shift
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MASTER TRANSPOSE", color = ConsoleColors.TextSecondary, fontSize = 7.5.sp)
                    Text(
                        if (transpose >= 0) "+$transpose SEMI" else "$transpose SEMI",
                        color = ConsoleColors.LedCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E2432))
                            .clickable { onShiftTranspose(-1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-1", color = ConsoleColors.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E2432))
                            .clickable { onShiftTranspose(+1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+1", color = ConsoleColors.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Octave Shift
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("KEYBOARD OCTAVE", color = ConsoleColors.TextSecondary, fontSize = 7.5.sp)
                    Text(
                        if (octaveShift >= 0) "+$octaveShift OCT" else "$octaveShift OCT",
                        color = ConsoleColors.LedOrange,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E2432))
                            .clickable { onShiftOctave(-1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("OCT -", color = ConsoleColors.TextPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E2432))
                            .clickable { onShiftOctave(+1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("OCT +", color = ConsoleColors.TextPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // COLUMN 3: AUDIO ENGINE DIAGNOSTICS & SYSTEM
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(ConsoleColors.ScreenCard)
                .border(1.dp, ConsoleColors.ScreenCardBorder, RoundedCornerShape(6.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DSP & ENGINE DIAGNOSTICS",
                color = ConsoleColors.TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DiagRow("AUDIO ARCHITECTURE", "Real-Time PCM Synthesis")
                DiagRow("SAMPLE RATE", "44,100 Hz Stereo (16-bit)")
                DiagRow("HARDWARE BUFFER", "512 Frames Low-Latency")
                DiagRow("POLYPHONY", "32 Dynamic Voices + Drums")
                DiagRow("DSP EFFECTS", "Schroeder Reverb + Chorus + Delay + EQ")
                DiagRow("ARRANGER ENGINE", "4/4 Real-Time 16-Step Clock")
                DiagRow("LOCAL STORAGE", "Room SQLite Database")
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF101B12))
                    .border(1.dp, ConsoleColors.LedGreen, RoundedCornerShape(4.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "● AUDIO ENGINE STATUS: ACTIVE",
                    color = ConsoleColors.LedGreen,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ConsoleColors.TextDisabled, fontSize = 7.sp)
        Text(value, color = ConsoleColors.TextPrimary, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
    }
}
