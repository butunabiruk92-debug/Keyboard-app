package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.model.EffectsState
import com.example.ui.ConsoleColors

@Composable
fun EffectsDisplayScreen(
    effectsState: EffectsState,
    onUpdateEffects: ((EffectsState) -> EffectsState) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .testTag("effects_display_screen"),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // UNIT 1: REVERB
        EffectCard(
            title = "SCHROEDER REVERB",
            isEnabled = effectsState.reverb.enabled,
            onToggleEnabled = {
                onUpdateEffects { it.copy(reverb = it.reverb.copy(enabled = !it.reverb.enabled)) }
            },
            accentColor = ConsoleColors.LedCyan,
            modifier = Modifier.weight(1f)
        ) {
            EffectSlider(
                label = "SEND LEVEL",
                value = effectsState.reverb.sendLevel,
                onValueChange = { v ->
                    onUpdateEffects { it.copy(reverb = it.reverb.copy(sendLevel = v)) }
                },
                accentColor = ConsoleColors.LedCyan
            )

            EffectSlider(
                label = "ROOM SIZE",
                value = effectsState.reverb.roomSize,
                onValueChange = { v ->
                    onUpdateEffects { it.copy(reverb = it.reverb.copy(roomSize = v)) }
                },
                accentColor = ConsoleColors.LedCyan
            )

            EffectSlider(
                label = "DAMPING",
                value = effectsState.reverb.damping,
                onValueChange = { v ->
                    onUpdateEffects { it.copy(reverb = it.reverb.copy(damping = v)) }
                },
                accentColor = ConsoleColors.LedCyan
            )
        }

        // UNIT 2: CHORUS
        EffectCard(
            title = "STEREO CHORUS",
            isEnabled = effectsState.chorus.enabled,
            onToggleEnabled = {
                onUpdateEffects { it.copy(chorus = it.chorus.copy(enabled = !it.chorus.enabled)) }
            },
            accentColor = ConsoleColors.LedOrange,
            modifier = Modifier.weight(1f)
        ) {
            EffectSlider(
                label = "MIX LEVEL",
                value = effectsState.chorus.mix,
                onValueChange = { v ->
                    onUpdateEffects { it.copy(chorus = it.chorus.copy(mix = v)) }
                },
                accentColor = ConsoleColors.LedOrange
            )

            EffectSlider(
                label = "MOD DEPTH",
                value = effectsState.chorus.depth,
                onValueChange = { v ->
                    onUpdateEffects { it.copy(chorus = it.chorus.copy(depth = v)) }
                },
                accentColor = ConsoleColors.LedOrange
            )

            EffectSlider(
                label = "RATE (0.1-5Hz)",
                value = (effectsState.chorus.rateHz - 0.1f) / 4.9f,
                displayValue = String.format("%.1f Hz", effectsState.chorus.rateHz),
                onValueChange = { norm ->
                    val hz = norm * 4.9f + 0.1f
                    onUpdateEffects { it.copy(chorus = it.chorus.copy(rateHz = hz)) }
                },
                accentColor = ConsoleColors.LedOrange
            )
        }

        // UNIT 3: DELAY
        EffectCard(
            title = "PING-PONG DELAY",
            isEnabled = effectsState.delay.enabled,
            onToggleEnabled = {
                onUpdateEffects { it.copy(delay = it.delay.copy(enabled = !it.delay.enabled)) }
            },
            accentColor = ConsoleColors.LedGreen,
            modifier = Modifier.weight(1f)
        ) {
            EffectSlider(
                label = "MIX",
                value = effectsState.delay.mix,
                onValueChange = { v ->
                    onUpdateEffects { it.copy(delay = it.delay.copy(mix = v)) }
                },
                accentColor = ConsoleColors.LedGreen
            )

            EffectSlider(
                label = "TIME",
                value = (effectsState.delay.timeMs - 50f) / 950f,
                displayValue = "${effectsState.delay.timeMs.toInt()} ms",
                onValueChange = { norm ->
                    val ms = norm * 950f + 50f
                    onUpdateEffects { it.copy(delay = it.delay.copy(timeMs = ms)) }
                },
                accentColor = ConsoleColors.LedGreen
            )

            EffectSlider(
                label = "FEEDBACK",
                value = effectsState.delay.feedback,
                onValueChange = { v ->
                    onUpdateEffects { it.copy(delay = it.delay.copy(feedback = v)) }
                },
                accentColor = ConsoleColors.LedGreen
            )
        }

        // UNIT 4: 3-BAND MASTER EQ
        EffectCard(
            title = "MASTER 3-BAND EQ",
            isEnabled = true,
            onToggleEnabled = {},
            accentColor = Color(0xFFAB47BC),
            modifier = Modifier.weight(1f)
        ) {
            // Low gain
            val lowNorm = (effectsState.eq.lowGainDb + 12f) / 24f
            EffectSlider(
                label = "LOW (200Hz)",
                value = lowNorm,
                displayValue = "${effectsState.eq.lowGainDb.toInt()} dB",
                onValueChange = { norm ->
                    val db = (norm * 24f) - 12f
                    onUpdateEffects { it.copy(eq = it.eq.copy(lowGainDb = db)) }
                },
                accentColor = Color(0xFFAB47BC)
            )

            // Mid gain
            val midNorm = (effectsState.eq.midGainDb + 12f) / 24f
            EffectSlider(
                label = "MID (1.5kHz)",
                value = midNorm,
                displayValue = "${effectsState.eq.midGainDb.toInt()} dB",
                onValueChange = { norm ->
                    val db = (norm * 24f) - 12f
                    onUpdateEffects { it.copy(eq = it.eq.copy(midGainDb = db)) }
                },
                accentColor = Color(0xFFAB47BC)
            )

            // High gain
            val highNorm = (effectsState.eq.highGainDb + 12f) / 24f
            EffectSlider(
                label = "HIGH (5kHz)",
                value = highNorm,
                displayValue = "${effectsState.eq.highGainDb.toInt()} dB",
                onValueChange = { norm ->
                    val db = (norm * 24f) - 12f
                    onUpdateEffects { it.copy(eq = it.eq.copy(highGainDb = db)) }
                },
                accentColor = Color(0xFFAB47BC)
            )
        }
    }
}

@Composable
private fun EffectCard(
    title: String,
    isEnabled: Boolean,
    onToggleEnabled: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(ConsoleColors.ScreenCard)
            .border(
                1.dp,
                if (isEnabled) accentColor.copy(alpha = 0.5f) else ConsoleColors.ScreenCardBorder,
                RoundedCornerShape(6.dp)
            )
            .padding(6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (isEnabled) accentColor else ConsoleColors.TextSecondary,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isEnabled) accentColor else Color(0xFF202636))
                    .clickable(onClick = onToggleEnabled)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isEnabled) "ON" else "BYPASS",
                    color = if (isEnabled) Color.Black else ConsoleColors.TextDisabled,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        content()
    }
}

@Composable
private fun EffectSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color,
    displayValue: String? = null
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = ConsoleColors.TextSecondary, fontSize = 7.sp)
            Text(
                displayValue ?: "${(value * 100).toInt()}%",
                color = accentColor,
                fontSize = 7.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = ConsoleColors.ChassisBorder
            )
        )
    }
}
