package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChordInfo
import com.example.ui.ConsoleColors

data class KeyGeometry(
    val midiNote: Int,
    val isBlack: Boolean,
    val rectLeft: Float,
    val rectRight: Float,
    val rectTop: Float,
    val rectBottom: Float,
    val noteName: String
)

@Composable
fun PianoKeyboard(
    totalKeys: Int, // 61, 76, 88
    splitPoint: Int,
    isSplit: Boolean,
    pressedNotes: Set<Int>,
    showNoteLabels: Boolean,
    onNoteDown: (Int) -> Unit,
    onNoteUp: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 25 keys: C3 (MIDI 48) to C5 (MIDI 72)
    // 37 keys: C3 (MIDI 48) to C6 (MIDI 84)
    // 49 keys: C2 (MIDI 36) to C6 (MIDI 84)
    // 61 keys: C2 (MIDI 36) to C7 (MIDI 96)
    // 76 keys: E1 (MIDI 28) to G7 (MIDI 103)
    // 88 keys: A0 (MIDI 21) to C8 (MIDI 108)
    val startNote = when (totalKeys) {
        25 -> 48
        37 -> 48
        49 -> 36
        76 -> 28
        88 -> 21
        else -> 36 // 61 keys
    }
    val endNote = startNote + totalKeys - 1

    val activeTouches = remember { mutableStateMapOf<PointerId, Int>() }
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .testTag("piano_keyboard_container")
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .background(Color(0xFF0A0C10))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(totalKeys, startNote, endNote) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()

                            // Build geometries for touch testing
                            val keys = computeKeyGeometries(startNote, endNote, width, height)

                            for (change in event.changes) {
                                val pointerId = change.id
                                val pos = change.position

                                if (change.pressed) {
                                    // Hit test: first test black keys (on top layer)
                                    var hitNote: Int? = null

                                    for (k in keys.filter { it.isBlack }) {
                                        if (pos.x in k.rectLeft..k.rectRight && pos.y in k.rectTop..k.rectBottom) {
                                            hitNote = k.midiNote
                                            break
                                        }
                                    }

                                    // If not black key, test white keys
                                    if (hitNote == null) {
                                        for (k in keys.filter { !it.isBlack }) {
                                            if (pos.x in k.rectLeft..k.rectRight && pos.y in k.rectTop..k.rectBottom) {
                                                hitNote = k.midiNote
                                                break
                                            }
                                        }
                                    }

                                    val prevNote = activeTouches[pointerId]
                                    if (hitNote != null) {
                                        if (prevNote != hitNote) {
                                            // Finger moved or just touched
                                            if (prevNote != null) {
                                                onNoteUp(prevNote)
                                            }
                                            activeTouches[pointerId] = hitNote
                                            onNoteDown(hitNote)
                                        }
                                    } else {
                                        // Moved outside keyboard
                                        if (prevNote != null) {
                                            onNoteUp(prevNote)
                                            activeTouches.remove(pointerId)
                                        }
                                    }
                                } else {
                                    // Pointer released
                                    val prevNote = activeTouches.remove(pointerId)
                                    if (prevNote != null) {
                                        onNoteUp(prevNote)
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val keys = computeKeyGeometries(startNote, endNote, width, height)

            val whiteKeys = keys.filter { !it.isBlack }
            val blackKeys = keys.filter { it.isBlack }

            // 1. Draw Red Felt Ribbon above keyboard
            drawRect(
                color = Color(0xFFB71C1C),
                topLeft = Offset(0f, 0f),
                size = Size(width, 3.dp.toPx())
            )

            // 2. Draw White Keys
            for (k in whiteKeys) {
                val isPressed = pressedNotes.contains(k.midiNote)
                val keyW = k.rectRight - k.rectLeft
                val keyH = k.rectBottom - k.rectTop

                val bgBrush = if (isPressed) {
                    Brush.verticalGradient(
                        colors = listOf(
                            ConsoleColors.KeyWhitePressed,
                            ConsoleColors.LedCyan.copy(alpha = 0.45f)
                        ),
                        startY = k.rectTop,
                        endY = k.rectBottom
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFCFDFD),
                            ConsoleColors.KeyWhite,
                            Color(0xFFE4E8F0)
                        ),
                        startY = k.rectTop,
                        endY = k.rectBottom
                    )
                }

                // White key background
                drawRoundRect(
                    brush = bgBrush,
                    topLeft = Offset(k.rectLeft + 0.5f, k.rectTop + 3.dp.toPx()),
                    size = Size(keyW - 1f, keyH - 4.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )

                // White key separator line
                drawLine(
                    color = ConsoleColors.KeyWhiteBorder,
                    start = Offset(k.rectRight, k.rectTop + 3.dp.toPx()),
                    end = Offset(k.rectRight, k.rectBottom),
                    strokeWidth = 1f
                )

                // Bottom bevel shadow
                drawLine(
                    color = Color(0xFF9EA7B8),
                    start = Offset(k.rectLeft + 2f, k.rectBottom - 2f),
                    end = Offset(k.rectRight - 2f, k.rectBottom - 2f),
                    strokeWidth = 2f
                )

                // Note label on C notes or all notes
                if (showNoteLabels && (k.midiNote % 12 == 0 || totalKeys <= 61)) {
                    val octaveNum = (k.midiNote / 12) - 1
                    val label = if (k.midiNote % 12 == 0) "C$octaveNum" else ""
                    if (label.isNotEmpty()) {
                        val textLayout = textMeasurer.measure(
                            text = label,
                            style = TextStyle(
                                color = if (isPressed) ConsoleColors.LedCyan else ConsoleColors.TextDisabled,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        val textX = k.rectLeft + (keyW - textLayout.size.width) / 2f
                        val textY = k.rectBottom - textLayout.size.height - 4.dp.toPx()
                        drawText(textLayout, topLeft = Offset(textX, textY))
                    }
                }
            }

            // 3. Draw Black Keys
            for (k in blackKeys) {
                val isPressed = pressedNotes.contains(k.midiNote)
                val keyW = k.rectRight - k.rectLeft
                val keyH = k.rectBottom - k.rectTop

                val bgBrush = if (isPressed) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E2430),
                            ConsoleColors.LedCyan.copy(alpha = 0.5f)
                        ),
                        startY = k.rectTop,
                        endY = k.rectBottom
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF323640),
                            ConsoleColors.KeyBlack,
                            Color(0xFF0C0E12)
                        ),
                        startY = k.rectTop,
                        endY = k.rectBottom
                    )
                }

                // Black key drop shadow
                drawRect(
                    color = Color(0x66000000),
                    topLeft = Offset(k.rectLeft - 1f, k.rectTop),
                    size = Size(keyW + 2f, keyH + 4.dp.toPx())
                )

                // Black key body
                drawRoundRect(
                    brush = bgBrush,
                    topLeft = Offset(k.rectLeft, k.rectTop + 2.dp.toPx()),
                    size = Size(keyW, keyH),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // Glossy top bevel
                drawRoundRect(
                    color = Color(0xFF4A5262).copy(alpha = 0.4f),
                    topLeft = Offset(k.rectLeft + 2.dp.toPx(), k.rectTop + 3.dp.toPx()),
                    size = Size(keyW - 4.dp.toPx(), keyH * 0.75f),
                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                )
            }

            // 4. Draw Split Point Marker
            if (isSplit) {
                val splitKey = keys.firstOrNull { it.midiNote == splitPoint }
                if (splitKey != null) {
                    val splitX = splitKey.rectLeft
                    drawLine(
                        color = ConsoleColors.SplitMarker,
                        start = Offset(splitX, 0f),
                        end = Offset(splitX, height),
                        strokeWidth = 2.5.dp.toPx()
                    )

                    // Draw glowing badge
                    drawCircle(
                        color = ConsoleColors.SplitMarker,
                        radius = 4.dp.toPx(),
                        center = Offset(splitX, 8.dp.toPx())
                    )
                }
            }
        }
    }
}

