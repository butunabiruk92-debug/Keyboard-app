package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arranger.ArrangerPlaybackState
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.viewmodel.ArrangerViewModel
import com.example.viewmodel.ConsoleDisplayScreen

@Composable
fun ArrangerConsoleScreen(
    viewModel: ArrangerViewModel,
    modifier: Modifier = Modifier
) {
    // Collect states from ViewModel and engines
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val voiceRight1 by viewModel.voiceRight1.collectAsStateWithLifecycle()
    val voiceRight2 by viewModel.voiceRight2.collectAsStateWithLifecycle()
    val voiceLeft by viewModel.voiceLeft.collectAsStateWithLifecycle()
    val isSplit by viewModel.isSplitEnabled.collectAsStateWithLifecycle()
    val isLayer by viewModel.isLayerEnabled.collectAsStateWithLifecycle()
    val splitPoint by viewModel.splitPoint.collectAsStateWithLifecycle()
    val transpose by viewModel.transpose.collectAsStateWithLifecycle()
    val octaveShift by viewModel.octaveShift.collectAsStateWithLifecycle()
    val upperOctaveShift by viewModel.upperOctaveShift.collectAsStateWithLifecycle()
    val isSustainOn by viewModel.isSustainOn.collectAsStateWithLifecycle()
    val pitchBend by viewModel.pitchBend.collectAsStateWithLifecycle()
    val modulation by viewModel.modulation.collectAsStateWithLifecycle()

    val masterVolume by viewModel.masterVolume.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val micVolume by viewModel.micVolume.collectAsStateWithLifecycle()

    val mixerState by viewModel.mixerState.collectAsStateWithLifecycle()
    val effectsState by viewModel.effectsState.collectAsStateWithLifecycle()

    val currentBank by viewModel.currentBank.collectAsStateWithLifecycle()
    val activeRegistrationSlot by viewModel.activeRegistrationSlot.collectAsStateWithLifecycle()
    val registrationsList by viewModel.registrationsList.collectAsStateWithLifecycle()
    val recordedSongsList by viewModel.recordedSongsList.collectAsStateWithLifecycle()

    val pressedNotes by viewModel.pressedNotes.collectAsStateWithLifecycle()
    val keyboardTotalKeys by viewModel.keyboardTotalKeys.collectAsStateWithLifecycle()
    val showNoteLabels by viewModel.showNoteLabels.collectAsStateWithLifecycle()
    val chordDetectMode by viewModel.chordDetectMode.collectAsStateWithLifecycle()

    // Arranger engine states
    val playbackState by viewModel.arrangerEngine.playbackState.collectAsStateWithLifecycle()
    val currentStyle by viewModel.arrangerEngine.currentStyle.collectAsStateWithLifecycle()
    val currentSection by viewModel.arrangerEngine.currentSection.collectAsStateWithLifecycle()
    val tempo by viewModel.arrangerEngine.tempo.collectAsStateWithLifecycle()
    val currentMeasure by viewModel.arrangerEngine.currentMeasure.collectAsStateWithLifecycle()
    val currentBeat by viewModel.arrangerEngine.currentBeat.collectAsStateWithLifecycle()
    val currentStep by viewModel.arrangerEngine.currentStep.collectAsStateWithLifecycle()
    val detectedChord by viewModel.arrangerEngine.detectedChord.collectAsStateWithLifecycle()
    val isAcmpEnabled by viewModel.arrangerEngine.isAcmpEnabled.collectAsStateWithLifecycle()
    val isAutoFillEnabled by viewModel.arrangerEngine.isAutoFillEnabled.collectAsStateWithLifecycle()
    val isOtsLinkEnabled by viewModel.arrangerEngine.isOtsLinkEnabled.collectAsStateWithLifecycle()
    val isMetronomeEnabled by viewModel.arrangerEngine.isMetronomeEnabled.collectAsStateWithLifecycle()
    val activeMultiPad by viewModel.arrangerEngine.activeMultiPad.collectAsStateWithLifecycle()

    // Recorder engine states
    val recorderStatus by viewModel.songRecorder.status.collectAsStateWithLifecycle()
    val recordedDurationMs by viewModel.songRecorder.recordedDurationMs.collectAsStateWithLifecycle()
    val currentPlayheadMs by viewModel.songRecorder.currentPlayheadMs.collectAsStateWithLifecycle()

    var isExpandedKeyboard by remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalSoundFeedback provides { type -> viewModel.playUiSound(type) }
    ) {
        // Main landscape chassis frame
        Column(
            modifier = modifier
                .testTag("arranger_console_main")
                .fillMaxSize()
                .background(ConsoleColors.ChassisDark)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
        // ===================================================================
        // 1. TOP HARDWARE CONTROL BAR (Brand plate, knobs, status indicators)
        // ===================================================================
        TopHardwareBar(
            masterVolume = masterVolume,
            balance = balance,
            micVolume = micVolume,
            isMetronomeOn = isMetronomeEnabled,
            currentBeat = currentBeat,
            isExpandedKeyboard = isExpandedKeyboard,
            onToggleExpandKeyboard = { isExpandedKeyboard = !isExpandedKeyboard },
            onMasterVolumeChange = { viewModel.setMasterVolume(it) },
            onBalanceChange = { viewModel.setBalance(it) },
            onMicVolumeChange = { viewModel.setMicVolume(it) },
            onToggleMetronome = { viewModel.arrangerEngine.toggleMetronome() },
            onTapTempo = { viewModel.arrangerEngine.tapTempo() },
            onHomeClick = { viewModel.navigateTo(ConsoleDisplayScreen.HOME) },
            onMenuClick = { viewModel.navigateTo(ConsoleDisplayScreen.SETTINGS) },
            onSaveClick = { viewModel.navigateTo(ConsoleDisplayScreen.REGISTRATION) }
        )

        Spacer(modifier = Modifier.height(2.dp))

        // ===================================================================
        // 2. MIDDLE DECK: LEFT CONTROLS | CENTER TOUCHSCREEN | RIGHT CONTROLS
        // ===================================================================
        if (!isExpandedKeyboard) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.95f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
            // --- LEFT PANEL: Arranger & Style Hardware Controls ---
            LeftArrangerPanel(
                playbackState = playbackState,
                currentSection = currentSection,
                isAcmpEnabled = isAcmpEnabled,
                isAutoFillEnabled = isAutoFillEnabled,
                isOtsLinkEnabled = isOtsLinkEnabled,
                onStartStop = { viewModel.arrangerEngine.startOrStop() },
                onSyncStart = { viewModel.arrangerEngine.armSyncStart() },
                onSyncStop = { viewModel.arrangerEngine.armSyncStop() },
                onSelectSection = { viewModel.arrangerEngine.selectSection(it) },
                onFillIn = { viewModel.arrangerEngine.triggerFillIn() },
                onBreak = { viewModel.arrangerEngine.triggerBreak() },
                onEnding = { viewModel.arrangerEngine.triggerEnding() },
                onToggleAcmp = { viewModel.arrangerEngine.toggleAcmp() },
                onToggleAutoFill = { viewModel.arrangerEngine.toggleAutoFill() },
                onToggleOts = { viewModel.arrangerEngine.toggleOtsLink() },
                onOpenStyles = { viewModel.navigateTo(ConsoleDisplayScreen.STYLE) },
                onOpenSongs = { viewModel.navigateTo(ConsoleDisplayScreen.SONG) }
            )

            // --- CENTER TOUCHSCREEN DISPLAY ---
            CenterTouchscreen(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) },
                styleName = currentStyle.name,
                tempo = tempo,
                timeSignature = "${currentStyle.timeSignatureNumerator}/${currentStyle.timeSignatureDenominator}",
                measure = currentMeasure,
                beat = currentBeat,
                chord = detectedChord,
                transpose = transpose,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (currentScreen) {
                    ConsoleDisplayScreen.HOME -> HomeDisplayScreen(
                        voiceRight1 = voiceRight1,
                        voiceRight2 = voiceRight2,
                        voiceLeft = voiceLeft,
                        isSplit = isSplit,
                        isLayer = isLayer,
                        splitPoint = splitPoint,
                        currentStyle = currentStyle,
                        currentSection = currentSection,
                        detectedChord = detectedChord,
                        masterVolume = masterVolume,
                        balance = balance,
                        onNavigate = { viewModel.navigateTo(it) },
                        onSectionSelect = { viewModel.arrangerEngine.selectSection(it) },
                        onMasterVolumeChange = { viewModel.setMasterVolume(it) },
                        onBalanceChange = { viewModel.setBalance(it) }
                    )
                    ConsoleDisplayScreen.VOICE -> VoiceDisplayScreen(
                        currentVoiceRight1 = voiceRight1,
                        currentVoiceRight2 = voiceRight2,
                        currentVoiceLeft = voiceLeft,
                        onSelectVoiceRight1 = { viewModel.setVoiceRight1(it) },
                        onSelectVoiceRight2 = { viewModel.setVoiceRight2(it) },
                        onSelectVoiceLeft = { viewModel.setVoiceLeft(it) }
                    )
                    ConsoleDisplayScreen.STYLE -> StyleDisplayScreen(
                        currentStyle = currentStyle,
                        tempo = tempo,
                        currentSection = currentSection,
                        isAcmpEnabled = isAcmpEnabled,
                        isAutoFillEnabled = isAutoFillEnabled,
                        isOtsLinkEnabled = isOtsLinkEnabled,
                        onSelectStyle = { viewModel.arrangerEngine.setStyle(it) },
                        onTempoChange = { viewModel.arrangerEngine.setTempo(it) },
                        onTapTempo = { viewModel.arrangerEngine.tapTempo() },
                        onToggleAcmp = { viewModel.arrangerEngine.toggleAcmp() },
                        onToggleAutoFill = { viewModel.arrangerEngine.toggleAutoFill() },
                        onToggleOtsLink = { viewModel.arrangerEngine.toggleOtsLink() },
                        onSectionSelect = { viewModel.arrangerEngine.selectSection(it) }
                    )
                    ConsoleDisplayScreen.SONG -> SongRecorderScreen(
                        status = recorderStatus,
                        recordedDurationMs = recordedDurationMs,
                        playbackHeadMs = currentPlayheadMs,
                        songsList = recordedSongsList,
                        onToggleRecord = { viewModel.toggleRecording() },
                        onPause = { viewModel.songRecorder.pausePlayback() },
                        onResume = { viewModel.songRecorder.resumePlayback() },
                        onStop = { viewModel.songRecorder.stopAll() },
                        onPlaySong = { viewModel.playRecordedSong(it) },
                        onDeleteSong = { viewModel.deleteRecordedSong(it) }
                    )
                    ConsoleDisplayScreen.MIXER -> MixerDisplayScreen(
                        mixerState = mixerState,
                        onVolumeChange = { ch, vol -> viewModel.updateMixerChannelVolume(ch, vol) },
                        onPanChange = { ch, pan -> viewModel.updateMixerChannelPan(ch, pan) },
                        onToggleMute = { ch -> viewModel.toggleMixerChannelMute(ch) },
                        onToggleSolo = { ch -> viewModel.toggleMixerChannelSolo(ch) },
                        onMasterVolumeChange = { viewModel.setMasterVolume(it) }
                    )
                    ConsoleDisplayScreen.EFFECTS -> EffectsDisplayScreen(
                        effectsState = effectsState,
                        onUpdateEffects = { viewModel.updateEffects(it) }
                    )
                    ConsoleDisplayScreen.REGISTRATION -> RegistrationDisplayScreen(
                        currentBank = currentBank,
                        activeSlot = activeRegistrationSlot,
                        registrationsList = registrationsList,
                        onSelectBank = { viewModel.setBank(it) },
                        onSelectSlot = { viewModel.selectRegistrationSlot(it) },
                        onSaveRegistration = { slot, name -> viewModel.saveCurrentToRegistration(slot, name) }
                    )
                    ConsoleDisplayScreen.SETTINGS -> SettingsDisplayScreen(
                        keyboardTotalKeys = keyboardTotalKeys,
                        showNoteLabels = showNoteLabels,
                        chordDetectMode = chordDetectMode,
                        splitPoint = splitPoint,
                        transpose = transpose,
                        octaveShift = octaveShift,
                        onSetTotalKeys = { viewModel.setKeyboardTotalKeys(it) },
                        onToggleNoteLabels = { viewModel.toggleNoteLabels() },
                        onSetChordMode = { viewModel.setChordDetectMode(it) },
                        onSetSplitPoint = { viewModel.setSplitPoint(it) },
                        onShiftTranspose = { viewModel.shiftTranspose(it) },
                        onShiftOctave = { viewModel.shiftOctave(it) }
                    )
                }
            }

            // --- RIGHT PANEL: Voice Parts, Multi Pad & Data Dial ---
            RightVoiceDataPanel(
                voiceRight1 = voiceRight1,
                voiceRight2 = voiceRight2,
                voiceLeft = voiceLeft,
                isSplit = isSplit,
                isLayer = isLayer,
                activeMultiPad = activeMultiPad,
                onToggleSplit = { viewModel.toggleSplit() },
                onToggleLayer = { viewModel.toggleLayer() },
                onTriggerMultiPad = { viewModel.arrangerEngine.triggerMultiPad(it) },
                onStopMultiPad = { viewModel.arrangerEngine.stopMultiPad() },
                onDataRotate = { viewModel.onDataDialRotated(it) },
                onOpenVoice = { viewModel.navigateTo(ConsoleDisplayScreen.VOICE) }
            )
        }
    } else {
        // Slim status and quick transport bar in Expanded Keyboard Mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF131822))
                .border(1.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "STYLE: ${currentStyle.name}",
                    color = ConsoleColors.LedCyan,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$tempo BPM",
                    color = ConsoleColors.LedOrange,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "BAR $currentMeasure:$currentBeat",
                    color = ConsoleColors.LedGreen,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "CHORD: ${detectedChord.displayName}",
                    color = ConsoleColors.LedCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "R1: ${voiceRight1.name}",
                    color = ConsoleColors.TextPrimary,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )

                // Start/Stop
                val isPlaying = playbackState == ArrangerPlaybackState.PLAYING
                Box(
                    modifier = Modifier
                        .height(22.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isPlaying) ConsoleColors.LedGreen else Color(0xFF1E2430))
                        .clickable { viewModel.arrangerEngine.startOrStop() }
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlaying) "■ STOP" else "▶ START",
                        color = if (isPlaying) Color.Black else Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ACMP
                Box(
                    modifier = Modifier
                        .height(22.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isAcmpEnabled) ConsoleColors.LedGreen.copy(alpha = 0.25f) else Color(0xFF1E2430))
                        .clickable { viewModel.arrangerEngine.toggleAcmp() }
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ACMP",
                        color = if (isAcmpEnabled) ConsoleColors.LedGreen else ConsoleColors.TextDisabled,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(2.dp))

    // ===================================================================
    // 3. REGISTRATION BUTTONS BAR (Memory 1-8 directly above keyboard)
    // ===================================================================
    RegistrationButtonsBar(
        currentBank = currentBank,
        activeSlot = activeRegistrationSlot,
        keyboardTotalKeys = keyboardTotalKeys,
        onSelectBank = { viewModel.setBank(it) },
        onSelectSlot = { viewModel.selectRegistrationSlot(it) },
        onSetTotalKeys = { viewModel.setKeyboardTotalKeys(it) }
    )

    Spacer(modifier = Modifier.height(2.dp))

    // ===================================================================
    // 4. BOTTOM PIANO SECTION: WHEELS & FULL MULTI-TOUCH KEYBOARD
    // ===================================================================
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(if (isExpandedKeyboard) 1.0f else 1.35f),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
            // Wheels & Performance Control Buttons on the left
            PerformanceLeftStrip(
                pitchBend = pitchBend,
                modulation = modulation,
                isSustainOn = isSustainOn,
                transpose = transpose,
                octaveShift = octaveShift,
                upperOctaveShift = upperOctaveShift,
                onPitchBendChange = { viewModel.setPitchBend(it) },
                onModulationChange = { viewModel.setModulation(it) },
                onToggleSustain = { viewModel.toggleSustain() },
                onShiftTranspose = { viewModel.shiftTranspose(it) },
                onShiftOctave = { viewModel.shiftOctave(it) }
            )

            // Full-Width Multi-Touch Responsive Piano Keyboard
            PianoKeyboard(
                totalKeys = keyboardTotalKeys,
                splitPoint = splitPoint,
                isSplit = isSplit,
                pressedNotes = pressedNotes,
                showNoteLabels = showNoteLabels,
                onNoteDown = { viewModel.onKeyTouchDown(it) },
                onNoteUp = { viewModel.onKeyTouchUp(it) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}
}

