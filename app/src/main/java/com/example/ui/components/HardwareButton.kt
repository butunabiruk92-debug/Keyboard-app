package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SoundEffectType
import com.example.ui.ConsoleColors

val LocalSoundFeedback = compositionLocalOf<(SoundEffectType) -> Unit> { {} }

enum class ButtonLedState {
    OFF,
    ACTIVE_CYAN,
    ACTIVE_ORANGE,
    ACTIVE_RED,
    ACTIVE_GREEN,
    ACTIVE_BLUE
}

@Composable
fun HardwareButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subLabel: String? = null,
    ledState: ButtonLedState = ButtonLedState.OFF,
    soundType: SoundEffectType? = SoundEffectType.BUTTON_CLICK,
    width: Dp = 56.dp,
    height: Dp = 38.dp,
    isHighlighted: Boolean = false,
    testTag: String = "hw_btn_${label.lowercase().replace(" ", "_")}"
) {
    val soundFeedback = LocalSoundFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val ledColor = when (ledState) {
        ButtonLedState.ACTIVE_CYAN -> ConsoleColors.LedCyan
        ButtonLedState.ACTIVE_ORANGE -> ConsoleColors.LedOrange
        ButtonLedState.ACTIVE_RED -> ConsoleColors.LedRed
        ButtonLedState.ACTIVE_GREEN -> ConsoleColors.LedGreen
        ButtonLedState.ACTIVE_BLUE -> ConsoleColors.LedBlue
        ButtonLedState.OFF -> ConsoleColors.LedOff
    }

    val isLedOn = ledState != ButtonLedState.OFF

    val buttonBrush = if (isPressed) {
        Brush.verticalGradient(
            listOf(Color(0xFF13151D), Color(0xFF181B24))
        )
    } else if (isHighlighted) {
        Brush.verticalGradient(
            listOf(Color(0xFF2E3748), Color(0xFF1E2330))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFF282E3E), Color(0xFF1B1E28), Color(0xFF141720))
        )
    }

    val elevation = if (isPressed) 1.dp else 3.dp

    Box(
        modifier = modifier
            .testTag(testTag)
            .width(width)
            .height(height)
            .shadow(elevation, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF0D0F14)) // Recessed bevel border
            .padding(1.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(buttonBrush)
            .border(
                width = 0.5.dp,
                color = if (isLedOn) ledColor.copy(alpha = 0.5f) else ConsoleColors.ChassisBevelHighlight.copy(alpha = 0.6f),
                shape = RoundedCornerShape(3.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (soundType != null) {
                        soundFeedback(soundType)
                    }
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 1.dp)
        ) {
            // LED Indicator Bar or Dot
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(ledColor)
                    .border(
                        0.5.dp,
                        if (isLedOn) Color.White.copy(alpha = 0.5f) else Color.Transparent,
                        RoundedCornerShape(1.5.dp)
                    )
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Primary label
            Text(
                text = label,
                color = if (isLedOn) ConsoleColors.TextPrimary else ConsoleColors.TextSecondary,
                fontSize = 9.sp,
                fontWeight = if (isLedOn) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            if (subLabel != null) {
                Text(
                    text = subLabel,
                    color = if (isLedOn) ledColor else ConsoleColors.TextDisabled,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
