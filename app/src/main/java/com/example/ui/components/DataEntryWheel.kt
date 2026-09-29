package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SoundEffectType
import com.example.ui.ConsoleColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DataEntrySection(
    onRotate: (delta: Int) -> Unit,
    onEnter: () -> Unit,
    onYes: () -> Unit,
    onNo: () -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val soundFeedback = LocalSoundFeedback.current
    var dialAngle by remember { mutableFloatStateOf(0f) }
    var accumulatedDelta by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .testTag("data_entry_section")
            .width(105.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ConsoleColors.ChassisPanel)
            .border(1.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DATA DIAL",
            color = ConsoleColors.TextSecondary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Large Rotary Data Entry Wheel
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Color(0xFF0C0E14))
                .border(2.dp, ConsoleColors.ChassisBevelHighlight, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // Drag left/up decrements, drag right/down increments
                            val delta = dragAmount.x - dragAmount.y
                            accumulatedDelta += delta
                            dialAngle += delta * 2f

                            if (accumulatedDelta > 15f) {
                                onRotate(1)
                                accumulatedDelta = 0f
                            } else if (accumulatedDelta < -15f) {
                                onRotate(-1)
                                accumulatedDelta = 0f
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.width / 2f) - 3.dp.toPx()

                // Radial textured background
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF3E4658), Color(0xFF202532), Color(0xFF10131A)),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )

                // Indented finger grip dimple
                val rad = Math.toRadians(dialAngle.toDouble())
                val dimpleDist = radius * 0.65f
                val dimpleCenter = Offset(
                    (center.x + dimpleDist * cos(rad)).toFloat(),
                    (center.y + dimpleDist * sin(rad)).toFloat()
                )

                drawCircle(
                    color = Color(0xFF0B0D12),
                    radius = 5.dp.toPx(),
                    center = dimpleCenter
                )
                drawCircle(
                    color = ConsoleColors.LedCyan.copy(alpha = 0.8f),
                    radius = 3.dp.toPx(),
                    center = dimpleCenter
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Directional Pad / Buttons (Up, Down, Left, Right, Enter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallDataButton(label = "NO", color = ConsoleColors.LedRed, onClick = onNo)
            SmallDataButton(label = "▲", color = ConsoleColors.TextPrimary, onClick = onUp)
            SmallDataButton(label = "YES", color = ConsoleColors.LedGreen, onClick = onYes)
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallDataButton(label = "◀", color = ConsoleColors.TextPrimary, onClick = onLeft)
            SmallDataButton(label = "ENTER", color = ConsoleColors.LedCyan, onClick = onEnter, width = 38.dp)
            SmallDataButton(label = "▶", color = ConsoleColors.TextPrimary, onClick = onRight)
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            SmallDataButton(label = "▼", color = ConsoleColors.TextPrimary, onClick = onDown)
        }
    }
}

@Composable
private fun SmallDataButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 28.dp,
    height: androidx.compose.ui.unit.Dp = 20.dp
) {
    val soundFeedback = LocalSoundFeedback.current
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF1E2330))
            .border(0.5.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(3.dp))
            .clickable(onClick = {
                soundFeedback(SoundEffectType.BUTTON_CLICK)
                onClick()
            }),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