// --- SUB-PANEL COMPOSABLES ---

@Composable
private fun TopHardwareBar(
    masterVolume: Float,
    balance: Float,
    micVolume: Float,
    isMetronomeOn: Boolean,
    currentBeat: Int,
    isExpandedKeyboard: Boolean,
    onToggleExpandKeyboard: () -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    onBalanceChange: (Float) -> Unit,
    onMicVolumeChange: (Float) -> Unit,
    onToggleMetronome: () -> Unit,
    onTapTempo: () -> Unit,
    onHomeClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(ConsoleColors.MetalBrushGradient)
            .border(1.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Power & Brand Plate
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Power LED button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141924))
                    .border(1.dp, ConsoleColors.LedCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ConsoleColors.LedCyan)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Workstation Brand Label
            Column {
                Text(
                    text = "AP-9000 WORKSTATION",
                    color = ConsoleColors.TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "PROFESSIONAL VIRTUAL ARRANGER CONSOLE",
                    color = ConsoleColors.TextSecondary,
                    fontSize = 6.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Knobs Section: Master, Balance, Mic
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RotaryKnob(
                label = "MASTER",
                value = masterVolume,
                onValueChange = onMasterVolumeChange,
                size = 36.dp
            )

            RotaryKnob(
                label = "BALANCE",
                value = balance,
                onValueChange = onBalanceChange,
                isBipolar = true,
                ledColor = ConsoleColors.LedOrange,
                size = 36.dp
            )

            RotaryKnob(
                label = "MIC VOL",
                value = micVolume,
                onValueChange = onMicVolumeChange,
                ledColor = ConsoleColors.LedGreen,
                size = 36.dp
            )
        }

        // Metronome, Tap Tempo & Status Indicators
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Metronome Button with blinking LED
            val metLedState = if (isMetronomeOn) {
                if (currentBeat == 1) ButtonLedState.ACTIVE_RED else ButtonLedState.ACTIVE_CYAN
            } else {
                ButtonLedState.OFF
            }

            HardwareButton(
                label = "METRO",
                subLabel = if (isMetronomeOn) "ON" else "OFF",
                ledState = metLedState,
                onClick = onToggleMetronome,
                width = 46.dp,
                height = 36.dp
            )

            HardwareButton(
                label = "TAP",
                subLabel = "TEMPO",
                ledState = ButtonLedState.OFF,
                onClick = onTapTempo,
                width = 46.dp,
                height = 36.dp
            )

            // Function buttons: HOME, MENU, SAVE
            HardwareButton(
                label = "HOME",
                onClick = onHomeClick,
                width = 42.dp,
                height = 36.dp
            )

            HardwareButton(
                label = "MENU",
                onClick = onMenuClick,
                width = 42.dp,
                height = 36.dp
            )

            HardwareButton(
                label = "SAVE",
                ledState = ButtonLedState.ACTIVE_ORANGE,
                onClick = onSaveClick,
                width = 42.dp,
                height = 36.dp
            )

            // Full Keyboard / Console View Switcher
            HardwareButton(
                label = if (isExpandedKeyboard) "CONSOLE" else "EXPAND",
                subLabel = if (isExpandedKeyboard) "VIEW" else "KEYBOARD",
                ledState = if (isExpandedKeyboard) ButtonLedState.ACTIVE_CYAN else ButtonLedState.OFF,
                onClick = onToggleExpandKeyboard,
                width = 54.dp,
                height = 36.dp
            )
        }
    }
}

