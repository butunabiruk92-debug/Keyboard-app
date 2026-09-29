package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChannelId
import com.example.model.ChannelStrip
import com.example.model.MixerState
import com.example.ui.ConsoleColors

@Composable
fun MixerDisplayScreen(
    mixerState: MixerState,
    onVolumeChange: (ChannelId, Float) -> Unit,
    onPanChange: (ChannelId, Float) -> Unit,
    onToggleMute: (ChannelId) -> Unit,
    onToggleSolo: (ChannelId) -> Unit,
    onMasterVolumeChange: (Float) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mixer_display_screen"),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // SCROLLABLE 11 CHANNEL STRIPS
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ChannelId.values().forEach { chId ->
                val strip = mixerState.channels[chId] ?: ChannelStrip(chId)
                MixerChannelStrip(
                    strip = strip,
                    onVolumeChange = { onVolumeChange(chId, it) },
                    onPanChange = { onPanChange(chId, it) },
                    onToggleMute = { onToggleMute(chId) },
                    onToggleSolo = { onToggleSolo(chId) },
                    modifier = Modifier.width(52.dp)
                )
            }
        }

        // MASTER CHANNEL STRIP ON THE RIGHT
        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF141924))
                .border(1.dp, ConsoleColors.LedCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MASTER",
                    color = ConsoleColors.LedCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )

                // Master Volume Slider
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Slider(
                        value = mixerState.masterVolume,
                        onValueChange = onMasterVolumeChange,
                        modifier = Modifier.fillMaxHeight(),
                        colors = SliderDefaults.colors(
                            thumbColor = ConsoleColors.LedCyan,
                            activeTrackColor = ConsoleColors.LedCyan,
                            inactiveTrackColor = ConsoleColors.ChassisBorder
                        )
                    )
                }

                Text(
                    text = "${(mixerState.masterVolume * 100).toInt()}%",
                    color = ConsoleColors.LedCyan,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MixerChannelStrip(
    strip: ChannelStrip,
    onVolumeChange: (Float) -> Unit,
    onPanChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(ConsoleColors.ScreenCard)
            .border(1.dp, ConsoleColors.ScreenCardBorder, RoundedCornerShape(6.dp))
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Channel ID tag
        Text(
            text = strip.id.shortName,
            color = ConsoleColors.TextPrimary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        // PAN Controller
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val panVal = (strip.pan * 50).toInt()
            val panStr = if (panVal < 0) "L${-panVal}" else if (panVal > 0) "R$panVal" else "C"
            Text(panStr, color = ConsoleColors.TextSecondary, fontSize = 6.5.sp, fontFamily = FontFamily.Monospace)
            Slider(
                value = strip.pan,
                onValueChange = onPanChange,
                valueRange = -1f..1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                colors = SliderDefaults.colors(
                    thumbColor = ConsoleColors.LedOrange,
                    activeTrackColor = ConsoleColors.LedOrange,
                    inactiveTrackColor = ConsoleColors.ChassisBorder
                )
            )
        }

        // MUTE & SOLO BUTTONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // MUTE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (strip.isMuted) ConsoleColors.LedRed else Color(0xFF1E2432))
                    .clickable(onClick = onToggleMute),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    color = if (strip.isMuted) Color.White else ConsoleColors.TextDisabled,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // SOLO
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (strip.isSolo) Color(0xFFFFD600) else Color(0xFF1E2432))
                    .clickable(onClick = onToggleSolo),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    color = if (strip.isSolo) Color.Black else ConsoleColors.TextDisabled,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Vertical Fader / Volume Slider
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = strip.volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.fillMaxHeight(),
                colors = SliderDefaults.colors(
                    thumbColor = ConsoleColors.MetallicSilver,
                    activeTrackColor = ConsoleColors.LedCyan,
                    inactiveTrackColor = ConsoleColors.ChassisBorder
                )
            )
        }

        // Numerical Value Readout
        val dB = if (strip.volume > 0f) {
            val dbVal = (20.0 * kotlin.math.log10(strip.volume.toDouble())).toInt()
            "${dbVal}dB"
        } else {
            "-∞"
        }
        Text(
            text = dB,
            color = ConsoleColors.LedCyan,
            fontSize = 7.5.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
    }
}
