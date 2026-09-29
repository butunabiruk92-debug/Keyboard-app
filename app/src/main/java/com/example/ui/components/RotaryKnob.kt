package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SoundEffectType
import com.example.ui.ConsoleColors
import kotlin.math.*

@Composable
fun RotaryKnob(
    label: String,
    value: Float, // 0.0f to 1.0f (or bipolar if isBipolar is true)
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isBipolar: Boolean = false,
    ledColor: Color = ConsoleColors.LedCyan,
    valueDisplay: String? = null,
    testTag: String = "rotary_knob_${label.lowercase().replace(" ", "_")}"
) {
    val soundFeedback = LocalSoundFeedback.current
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }
    var lastTickValue by remember { mutableFloatStateOf(value) }

    Column(
        modifier = modifier.testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Label
        Text(
            text = label.uppercase(),
            color = ConsoleColors.TextSecondary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Knob Canvas with drag detection
        Box(
            modifier = Modifier
                .size(size)
                .pointerInput(value) {
                    detectDragGestures(
                        onDragStart = { accumulatedDrag = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // Vertical drag: drag up increases value, drag down decreases
                            accumulatedDrag -= dragAmount.y * 0.008f
                            val delta = accumulatedDrag
                            accumulatedDrag = 0f
                            val newValue = (value + delta).coerceIn(0.0f, 1.0f)
                            if (abs(newValue - lastTickValue) >= 0.04f) {
                                soundFeedback(SoundEffectType.DIAL_TICK)
                                lastTickValue = newValue
                            }
                            onValueChange(newValue)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                val radius = (size.toPx() / 2f) - 4.dp.toPx()
                val knobRadius = radius - 4.dp.toPx()

                // Draw outer track arc (270 degrees total: from 135 deg to 405 deg)
                val startAngle = 135f
                val sweepAngle = 270f
                drawArc(
                    color = ConsoleColors.ChassisBorder,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw active LED arc
                if (isBipolar) {
                    val midAngle = 270f // Top center
                    val normalizedOffset = (value - 0.5f) * 2.0f // -1 to +1
                    val activeSweep = normalizedOffset * (sweepAngle / 2f)
                    drawArc(
                        color = ledColor,
                        startAngle = midAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else {
                    val activeSweep = value * sweepAngle
                    drawArc(
                        color = ledColor,
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Draw metallic Knob Body
                drawCircle(
                    brush = ConsoleColors.KnobMetalGradient,
                    radius = knobRadius,
                    center = center
                )

                // Bevel ring
                drawCircle(
                    color = ConsoleColors.MetallicSilver.copy(alpha = 0.5f),
                    radius = knobRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Pointer notch
                val pointerAngleDeg = startAngle + (value * sweepAngle)
                val pointerRad = Math.toRadians(pointerAngleDeg.toDouble())
                val notchStart = Offset(
                    (center.x + (knobRadius * 0.35f * cos(pointerRad))).toFloat(),
                    (center.y + (knobRadius * 0.35f * sin(pointerRad))).toFloat()
                )
                val notchEnd = Offset(
                    (center.x + (knobRadius * 0.88f * cos(pointerRad))).toFloat(),
                    (center.y + (knobRadius * 0.88f * sin(pointerRad))).toFloat()
                )
                drawLine(
                    color = ConsoleColors.TextPrimary,
                    start = notchStart,
                    end = notchEnd,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Value Readout
        val displayVal = valueDisplay ?: if (isBipolar) {
            val v = ((value - 0.5f) * 100).toInt()
            if (v > 0) "+$v" else "$v"
        } else {
            "${(value * 100).toInt()}%"
        }

        Text(
            text = displayVal,
            color = ledColor,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