@Composable
private fun LeftArrangerPanel(
    playbackState: ArrangerPlaybackState,
    currentSection: StyleSection,
    isAcmpEnabled: Boolean,
    isAutoFillEnabled: Boolean,
    isOtsLinkEnabled: Boolean,
    onStartStop: () -> Unit,
    onSyncStart: () -> Unit,
    onSyncStop: () -> Unit,
    onSelectSection: (StyleSection) -> Unit,
    onFillIn: () -> Unit,
    onBreak: () -> Unit,
    onEnding: () -> Unit,
    onToggleAcmp: () -> Unit,
    onToggleAutoFill: () -> Unit,
    onToggleOts: () -> Unit,
    onOpenStyles: () -> Unit,
    onOpenSongs: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(135.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(ConsoleColors.ChassisPanel)
            .border(1.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(6.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ARRANGER", color = ConsoleColors.TextSecondary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                // Style selector shortcut
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF1E2638))
                        .clickable(onClick = onOpenStyles)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("STYLES", color = ConsoleColors.LedCyan, fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF1E2638))
                        .clickable(onClick = onOpenSongs)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("SONGS", color = ConsoleColors.LedGreen, fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Large START / STOP Button + SYNC START / STOP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val isPlaying = playbackState == ArrangerPlaybackState.PLAYING
            val isArmed = playbackState == ArrangerPlaybackState.SYNC_START_ARMED

            // START / STOP
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isPlaying) Color(0xFF1A3824) else Color(0xFF1E2430))
                    .border(
                        1.dp,
                        if (isPlaying) ConsoleColors.LedGreen else ConsoleColors.ChassisBevelHighlight,
                        RoundedCornerShape(4.dp)
                    )
                    .clickable(onClick = onStartStop),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(if (isPlaying) ConsoleColors.LedGreen else ConsoleColors.LedRed)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isPlaying) "STOP" else "START",
                        color = if (isPlaying) ConsoleColors.LedGreen else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // SYNC START
            HardwareButton(
                label = "SYNC",
                subLabel = "START",
                ledState = if (isArmed) ButtonLedState.ACTIVE_GREEN else ButtonLedState.OFF,
                onClick = onSyncStart,
                width = 38.dp,
                height = 34.dp
            )

            // SYNC STOP
            HardwareButton(
                label = "SYNC",
                subLabel = "STOP",
                onClick = onSyncStop,
                width = 38.dp,
                height = 34.dp
            )
        }

        // INTRO & MAIN A, B, C, D
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            HardwareButton(
                label = "INTRO",
                ledState = if (currentSection == StyleSection.INTRO) ButtonLedState.ACTIVE_GREEN else ButtonLedState.OFF,
                onClick = { onSelectSection(StyleSection.INTRO) },
                width = 30.dp,
                height = 28.dp
            )
            HardwareButton(
                label = "A",
                ledState = if (currentSection == StyleSection.MAIN_A) ButtonLedState.ACTIVE_CYAN else ButtonLedState.OFF,
                onClick = { onSelectSection(StyleSection.MAIN_A) },
                width = 24.dp,
                height = 28.dp
            )
            HardwareButton(
                label = "B",
                ledState = if (currentSection == StyleSection.MAIN_B) ButtonLedState.ACTIVE_CYAN else ButtonLedState.OFF,
                onClick = { onSelectSection(StyleSection.MAIN_B) },
                width = 24.dp,
                height = 28.dp
            )
            HardwareButton(
                label = "C",
                ledState = if (currentSection == StyleSection.MAIN_C) ButtonLedState.ACTIVE_CYAN else ButtonLedState.OFF,
                onClick = { onSelectSection(StyleSection.MAIN_C) },
                width = 24.dp,
                height = 28.dp
            )
            HardwareButton(
                label = "D",
                ledState = if (currentSection == StyleSection.MAIN_D) ButtonLedState.ACTIVE_CYAN else ButtonLedState.OFF,
                onClick = { onSelectSection(StyleSection.MAIN_D) },
                width = 24.dp,
                height = 28.dp
            )
        }

        // FILL IN, BREAK, ENDING
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            HardwareButton(
                label = "FILL",
                ledState = if (currentSection == StyleSection.FILL_IN) ButtonLedState.ACTIVE_ORANGE else ButtonLedState.OFF,
                onClick = onFillIn,
                width = 38.dp,
                height = 28.dp
            )
            HardwareButton(
                label = "BREAK",
                ledState = if (currentSection == StyleSection.BREAK) ButtonLedState.ACTIVE_RED else ButtonLedState.OFF,
                onClick = onBreak,
                width = 42.dp,
                height = 28.dp
            )
            HardwareButton(
                label = "ENDING",
                ledState = if (currentSection == StyleSection.ENDING) ButtonLedState.ACTIVE_CYAN else ButtonLedState.OFF,
                onClick = onEnding,
                width = 44.dp,
                height = 28.dp
            )
        }

        // ACMP, AUTO FILL, OTS LINK
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            HardwareButton(
                label = "ACMP",
                ledState = if (isAcmpEnabled) ButtonLedState.ACTIVE_GREEN else ButtonLedState.OFF,
                onClick = onToggleAcmp,
                width = 40.dp,
                height = 28.dp
            )
            HardwareButton(
                label = "A.FILL",
                ledState = if (isAutoFillEnabled) ButtonLedState.ACTIVE_ORANGE else ButtonLedState.OFF,
                onClick = onToggleAutoFill,
                width = 42.dp,
                height = 28.dp
            )
            HardwareButton(
                label = "OTS",
                ledState = if (isOtsLinkEnabled) ButtonLedState.ACTIVE_CYAN else ButtonLedState.OFF,
                onClick = onToggleOts,
                width = 42.dp,
                height = 28.dp
            )
        }
    }
}