private fun isBlackNote(midiNote: Int): Boolean {
    val noteInOctave = midiNote % 12
    return noteInOctave == 1 || noteInOctave == 3 || noteInOctave == 6 || noteInOctave == 8 || noteInOctave == 10
}

private fun computeKeyGeometries(startNote: Int, endNote: Int, totalWidth: Float, totalHeight: Float): List<KeyGeometry> {
    // Count white keys
    val whiteNotes = (startNote..endNote).filter { !isBlackNote(it) }
    val whiteCount = whiteNotes.size
    val whiteKeyWidth = totalWidth / whiteCount.toFloat()
    val blackKeyWidth = whiteKeyWidth * 0.62f
    val blackKeyHeight = totalHeight * 0.60f

    val result = mutableListOf<KeyGeometry>()
    var whiteIndex = 0

    // First map all white keys
    val whiteLeftMap = mutableMapOf<Int, Float>()

    for (n in startNote..endNote) {
        if (!isBlackNote(n)) {
            val left = whiteIndex * whiteKeyWidth
            val right = left + whiteKeyWidth
            whiteLeftMap[n] = left
            result.add(
                KeyGeometry(
                    midiNote = n,
                    isBlack = false,
                    rectLeft = left,
                    rectRight = right,
                    rectTop = 0f,
                    rectBottom = totalHeight,
                    noteName = ChordInfo.NOTE_NAMES[n % 12]
                )
            )
            whiteIndex++
        }
    }

    // Now map black keys relative to preceding white key
    for (n in startNote..endNote) {
        if (isBlackNote(n)) {
            val prevWhite = n - 1
            val prevLeft = whiteLeftMap[prevWhite] ?: 0f
            val centerBoundary = prevLeft + whiteKeyWidth
            val left = centerBoundary - (blackKeyWidth / 2f)
            val right = left + blackKeyWidth

            result.add(
                KeyGeometry(
                    midiNote = n,
                    isBlack = true,
                    rectLeft = left,
                    rectRight = right,
                    rectTop = 0f,
                    rectBottom = blackKeyHeight,
                    noteName = ChordInfo.NOTE_NAMES[n % 12]
                )
            )
        }
    }

    return result
}
