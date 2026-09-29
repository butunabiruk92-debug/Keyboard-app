package com.example.arranger

import com.example.audio.AudioEngine
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

enum class ArrangerPlaybackState {
    STOPPED,
    SYNC_START_ARMED,
    PLAYING
}

class ArrangerEngine(
    private val audioEngine: AudioEngine,
    private val onOtsChange: ((voiceR1: Voice, voiceR2: Voice) -> Unit)? = null
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var clockJob: Job? = null

    // State flows
    private val _playbackState = MutableStateFlow(ArrangerPlaybackState.STOPPED)
    val playbackState: StateFlow<ArrangerPlaybackState> = _playbackState.asStateFlow()

    private val _currentStyle = MutableStateFlow(StylePresets.ALL_STYLES[0])
    val currentStyle: StateFlow<Style> = _currentStyle.asStateFlow()

    private val _currentSection = MutableStateFlow(StyleSection.MAIN_A)
    val currentSection: StateFlow<StyleSection> = _currentSection.asStateFlow()

    private val _tempo = MutableStateFlow(118)
    val tempo: StateFlow<Int> = _tempo.asStateFlow()

    private val _currentMeasure = MutableStateFlow(1)
    val currentMeasure: StateFlow<Int> = _currentMeasure.asStateFlow()

    private val _currentBeat = MutableStateFlow(1)
    val currentBeat: StateFlow<Int> = _currentBeat.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _detectedChord = MutableStateFlow(ChordInfo.DEFAULT)
    val detectedChord: StateFlow<ChordInfo> = _detectedChord.asStateFlow()

    private val _isAcmpEnabled = MutableStateFlow(true)
    val isAcmpEnabled: StateFlow<Boolean> = _isAcmpEnabled.asStateFlow()

    private val _isAutoFillEnabled = MutableStateFlow(true)
    val isAutoFillEnabled: StateFlow<Boolean> = _isAutoFillEnabled.asStateFlow()

    private val _isOtsLinkEnabled = MutableStateFlow(true)
    val isOtsLinkEnabled: StateFlow<Boolean> = _isOtsLinkEnabled.asStateFlow()

    private val _isMetronomeEnabled = MutableStateFlow(false)
    val isMetronomeEnabled: StateFlow<Boolean> = _isMetronomeEnabled.asStateFlow()

    private val _activeMultiPad = MutableStateFlow<MultiPadId?>(null)
    val activeMultiPad: StateFlow<MultiPadId?> = _activeMultiPad.asStateFlow()

    // Internal navigation targets
    private var queuedSection: StyleSection? = null
    private var returnAfterFillSection: StyleSection = StyleSection.MAIN_A
    private var isBreakOneShot = false
    private var multiPadStepCounter = 0
    private var metronomeJob: Job? = null

    // Voice assignments for accompaniment tracks
    private val acmpBassVoice = VoicePresets.getById("bass_electric")
    private val acmpChord1Voice = VoicePresets.getById("grand_concert")
    private val acmpChord2Voice = VoicePresets.getById("ep_rhodes")
    private val acmpPadVoice = VoicePresets.getById("pad_warm")

    fun setStyle(style: Style) {
        _currentStyle.value = style
        _tempo.value = style.tempo
        _currentSection.value = StyleSection.MAIN_A
        applyOts(StyleSection.MAIN_A)
    }

    fun setTempo(newTempo: Int) {
        _tempo.value = newTempo.coerceIn(40, 280)
    }

    fun tapTempo() {
        audioEngine.playUiSound(SoundEffectType.METRONOME_HIGH, 1.0f)
        val now = System.currentTimeMillis()
        synchronized(tapTimes) {
            tapTimes.add(now)
            if (tapTimes.size > 4) tapTimes.removeAt(0)
            if (tapTimes.size >= 2) {
                var totalDiff = 0L
                for (i in 1 until tapTimes.size) {
                    totalDiff += (tapTimes[i] - tapTimes[i - 1])
                }
                val avgMs = totalDiff / (tapTimes.size - 1)
                if (avgMs in 200..2000) {
                    val calcBpm = (60000L / avgMs).toInt()
                    setTempo(calcBpm)
                }
            }
        }
    }
    private val tapTimes = mutableListOf<Long>()

    fun setChord(chord: ChordInfo) {
        _detectedChord.value = chord
    }

    fun toggleAcmp() {
        _isAcmpEnabled.value = !_isAcmpEnabled.value
        audioEngine.playUiSound(SoundEffectType.BUTTON_CLICK)
    }

    fun toggleAutoFill() {
        _isAutoFillEnabled.value = !_isAutoFillEnabled.value
        audioEngine.playUiSound(SoundEffectType.BUTTON_CLICK)
    }

    fun toggleOtsLink() {
        _isOtsLinkEnabled.value = !_isOtsLinkEnabled.value
        audioEngine.playUiSound(SoundEffectType.BUTTON_CLICK)
    }

    fun toggleMetronome() {
        val newState = !_isMetronomeEnabled.value
        _isMetronomeEnabled.value = newState
        audioEngine.playUiSound(if (newState) SoundEffectType.METRONOME_HIGH else SoundEffectType.BUTTON_HEAVY)
        if (newState && _playbackState.value != ArrangerPlaybackState.PLAYING) {
            startStandaloneMetronome()
        } else if (!newState) {
            metronomeJob?.cancel()
            metronomeJob = null
        }
    }

    private fun startStandaloneMetronome() {
        metronomeJob?.cancel()
        metronomeJob = coroutineScope.launch {
            var b = 1
            while (isActive && _isMetronomeEnabled.value && _playbackState.value != ArrangerPlaybackState.PLAYING) {
                _currentBeat.value = b
                if (b == 1) {
                    audioEngine.playUiSound(SoundEffectType.METRONOME_HIGH, 1.0f)
                } else {
                    audioEngine.playUiSound(SoundEffectType.METRONOME_LOW, 0.9f)
                }
                val intervalMs = (60000L / _tempo.value.coerceAtLeast(30))
                delay(intervalMs)
                b = if (b >= 4) 1 else b + 1
            }
        }
    }

    fun armSyncStart() {
        audioEngine.playUiSound(SoundEffectType.BUTTON_HEAVY)
        if (_playbackState.value == ArrangerPlaybackState.PLAYING) {
            stop()
        }
        _playbackState.value = ArrangerPlaybackState.SYNC_START_ARMED
    }

    fun armSyncStop() {
        audioEngine.playUiSound(SoundEffectType.BUTTON_HEAVY)
        if (_playbackState.value == ArrangerPlaybackState.PLAYING) {
            stop()
        }
    }

    fun startOrStop() {
        audioEngine.playUiSound(SoundEffectType.BUTTON_HEAVY)
        if (_playbackState.value == ArrangerPlaybackState.PLAYING) {
            stop()
        } else {
            start()
        }
    }

    fun start(startSection: StyleSection? = null) {
        metronomeJob?.cancel()
        metronomeJob = null
        clockJob?.cancel()
        if (startSection != null) {
            _currentSection.value = startSection
        }
        _playbackState.value = ArrangerPlaybackState.PLAYING
        _currentMeasure.value = 1
        _currentBeat.value = 1
        _currentStep.value = 0
        multiPadStepCounter = 0
        startClock()
    }

    fun stop() {
        clockJob?.cancel()
        clockJob = null
        _playbackState.value = ArrangerPlaybackState.STOPPED
        _currentStep.value = 0
        _currentBeat.value = 1
        _activeMultiPad.value = null
        if (_isMetronomeEnabled.value) {
            startStandaloneMetronome()
        }
    }

    fun selectSection(section: StyleSection) {
        audioEngine.playUiSound(SoundEffectType.BUTTON_CLICK)
        if (_playbackState.value != ArrangerPlaybackState.PLAYING) {
            _currentSection.value = section
            applyOts(section)
            return
        }

        if (section == _currentSection.value && (section == StyleSection.MAIN_A || section == StyleSection.MAIN_B || section == StyleSection.MAIN_C || section == StyleSection.MAIN_D)) {
            // Trigger fill-in within current variation
            triggerFillIn(section)
            return
        }

        if (_isAutoFillEnabled.value && (section == StyleSection.MAIN_A || section == StyleSection.MAIN_B || section == StyleSection.MAIN_C || section == StyleSection.MAIN_D)) {
            triggerFillIn(section)
        } else {
            queuedSection = section
            applyOts(section)
        }
    }

    fun triggerFillIn(targetSectionAfterFill: StyleSection? = null) {
        audioEngine.playUiSound(SoundEffectType.BUTTON_CLICK)
        returnAfterFillSection = targetSectionAfterFill ?: _currentSection.value
        _currentSection.value = StyleSection.FILL_IN
    }

    fun triggerBreak() {
        audioEngine.playUiSound(SoundEffectType.BUTTON_CLICK)
        isBreakOneShot = true
        returnAfterFillSection = _currentSection.value
        _currentSection.value = StyleSection.BREAK
    }

    fun triggerEnding() {
        audioEngine.playUiSound(SoundEffectType.BUTTON_CLICK)
        _currentSection.value = StyleSection.ENDING
    }

    fun triggerMultiPad(padId: MultiPadId) {
        _activeMultiPad.value = padId
        multiPadStepCounter = 0
        val fx = when (padId) {
            MultiPadId.PAD_1 -> SoundEffectType.PAD_FX_1
            MultiPadId.PAD_2 -> SoundEffectType.PAD_FX_2
            MultiPadId.PAD_3 -> SoundEffectType.PAD_FX_3
            MultiPadId.PAD_4 -> SoundEffectType.PAD_FX_4
        }
        audioEngine.playUiSound(fx, 1.0f)
    }

    fun stopMultiPad() {
        _activeMultiPad.value = null
        audioEngine.playUiSound(SoundEffectType.BUTTON_HEAVY)
    }

    fun onKeyboardKeyTouched(note: Int) {
        if (_playbackState.value == ArrangerPlaybackState.SYNC_START_ARMED) {
            start()
        }
    }

    private fun applyOts(section: StyleSection) {
        if (!_isOtsLinkEnabled.value) return
        val r1: Voice
        val r2: Voice

        when (section) {
            StyleSection.MAIN_A -> {
                r1 = VoicePresets.getById("grand_concert")
                r2 = VoicePresets.getById("strings_symphony")
            }
            StyleSection.MAIN_B -> {
                r1 = VoicePresets.getById("ep_rhodes")
                r2 = VoicePresets.getById("pad_warm")
            }
            StyleSection.MAIN_C -> {
                r1 = VoicePresets.getById("organ_jazz")
                r2 = VoicePresets.getById("brass_section")
            }
            StyleSection.MAIN_D, StyleSection.FILL_IN, StyleSection.BREAK, StyleSection.ENDING, StyleSection.INTRO -> {
                r1 = VoicePresets.getById("synth_saw_lead")
                r2 = VoicePresets.getById("pad_shimmer")
            }
        }

        onOtsChange?.invoke(r1, r2)
    }

    private fun startClock() {
        clockJob = coroutineScope.launch {
            var stepIndex = 0

            while (isActive && _playbackState.value == ArrangerPlaybackState.PLAYING) {
                val currentBpm = _tempo.value
                val stepIntervalMs = (60000L / currentBpm) / 4L

                val beat = (stepIndex / 4) + 1
                _currentStep.value = stepIndex
                _currentBeat.value = beat

                // Metronome click on beat 1 (high pitch) and beats 2-4 (mid pitch)
                if (_isMetronomeEnabled.value && stepIndex % 4 == 0) {
                    if (beat == 1) {
                        audioEngine.playUiSound(SoundEffectType.METRONOME_HIGH, 1.0f)
                    } else {
                        audioEngine.playUiSound(SoundEffectType.METRONOME_LOW, 0.9f)
                    }
                }

                // Execute arranger pattern step
                executePatternStep(stepIndex)

                // Execute active MultiPad step
                executeMultiPadStep()

                delay(stepIntervalMs)

                stepIndex++
                if (stepIndex >= 16) {
                    stepIndex = 0
                    _currentMeasure.value = _currentMeasure.value + 1

                    // Measure boundary: handle queued section transitions
                    if (_currentSection.value == StyleSection.FILL_IN || _currentSection.value == StyleSection.BREAK) {
                        _currentSection.value = returnAfterFillSection
                        applyOts(returnAfterFillSection)
                    } else if (_currentSection.value == StyleSection.ENDING) {
                        stop()
                        break
                    } else if (queuedSection != null) {
                        _currentSection.value = queuedSection!!
                        queuedSection = null
                    }
                }
            }
        }
    }

    private fun executePatternStep(step: Int) {
        val style = _currentStyle.value
        val section = _currentSection.value
        val patternData = style.sections[section] ?: style.sections[StyleSection.MAIN_A] ?: return

        // 1. Drums
        val drumHits = patternData.drumTriggers[step]
        if (drumHits != null) {
            for (hit in drumHits) {
                audioEngine.triggerDrum(hit.sound, hit.velocity)
            }
        }

        // Accompaniment tracks (Bass, Chords, Pad) only play if ACMP is enabled
        if (!_isAcmpEnabled.value) return

        val chord = _detectedChord.value
        val rootNote = chord.rootNote
        val stepDurationMs = ((60000f / _tempo.value) / 4f)

        // 2. Bass Track
        val bassTrigger = patternData.bassTriggers[step]
        if (bassTrigger != null) {
            val bassBaseMidi = 36 // C2
            val bassNote = bassBaseMidi + rootNote + bassTrigger.intervalFromRoot + (bassTrigger.octaveOffset * 12)
            val duration = bassTrigger.durationSteps * stepDurationMs
            audioEngine.triggerArrangerNote(bassNote, bassTrigger.velocity, acmpBassVoice, ChannelId.BASS, duration)
        }

        // 3. Chord 1 Track (Comping)
        val chord1Trigger = patternData.chord1Triggers[step]
        if (chord1Trigger != null) {
            val voicing = chord.getVoicingNotes(baseOctave = 3)
            val duration = chord1Trigger.durationSteps * stepDurationMs
            for (note in voicing) {
                audioEngine.triggerArrangerNote(note, chord1Trigger.velocity, acmpChord1Voice, ChannelId.CHORD1, duration)
            }
        }

        // 4. Chord 2 Track (Arpeggio / Countermelody)
        val chord2Trigger = patternData.chord2Triggers[step]
        if (chord2Trigger != null) {
            val voicing = chord.getVoicingNotes(baseOctave = 4)
            val noteIndex = (step / 2) % voicing.size
            val note = voicing[noteIndex]
            val duration = chord2Trigger.durationSteps * stepDurationMs
            audioEngine.triggerArrangerNote(note, chord2Trigger.velocity, acmpChord2Voice, ChannelId.CHORD2, duration)
        }

        // 5. Pad Track (Sustained Harmony)
        val padTrigger = patternData.padTriggers[step]
        if (padTrigger != null) {
            val voicing = chord.getVoicingNotes(baseOctave = 4)
            val duration = padTrigger.durationSteps * stepDurationMs
            for (note in voicing) {
                audioEngine.triggerArrangerNote(note, padTrigger.velocity, acmpPadVoice, ChannelId.PAD, duration)
            }
        }
    }

    private fun executeMultiPadStep() {
        val padId = _activeMultiPad.value ?: return
        val phrase = MultiPadPresets.PHRASES[padId] ?: return
        val chord = _detectedChord.value
        val stepDurationMs = ((60000f / _tempo.value) / 4f)

        val matchingNotes = phrase.notes.filter { it.first == multiPadStepCounter }
        val voice = VoicePresets.getById(phrase.soundVoiceId)

        for (match in matchingNotes) {
            val noteOffset = match.second
            val midiNote = if (phrase.soundVoiceId == "drum_standard") {
                noteOffset
            } else {
                60 + chord.rootNote + noteOffset
            }

            if (phrase.soundVoiceId == "drum_standard") {
                val sound = DrumSound.values().firstOrNull { it.midiNote == midiNote } ?: DrumSound.SNARE
                audioEngine.triggerDrum(sound, 0.85f)
            } else {
                audioEngine.triggerArrangerNote(midiNote, 0.8f, voice, ChannelId.PHRASE1, stepDurationMs * 2)
            }
        }

        multiPadStepCounter++
        if (multiPadStepCounter >= 16) {
            multiPadStepCounter = 0
            _activeMultiPad.value = null // Stop after 1 phrase
        }
    }
}