@Composable
private fun RightVoiceDataPanel(
    voiceRight1: Voice,
    voiceRight2: Voice,
    voiceLeft: Voice,
    isSplit: Boolean,
    isLayer: Boolean,
    activeMultiPad: MultiPadId?,
    onToggleSplit: () -> Unit,
    onToggleLayer: () -> Unit,
    onTriggerMultiPad: (MultiPadId) -> Unit,
    onStopMultiPad: () -> Unit,
    onDataRotate: (Int) -> Unit,
    onOpenVoice: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(225.dp)
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // SUB-COLUMN A: Voice Part Selectors & Multi-Pads
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(ConsoleColors.ChassisPanel)
                .border(1.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(6.dp))
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("VOICE & PADS", color = ConsoleColors.TextSecondary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)

            // Right 1, Right 2, Left Buttons
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                HardwareButton(
                    label = "RIGHT 1",
                    subLabel = voiceRight1.name.take(9),
                    ledState = ButtonLedState.ACTIVE_CYAN,
                    onClick = onOpenVoice,
                    width = 110.dp,
                    height = 28.dp
                )

                HardwareButton(
                    label = "RIGHT 2 (LAYER)",
                    subLabel = if (isLayer) voiceRight2.name.take(9) else "OFF",
                    ledState = if (isLayer) ButtonLedState.ACTIVE_ORANGE else ButtonLedState.OFF,
                    onClick = onToggleLayer,
                    width = 110.dp,
                    height = 28.dp
                )

                HardwareButton(
                    label = "LEFT (SPLIT)",
                    subLabel = if (isSplit) voiceLeft.name.take(9) else "OFF",
                    ledState = if (isSplit) ButtonLedState.ACTIVE_CYAN else ButtonLedState.OFF,
                    onClick = onToggleSplit,
                    width = 110.dp,
                    height = 28.dp
                )
            }

            // MULTI PADS (1, 2, 3, 4, STOP)
            Column {
                Text("MULTI PAD", color = ConsoleColors.TextSecondary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    MultiPadId.values().forEach { pad ->
                        val isTriggered = activeMultiPad == pad
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isTriggered) ConsoleColors.LedOrange else Color(0xFF1E2430))
                                .border(
                                    0.5.dp,
                                    if (isTriggered) Color.White else ConsoleColors.ChassisBorder,
                                    RoundedCornerShape(3.dp)
                                )
                                .clickable { onTriggerMultiPad(pad) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${pad.index}",
                                color = if (isTriggered) Color.Black else ConsoleColors.TextPrimary,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Stop Pad button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF2C1618))
                            .border(0.5.dp, ConsoleColors.LedRed, RoundedCornerShape(3.dp))
                            .clickable(onClick = onStopMultiPad),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("■", color = ConsoleColors.LedRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SUB-COLUMN B: Data Entry Wheel & Directional Buttons
        DataEntrySection(
            onRotate = onDataRotate,
            onEnter = {},
            onYes = { onDataRotate(+1) },
            onNo = { onDataRotate(-1) },
            onUp = { onDataRotate(+5) },
            onDown = { onDataRotate(-5) },
            onLeft = { onDataRotate(-1) },
            onRight = { onDataRotate(+1) }
        )
    }
}

