package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import com.example.model.*
import com.example.ui.ConsoleColors

@Composable
fun StyleDisplayScreen(
    currentStyle: Style,
    tempo: Int,
    currentSection: StyleSection,
    isAcmpEnabled: Boolean,
    isAutoFillEnabled: Boolean,
    isOtsLinkEnabled: Boolean,
    onSelectStyle: (Style) -> Unit,
    onTempoChange: (Int) -> Unit,
    onTapTempo: () -> Unit,
    onToggleAcmp: () -> Unit,
    onToggleAutoFill: () -> Unit,
    onToggleOtsLink: () -> Unit,
    onSectionSelect: (StyleSection) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(currentStyle.category) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .testTag("style_display_screen"),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // COLUMN 1: Style Categories
        Column(
            modifier = Modifier
                .width(90.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "STYLE CATEGORY",
                color = ConsoleColors.TextSecondary,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold
            )

            StyleCategory.values().forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) ConsoleColors.LedOrange else Color(0xFF141924))
                        .border(
                            0.5.dp,
                            if (isSelected) Color.White else ConsoleColors.ChassisBorder,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = cat.displayName,
                        color = if (isSelected) Color.White else ConsoleColors.TextSecondary,
                        fontSize = 8.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // COLUMN 2: Style List for Category
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(ConsoleColors.ScreenCard)
                .border(1.dp, ConsoleColors.ScreenCardBorder, RoundedCornerShape(6.dp))
                .padding(6.dp)
        ) {
            Text(
                text = "${selectedCategory.displayName.uppercase()} STYLES",
                color = ConsoleColors.TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            val categoryStyles = StylePresets.ALL_STYLES.filter { it.category == selectedCategory }
            val displayStyles = if (categoryStyles.isNotEmpty()) categoryStyles else StylePresets.ALL_STYLES

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(displayStyles) { style ->
                    val isCurrent = currentStyle.id == style.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCurrent) Color(0xFF282F3E) else Color(0xFF131822))
                            .border(
                                1.dp,
                                if (isCurrent) ConsoleColors.LedOrange else ConsoleColors.ChassisBorder,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onSelectStyle(style) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = style.name,
                                    color = if (isCurrent) ConsoleColors.LedOrange else ConsoleColors.TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${style.timeSignatureNumerator}/${style.timeSignatureDenominator} • ${style.tempo} BPM",
                                    color = ConsoleColors.TextDisabled,
                                    fontSize = 7.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (isCurrent) {
                                Text("LOADED", color = ConsoleColors.LedOrange, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // COLUMN 3: Tempo, Arranger Parameters & Features
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
                text = "TEMPO & SETTINGS",
                color = ConsoleColors.TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )

            // Tempo Display and Adjuster
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TEMPO", color = ConsoleColors.TextSecondary, fontSize = 7.5.sp)
                    Text(
                        "$tempo BPM",
                        color = ConsoleColors.LedOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E2430))
                            .border(0.5.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
                            .clickable { onTempoChange(tempo - 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", color = ConsoleColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E2430))
                            .border(0.5.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
                            .clickable { onTempoChange(tempo + 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = ConsoleColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2E2618))
                            .border(1.dp, ConsoleColors.LedOrange, RoundedCornerShape(4.dp))
                            .clickable { onTapTempo() }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("TAP", color = ConsoleColors.LedOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Slider(
                value = tempo.toFloat(),
                onValueChange = { onTempoChange(it.toInt()) },
                valueRange = 40f..260f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = ConsoleColors.LedOrange,
                    activeTrackColor = ConsoleColors.LedOrange,
                    inactiveTrackColor = ConsoleColors.ChassisBorder
                )
            )

            // Feature Toggles: ACMP, AUTO FILL, OTS LINK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // ACMP
                ToggleCard(
                    title = "ACMP",
                    subtitle = if (isAcmpEnabled) "ON" else "OFF",
                    isActive = isAcmpEnabled,
                    activeColor = ConsoleColors.LedGreen,
                    onClick = onToggleAcmp,
                    modifier = Modifier.weight(1f)
                )

                // AUTO FILL
                ToggleCard(
                    title = "AUTO FILL",
                    subtitle = if (isAutoFillEnabled) "ON" else "OFF",
                    isActive = isAutoFillEnabled,
                    activeColor = ConsoleColors.LedOrange,
                    onClick = onToggleAutoFill,
                    modifier = Modifier.weight(1f)
                )

                // OTS LINK
                ToggleCard(
                    title = "OTS LINK",
                    subtitle = if (isOtsLinkEnabled) "ON" else "OFF",
                    isActive = isOtsLinkEnabled,
                    activeColor = ConsoleColors.LedCyan,
                    onClick = onToggleOtsLink,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ToggleCard(
    title: String,
    subtitle: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.2f) else Color(0xFF131822))
            .border(1.dp, if (isActive) activeColor else ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = if (isActive) activeColor else ConsoleColors.TextSecondary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = if (isActive) Color.White else ConsoleColors.TextDisabled, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}
