package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ConsoleColors

@Composable
fun PitchBendWheel(
    pitchBend: Float, // -2.0f to +2.0f
    onPitchBendChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val heightPx = 100f

    Column(
        modifier = modifier
            .testTag("pitch_bend_wheel")
            .width(42.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PITCH",
            color = ConsoleColors.TextSecondary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0C0E14))
                .border(1.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(6.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {},
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // drag up increases pitch, drag down decreases
                            val delta = -dragAmount.y / 25f
                            val current = pitchBend + delta
                            onPitchBendChange(current.coerceIn(-2.0f, 2.0f))
                        },
                        onDragEnd = {
                            // Spring return to center!
                            onPitchBendChange(0.0f)
                        },
                        onDragCancel = {
                            onPitchBendChange(0.0f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerY = h / 2f

                // Draw wheel cylinder background
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF151821), Color(0xFF2C3242), Color(0xFF151821))
                    )
                )

                // Draw wheel ribbing lines
                val numRibs = 9
                val normOffset = (pitchBend / 2.0f) // -1 to +1
                val offsetPx = -normOffset * (h * 0.35f)

                for (i in -numRibs..numRibs) {
                    val y = centerY + offsetPx + (i * 8.dp.toPx())
                    if (y in 4f..(h - 4f)) {
                        drawLine(
                            color = Color(0xFF485268),
                            start = Offset(4f, y),
                            end = Offset(w - 4f, y),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }

                // Center thumb groove
                val grooveY = centerY + offsetPx
                drawRect(
                    color = ConsoleColors.LedCyan,
                    topLeft = Offset(w * 0.2f, grooveY - 2.dp.toPx()),
                    size = Size(w * 0.6f, 4.dp.toPx())
                )

                // Center zero marker lines on the housing
                drawLine(
                    color = ConsoleColors.LedCyan.copy(alpha = 0.6f),
                    start = Offset(0f, centerY),
                    end = Offset(4f, centerY),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = ConsoleColors.LedCyan.copy(alpha = 0.6f),
                    start = Offset(w - 4f, centerY),
                    end = Offset(w, centerY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        val semiText = String.format("%.1f", pitchBend)
        Text(
            text = semiText,
            color = if (pitchBend != 0f) ConsoleColors.LedCyan else ConsoleColors.TextDisabled,
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ModulationWheel(
    modulation: Float, // 0.0f to 1.0f
    onModulationChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("modulation_wheel")
            .width(42.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MOD",
            color = ConsoleColors.TextSecondary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0C0E14))
                .border(1.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(6.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val delta = -dragAmount.y / 50f
                            val current = modulation + delta
                            onModulationChange(current.coerceIn(0.0f, 1.0f))
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw wheel cylinder background
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF151821), Color(0xFF2C3242), Color(0xFF151821))
                    )
                )

                // Draw wheel ribbing
                val numRibs = 9
                val offsetPx = (1.0f - modulation) * (h * 0.7f) + (h * 0.15f)

                for (i in -numRibs..numRibs) {
                    val y = offsetPx + (i * 8.dp.toPx())
                    if (y in 4f..(h - 4f)) {
                        drawLine(
                            color = Color(0xFF485268),
                            start = Offset(4f, y),
                            end = Offset(w - 4f, y),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }

                // Thumb position bar
                drawRect(
                    color = ConsoleColors.LedOrange,
                    topLeft = Offset(w * 0.2f, offsetPx - 2.dp.toPx()),
                    size = Size(w * 0.6f, 4.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "${(modulation * 100).toInt()}%",
            color = if (modulation > 0f) ConsoleColors.LedOrange else ConsoleColors.TextDisabled,
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
