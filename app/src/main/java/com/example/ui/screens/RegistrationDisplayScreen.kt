package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RegistrationEntity
import com.example.ui.ConsoleColors

@Composable
fun RegistrationDisplayScreen(
    currentBank: Int,
    activeSlot: Int?,
    registrationsList: List<RegistrationEntity>,
    onSelectBank: (Int) -> Unit,
    onSelectSlot: (Int) -> Unit,
    onSaveRegistration: (slot: Int, name: String) -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveSlotTarget by remember { mutableIntStateOf(1) }
    var saveNameInput by remember { mutableStateOf("") }

    val currentBankRegistrations = (1..8).map { slot ->
        val id = "B${currentBank}_S$slot"
        registrationsList.firstOrNull { it.id == id }
    }

    val activeRegistration = currentBankRegistrations.getOrNull((activeSlot ?: 1) - 1)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .testTag("registration_display_screen"),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // COLUMN 1: Bank Selector & Actions
        Column(
            modifier = Modifier
                .width(110.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(ConsoleColors.ScreenCard)
                .border(1.dp, ConsoleColors.ScreenCardBorder, RoundedCornerShape(6.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "BANK SELECT",
                    color = ConsoleColors.TextSecondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E2432))
                            .border(0.5.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
                            .clickable { onSelectBank((currentBank - 1).coerceAtLeast(1)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("◀", color = ConsoleColors.TextPrimary, fontSize = 10.sp)
                    }

                    Text(
                        text = "BANK $currentBank",
                        color = ConsoleColors.LedCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E2432))
                            .border(0.5.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
                            .clickable { onSelectBank((currentBank + 1).coerceAtMost(10)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", color = ConsoleColors.TextPrimary, fontSize = 10.sp)
                    }
                }
            }

            // Save Registration Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2C2214))
                    .border(1.dp, ConsoleColors.LedOrange, RoundedCornerShape(4.dp))
                    .clickable {
                        saveSlotTarget = activeSlot ?: 1
                        saveNameInput = activeRegistration?.name ?: "My Registration"
                        showSaveDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SAVE REGISTRATION",
                    color = ConsoleColors.LedOrange,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // COLUMN 2: 8 Registration Memory Slots (1-8)
        Column(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(ConsoleColors.ScreenCard)
                .border(1.dp, ConsoleColors.ScreenCardBorder, RoundedCornerShape(6.dp))
                .padding(6.dp)
        ) {
            Text(
                text = "MEMORY BUTTONS 1-8 (BANK $currentBank)",
                color = ConsoleColors.TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(8) { index ->
                    val slot = index + 1
                    val reg = currentBankRegistrations[index]
                    val isSelected = activeSlot == slot
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0xFF1E3548) else Color(0xFF141924))
                            .border(
                                1.dp,
                                if (isSelected) ConsoleColors.LedCyan else ConsoleColors.ChassisBorder,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onSelectSlot(slot) }
                            .padding(4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "[$slot] ${reg?.name ?: "<EMPTY>"}",
                                    color = if (isSelected) ConsoleColors.LedCyan else ConsoleColors.TextPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (reg != null) "${reg.styleId} • ${reg.tempo} BPM" else "Tap to select",
                                    color = ConsoleColors.TextDisabled,
                                    fontSize = 7.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                            if (isSelected) {
                                Text("LOADED", color = ConsoleColors.LedCyan, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // COLUMN 3: Active Registration Snapshot Details
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
                text = "SNAPSHOT PARAMETERS",
                color = ConsoleColors.TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )

            if (activeRegistration != null) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    SnapshotDetailRow("VOICE RIGHT 1", activeRegistration.voiceRight1Id)
                    SnapshotDetailRow("VOICE RIGHT 2", activeRegistration.voiceRight2Id)
                    SnapshotDetailRow("VOICE LEFT", activeRegistration.voiceLeftId)
                    SnapshotDetailRow("STYLE", activeRegistration.styleId)
                    SnapshotDetailRow("TEMPO", "${activeRegistration.tempo} BPM")
                    SnapshotDetailRow("KEY TRANSPOSE", "${activeRegistration.transpose}")
                    SnapshotDetailRow("SPLIT / LAYER", "Split: ${activeRegistration.isSplit} | Layer: ${activeRegistration.isLayer}")
                    SnapshotDetailRow("REVERB SEND", "${(activeRegistration.reverbSend * 100).toInt()}%")
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a slot to view parameters", color = ConsoleColors.TextDisabled, fontSize = 8.sp)
                }
            }
        }
    }

    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save to Registration $currentBank-$saveSlotTarget", color = ConsoleColors.TextPrimary, fontSize = 14.sp) },
            text = {
                Column {
                    Text("Enter name for this registration snapshot:", color = ConsoleColors.TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = saveNameInput,
                        onValueChange = { saveNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveRegistration(saveSlotTarget, saveNameInput)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ConsoleColors.LedCyan)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = ConsoleColors.TextSecondary)
                }
            },
            containerColor = Color(0xFF1B202C)
        )
    }
}

@Composable
private fun SnapshotDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ConsoleColors.TextDisabled, fontSize = 7.sp)
        Text(value, color = ConsoleColors.TextPrimary, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