@Composable
private fun RegistrationButtonsBar(
    currentBank: Int,
    activeSlot: Int?,
    keyboardTotalKeys: Int,
    onSelectBank: (Int) -> Unit,
    onSelectSlot: (Int) -> Unit,
    onSetTotalKeys: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF12151D))
            .border(1.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Bank +/- Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text("REG BANK:", color = ConsoleColors.TextSecondary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E2330))
                    .clickable { onSelectBank((currentBank - 1).coerceAtLeast(1)) },
                contentAlignment = Alignment.Center
            ) {
                Text("-", color = ConsoleColors.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "$currentBank",
                color = ConsoleColors.LedCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E2330))
                    .clickable { onSelectBank((currentBank + 1).coerceAtMost(10)) },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = ConsoleColors.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Memory Buttons 1 - 8
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..8).forEach { slot ->
                    val isCurrent = activeSlot == slot
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isCurrent) Color(0xFF1B3248) else Color(0xFF1A1E28))
                            .border(
                                1.dp,
                                if (isCurrent) ConsoleColors.LedCyan else ConsoleColors.ChassisBorder,
                                RoundedCornerShape(3.dp)
                            )
                            .clickable { onSelectSlot(slot) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrent) ConsoleColors.LedCyan else ConsoleColors.LedOff)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$slot",
                                color = if (isCurrent) ConsoleColors.LedCyan else ConsoleColors.TextPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Quick Key Range Selectors for phone playing
        val soundFeedback = LocalSoundFeedback.current
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text("KEYS:", color = ConsoleColors.TextSecondary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            listOf(25, 37, 49, 61).forEach { k ->
                val isSel = keyboardTotalKeys == k
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSel) ConsoleColors.LedCyan else Color(0xFF1E2330))
                        .clickable {
                            soundFeedback(SoundEffectType.BUTTON_CLICK)
                            onSetTotalKeys(k)
                        }
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${k}K",
                        color = if (isSel) Color.Black else ConsoleColors.TextSecondary,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceLeftStrip(
    pitchBend: Float,
    modulation: Float,
    isSustainOn: Boolean,
    transpose: Int,
    octaveShift: Int,
    upperOctaveShift: Int,
    onPitchBendChange: (Float) -> Unit,
    onModulationChange: (Float) -> Unit,
    onToggleSustain: () -> Unit,
    onShiftTranspose: (Int) -> Unit,
    onShiftOctave: (Int) -> Unit
) {
    val soundFeedback = LocalSoundFeedback.current
    Row(
        modifier = Modifier
            .width(135.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(ConsoleColors.ChassisPanel)
            .border(1.dp, ConsoleColors.ChassisBorder, RoundedCornerShape(4.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pitch Bend Wheel
        PitchBendWheel(
            pitchBend = pitchBend,
            onPitchBendChange = onPitchBendChange,
            modifier = Modifier.weight(1f)
        )

        // Modulation Wheel
        ModulationWheel(
            modulation = modulation,
            onModulationChange = onModulationChange,
            modifier = Modifier.weight(1f)
        )

        // Performance Buttons: Sustain, Transpose, Octave
        Column(
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sustain Button
            HardwareButton(
                label = "SUST",
                ledState = if (isSustainOn) ButtonLedState.ACTIVE_ORANGE else ButtonLedState.OFF,
                onClick = onToggleSustain,
                width = 42.dp,
                height = 24.dp
            )

            // Octave - / +
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E2430))
                        .clickable {
                            soundFeedback(SoundEffectType.BUTTON_CLICK)
                            onShiftOctave(-1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("O-", color = ConsoleColors.TextPrimary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E2430))
                        .clickable {
                            soundFeedback(SoundEffectType.BUTTON_CLICK)
                            onShiftOctave(+1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("O+", color = ConsoleColors.TextPrimary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Transpose - / +
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E2430))
                        .clickable {
                            soundFeedback(SoundEffectType.BUTTON_CLICK)
                            onShiftTranspose(-1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("T-", color = ConsoleColors.TextPrimary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E2430))
                        .clickable {
                            soundFeedback(SoundEffectType.BUTTON_CLICK)
                            onShiftTranspose(+1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("T+", color = ConsoleColors.TextPrimary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
