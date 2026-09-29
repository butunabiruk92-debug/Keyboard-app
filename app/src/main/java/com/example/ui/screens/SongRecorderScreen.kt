package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.RecordedSongEntity
import com.example.recorder.RecorderStatus
import com.example.ui.ConsoleColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SongRecorderScreen(
    status: RecorderStatus,
    recordedDurationMs: Long,
    playbackHeadMs: Long,
    songsList: List<RecordedSongEntity>,
    onToggleRecord: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onPlaySong: (RecordedSongEntity) -> Unit,
    onDeleteSong: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .testTag("song_recorder_screen"),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // COLUMN 1: Transport Controls & Status
        Column(
            modifier = Modifier
                .width(160.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(ConsoleColors.ScreenCard)
                .border(1.dp, ConsoleColors.ScreenCardBorder, RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "SONG RECORDER",
                    color = ConsoleColors.TextSecondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Time Display
                val displayMs = if (status == RecorderStatus.RECORDING) recordedDurationMs else playbackHeadMs
                val totalSec = displayMs / 1000
                val min = totalSec / 60
                val sec = totalSec % 60
                val tenths = (displayMs % 1000) / 100

                val timeStr = String.format("%02d:%02d.%d", min, sec, tenths)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0F131C))
                        .border(1.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeStr,
                        color = when (status) {
                            RecorderStatus.RECORDING -> ConsoleColors.LedRed
                            RecorderStatus.PLAYING -> ConsoleColors.LedGreen
                            else -> ConsoleColors.LedCyan
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Status Badge
                Text(
                    text = "STATUS: ${status.name}",
                    color = when (status) {
                        RecorderStatus.RECORDING -> ConsoleColors.LedRed
                        RecorderStatus.PLAYING -> ConsoleColors.LedGreen
                        RecorderStatus.PAUSED -> ConsoleColors.LedOrange
                        else -> ConsoleColors.TextSecondary
                    },
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Transport Buttons (Record, Play/Pause, Stop)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // RECORD BUTTON
                val isRec = status == RecorderStatus.RECORDING
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isRec) ConsoleColors.LedRed else Color(0xFF2C1618))
                        .border(1.dp, ConsoleColors.LedRed, RoundedCornerShape(4.dp))
                        .clickable(onClick = onToggleRecord),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRec) "■ STOP" else "● REC",
                        color = if (isRec) Color.White else ConsoleColors.LedRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // PLAY / PAUSE BUTTON
                val isPlay = status == RecorderStatus.PLAYING
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPlay) ConsoleColors.LedGreen else Color(0xFF132A1C))
                        .border(1.dp, ConsoleColors.LedGreen, RoundedCornerShape(4.dp))
                        .clickable {
                            if (status == RecorderStatus.PLAYING) onPause()
                            else if (status == RecorderStatus.PAUSED) onResume()
                            else if (songsList.isNotEmpty()) onPlaySong(songsList.first())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlay) "❚❚ PAUSE" else "▶ PLAY",
                        color = if (isPlay) Color.Black else ConsoleColors.LedGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // STOP BUTTON
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E2430))
                        .border(0.5.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
                        .clickable(onClick = onStop),
                    contentAlignment = Alignment.Center
                ) {
                    Text("■ STOP", color = ConsoleColors.TextPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // COLUMN 2: Recorded Songs List
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
                text = "SAVED RECORDINGS (${songsList.size})",
                color = ConsoleColors.TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (songsList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No saved songs yet. Tap '● REC' to record your performance!",
                        color = ConsoleColors.TextDisabled,
                        fontSize = 9.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(songsList) { song ->
                        val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(song.timestamp))
                        val songSec = song.durationMs / 1000
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF141924))
                                .border(0.5.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = song.title,
                                        color = ConsoleColors.TextPrimary,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$dateStr • ${song.styleId} • ${songSec}s",
                                        color = ConsoleColors.TextDisabled,
                                        fontSize = 7.5.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Play Button
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(ConsoleColors.LedGreen.copy(alpha = 0.2f))
                                            .border(1.dp, ConsoleColors.LedGreen, RoundedCornerShape(3.dp))
                                            .clickable { onPlaySong(song) }
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("▶ PLAY", color = ConsoleColors.LedGreen, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Delete Button
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(ConsoleColors.LedRed.copy(alpha = 0.2f))
                                            .border(1.dp, ConsoleColors.LedRed, RoundedCornerShape(3.dp))
                                            .clickable { onDeleteSong(song.id) }
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✕", color = ConsoleColors.LedRed, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
