package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.arranger.ArrangerEngine
import com.example.arranger.ArrangerPlaybackState
import com.example.audio.AudioEngine
import com.example.data.ArrangerDatabase
import com.example.data.ArrangerRepository
import com.example.data.ConsoleSettingsEntity
import com.example.data.RecordedSongEntity
import com.example.data.RegistrationEntity
import com.example.model.*
import com.example.recorder.RecorderStatus
import com.example.recorder.SongRecorder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

enum class ConsoleDisplayScreen(val title: String) {
    HOME("Home"),
    VOICE("Voice"),
    STYLE("Style"),
    SONG("Song"),
    MIXER("Mixer"),
    EFFECTS("Effects"),
    REGISTRATION("Registration"),
    SETTINGS("Settings")
}

class ArrangerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ArrangerDatabase.getDatabase(application)
    val repository = ArrangerRepository(database.arrangerDao())

    val audioEngine = AudioEngine()
    val arrangerEngine = ArrangerEngine(audioEngine) { r1, r2 ->
        _voiceRight1.value = r1
        _voiceRight2.value = r2
        audioEngine.voiceRight1 = r1
        audioEngine.voiceRight2 = r2
    }
    val songRecorder = SongRecorder(audioEngine)

    // Touchscreen navigation
    private val _currentScreen = MutableStateFlow(ConsoleDisplayScreen.HOME)
    val currentScreen: StateFlow<ConsoleDisplayScreen> = _currentScreen.asStateFlow()

    // Voice parts
    private val _voiceRight1 = MutableStateFlow(VoicePresets.ALL_VOICES[0])
    val voiceRight1: StateFlow<Voice> = _voiceRight1.asStateFlow()

    private val _voiceRight2 = MutableStateFlow(VoicePresets.ALL_VOICES[8])
    val voiceRight2: StateFlow<Voice> = _voiceRight2.asStateFlow()

    private val _voiceLeft = MutableStateFlow(VoicePresets.ALL_VOICES[16])
    val voiceLeft: StateFlow<Voice> = _voiceLeft.asStateFlow()

    private val _isSplitEnabled = MutableStateFlow(true)
    val isSplitEnabled: StateFlow<Boolean> = _isSplitEnabled.asStateFlow()

    private val _isLayerEnabled = MutableStateFlow(false)
    val isLayerEnabled: StateFlow<Boolean> = _isLayerEnabled.asStateFlow()

    private val _splitPoint = MutableStateFlow(60) // Middle C (C3/C4 depending on octave standard)
    val splitPoint: StateFlow<Int> = _splitPoint.asStateFlow()

    // Keyboard pitch / tuning
    private val _transpose = MutableStateFlow(0) // -12 to +12
    val transpose: StateFlow<Int> = _transpose.asStateFlow()

    private val _octaveShift = MutableStateFlow(0) // -3 to +3
    val octaveShift: StateFlow<Int> = _octaveShift.asStateFlow()

    private val _upperOctaveShift = MutableStateFlow(0) // -2 to +2
    val upperOctaveShift: StateFlow<Int> = _upperOctaveShift.asStateFlow()

    // Performance controllers
    private val _isSustainOn = MutableStateFlow(false)
    val isSustainOn: StateFlow<Boolean> = _isSustainOn.asStateFlow()

    private val _pitchBend = MutableStateFlow(0.0f) // -2.0 to +2.0 semitones
    val pitchBend: StateFlow<Float> = _pitchBend.asStateFlow()

    private val _modulation = MutableStateFlow(0.0f) // 0.0 to 1.0
    val modulation: StateFlow<Float> = _modulation.asStateFlow()

    // Master Console Knobs
    private val _masterVolume = MutableStateFlow(1.0f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    private val _balance = MutableStateFlow(0.5f)
    val balance: StateFlow<Float> = _balance.asStateFlow()

    private val _micVolume = MutableStateFlow(0.85f)
    val micVolume: StateFlow<Float> = _micVolume.asStateFlow()

    // Mixer & Effects
    private val _mixerState = MutableStateFlow(MixerState())
    val mixerState: StateFlow<MixerState> = _mixerState.asStateFlow()

    private val _effectsState = MutableStateFlow(EffectsState())
    val effectsState: StateFlow<EffectsState> = _effectsState.asStateFlow()

    // Registrations
    private val _currentBank = MutableStateFlow(1)
    val currentBank: StateFlow<Int> = _currentBank.asStateFlow()

    private val _activeRegistrationSlot = MutableStateFlow<Int?>(1)
    val activeRegistrationSlot: StateFlow<Int?> = _activeRegistrationSlot.asStateFlow()

    val registrationsList: StateFlow<List<RegistrationEntity>> = repository.registrations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recordedSongsList: StateFlow<List<RecordedSongEntity>> = repository.recordedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active pressed notes on keyboard for visual feedback
    private val _pressedNotes = MutableStateFlow<Set<Int>>(emptySet())
    val pressedNotes: StateFlow<Set<Int>> = _pressedNotes.asStateFlow()

    // Chord detection keys held in split zone
    private val leftZoneNotesHeld = ConcurrentHashMap.newKeySet<Int>()

    // Settings
    private val _keyboardTotalKeys = MutableStateFlow(61) // 61, 76, 88
    val keyboardTotalKeys: StateFlow<Int> = _keyboardTotalKeys.asStateFlow()

    private val _showNoteLabels = MutableStateFlow(true)
    val showNoteLabels: StateFlow<Boolean> = _showNoteLabels.asStateFlow()

    private val _chordDetectMode = MutableStateFlow("FINGERED") // FINGERED, SINGLE_FINGER, FULL
    val chordDetectMode: StateFlow<String> = _chordDetectMode.asStateFlow()

    init {
        audioEngine.start()
        syncAudioEngineState()

        // Restore saved settings on startup if available
        viewModelScope.launch {
            repository.savedSettings.firstOrNull()?.let { saved ->
                _masterVolume.value = saved.masterVolume
                _balance.value = saved.balance
                _micVolume.value = saved.micVolume
                _transpose.value = saved.transpose
                _octaveShift.value = saved.octave
                _splitPoint.value = saved.splitPoint
                _isSplitEnabled.value = saved.isSplit
                _isLayerEnabled.value = saved.isLayer
                _voiceRight1.value = VoicePresets.getById(saved.voiceR1Id)
                _voiceRight2.value = VoicePresets.getById(saved.voiceR2Id)
                _voiceLeft.value = VoicePresets.getById(saved.voiceLeftId)
                arrangerEngine.setStyle(StylePresets.getById(saved.styleId))
                arrangerEngine.setTempo(saved.tempo)
                try {
                    _currentScreen.value = ConsoleDisplayScreen.valueOf(saved.activeScreen)
                } catch (_: Exception) {}
                syncAudioEngineState()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
        arrangerEngine.stop()
        songRecorder.stopAll()
    }

    private fun syncAudioEngineState() {
        audioEngine.masterVolume = _masterVolume.value
        audioEngine.balance = _balance.value
        audioEngine.pitchBendSemitones = _pitchBend.value
        audioEngine.modulationDepth = _modulation.value
        audioEngine.sustainPedal = _isSustainOn.value
        audioEngine.mixerState = _mixerState.value
        audioEngine.effectsState = _effectsState.value
        audioEngine.voiceRight1 = _voiceRight1.value
        audioEngine.voiceRight2 = _voiceRight2.value
        audioEngine.voiceLeft = _voiceLeft.value
    }

    // --- NAVIGATION ---

    fun navigateTo(screen: ConsoleDisplayScreen) {
        _currentScreen.value = screen
    }

    // --- VOICE CONTROLS ---

    fun setVoiceRight1(voice: Voice) {
        _voiceRight1.value = voice
        audioEngine.voiceRight1 = voice
        saveConsoleSettings()
    }

    fun setVoiceRight2(voice: Voice) {
        _voiceRight2.value = voice
        audioEngine.voiceRight2 = voice
        saveConsoleSettings()
    }

    fun setVoiceLeft(voice: Voice) {
        _voiceLeft.value = voice
        audioEngine.voiceLeft = voice
        saveConsoleSettings()
    }

    fun toggleSplit() {
        _isSplitEnabled.value = !_isSplitEnabled.value
        saveConsoleSettings()
    }

    fun toggleLayer() {
        _isLayerEnabled.value = !_isLayerEnabled.value
        saveConsoleSettings()
    }

    fun setSplitPoint(point: Int) {
        _splitPoint.value = point.coerceIn(36, 84)
        saveConsoleSettings()
    }

    // --- PERFORMANCE CONTROLS ---

    fun setTranspose(semitones: Int) {
        _transpose.value = semitones.coerceIn(-12, 12)
        saveConsoleSettings()
    }

    fun shiftTranspose(delta: Int) {
        setTranspose(_transpose.value + delta)
    }

    fun setOctaveShift(octaves: Int) {
        _octaveShift.value = octaves.coerceIn(-3, 3)
        saveConsoleSettings()
    }

    fun shiftOctave(delta: Int) {
        setOctaveShift(_octaveShift.value + delta)
    }

    fun setUpperOctaveShift(octaves: Int) {
        _upperOctaveShift.value = octaves.coerceIn(-2, 2)
    }

    fun toggleSustain() {
        val newState = !_isSustainOn.value
        _isSustainOn.value = newState
        audioEngine.setSustain(newState)
    }

    fun setPitchBend(value: Float) {
        _pitchBend.value = value.coerceIn(-2.0f, 2.0f)
        audioEngine.pitchBendSemitones = _pitchBend.value
    }

    fun setModulation(value: Float) {
        _modulation.value = value.coerceIn(0.0f, 1.0f)
        audioEngine.modulationDepth = _modulation.value
    }

    // --- MASTER KNOBS ---

    fun setMasterVolume(vol: Float) {
        _masterVolume.value = vol.coerceIn(0.0f, 1.0f)
        audioEngine.masterVolume = _masterVolume.value
    }

    fun setBalance(bal: Float) {
        _balance.value = bal.coerceIn(0.0f, 1.0f)
        audioEngine.balance = _balance.value
    }

    fun setMicVolume(vol: Float) {
        _micVolume.value = vol.coerceIn(0.0f, 1.0f)
    }

    // --- MIXER & EFFECTS ---

    fun updateMixerChannelVolume(channelId: ChannelId, volume: Float) {
        val current = _mixerState.value
        val channel = current.channels[channelId] ?: ChannelStrip(channelId)
        val updated = channel.copy(volume = volume.coerceIn(0f, 1f))
        val newMap = current.channels.toMutableMap().apply { put(channelId, updated) }
        _mixerState.value = current.copy(channels = newMap)
        audioEngine.mixerState = _mixerState.value
    }

    fun updateMixerChannelPan(channelId: ChannelId, pan: Float) {
        val current = _mixerState.value
        val channel = current.channels[channelId] ?: ChannelStrip(channelId)
        val updated = channel.copy(pan = pan.coerceIn(-1f, 1f))
        val newMap = current.channels.toMutableMap().apply { put(channelId, updated) }
        _mixerState.value = current.copy(channels = newMap)
        audioEngine.mixerState = _mixerState.value
    }

    fun toggleMixerChannelMute(channelId: ChannelId) {
        val current = _mixerState.value
        val channel = current.channels[channelId] ?: ChannelStrip(channelId)
        val updated = channel.copy(isMuted = !channel.isMuted)
        val newMap = current.channels.toMutableMap().apply { put(channelId, updated) }
        _mixerState.value = current.copy(channels = newMap)
        audioEngine.mixerState = _mixerState.value
    }

    fun toggleMixerChannelSolo(channelId: ChannelId) {
        val current = _mixerState.value
        val channel = current.channels[channelId] ?: ChannelStrip(channelId)
        val updated = channel.copy(isSolo = !channel.isSolo)
        val newMap = current.channels.toMutableMap().apply { put(channelId, updated) }
        _mixerState.value = current.copy(channels = newMap)
        audioEngine.mixerState = _mixerState.value
    }

    fun updateEffects(update: (EffectsState) -> EffectsState) {
        val newState = update(_effectsState.value)
        _effectsState.value = newState
        audioEngine.effectsState = newState
    }

    fun playUiSound(type: SoundEffectType, volume: Float = 1.0f) {
        audioEngine.playUiSound(type, volume)
    }

    // --- KEYBOARD TOUCH HANDLING ---

    fun onKeyTouchDown(rawMidiNote: Int, velocity: Float = 1.0f) {
        val effectiveNote = rawMidiNote + _transpose.value + (_octaveShift.value * 12)

        _pressedNotes.value = _pressedNotes.value + effectiveNote
        arrangerEngine.onKeyboardKeyTouched(effectiveNote)

        val split = _isSplitEnabled.value
        val splitPt = _splitPoint.value

        if (split && effectiveNote < splitPt) {
            // Left part
            audioEngine.noteOn(effectiveNote, velocity, KeyboardPart.LEFT)
            songRecorder.recordNoteEvent(effectiveNote, velocity, isNoteOn = true, KeyboardPart.LEFT)

            leftZoneNotesHeld.add(effectiveNote)
            val detected = ChordDetector.detectChord(leftZoneNotesHeld, _chordDetectMode.value == "SINGLE_FINGER")
            if (detected != null) {
                arrangerEngine.setChord(detected)
            }
        } else {
            // Right part (Right 1, and Right 2 if Layer is ON)
            val rNote = effectiveNote + (_upperOctaveShift.value * 12)
            audioEngine.noteOn(rNote, velocity, KeyboardPart.RIGHT1)
            songRecorder.recordNoteEvent(rNote, velocity, isNoteOn = true, KeyboardPart.RIGHT1)

            if (_isLayerEnabled.value) {
                audioEngine.noteOn(rNote, velocity * 0.90f, KeyboardPart.RIGHT2)
                songRecorder.recordNoteEvent(rNote, velocity * 0.90f, isNoteOn = true, KeyboardPart.RIGHT2)
            }

            if (!split) {
                // If split is off, all notes can also inform chord detection if in FULL mode
                if (_chordDetectMode.value == "FULL") {
                    leftZoneNotesHeld.add(effectiveNote)
                    val detected = ChordDetector.detectChord(leftZoneNotesHeld, false)
                    if (detected != null) {
                        arrangerEngine.setChord(detected)
                    }
                }
            }
        }
    }

    fun onKeyTouchUp(rawMidiNote: Int) {
        val effectiveNote = rawMidiNote + _transpose.value + (_octaveShift.value * 12)
        _pressedNotes.value = _pressedNotes.value - effectiveNote

        val split = _isSplitEnabled.value
        val splitPt = _splitPoint.value

        if (split && effectiveNote < splitPt) {
            audioEngine.noteOff(effectiveNote, KeyboardPart.LEFT)
            songRecorder.recordNoteEvent(effectiveNote, 0f, isNoteOn = false, KeyboardPart.LEFT)
            leftZoneNotesHeld.remove(effectiveNote)
        } else {
            val rNote = effectiveNote + (_upperOctaveShift.value * 12)
            audioEngine.noteOff(rNote, KeyboardPart.RIGHT1)
            songRecorder.recordNoteEvent(rNote, 0f, isNoteOn = false, KeyboardPart.RIGHT1)

            if (_isLayerEnabled.value) {
                audioEngine.noteOff(rNote, KeyboardPart.RIGHT2)
                songRecorder.recordNoteEvent(rNote, 0f, isNoteOn = false, KeyboardPart.RIGHT2)
            }

            if (!split && _chordDetectMode.value == "FULL") {
                leftZoneNotesHeld.remove(effectiveNote)
            }
        }
    }

    // --- REGISTRATIONS ---

    fun setBank(bank: Int) {
        _currentBank.value = bank.coerceIn(1, 10)
        playUiSound(SoundEffectType.BUTTON_CLICK)
    }

    fun selectRegistrationSlot(slot: Int) {
        _activeRegistrationSlot.value = slot
        playUiSound(SoundEffectType.BUTTON_BEEP)
        val bank = _currentBank.value
        val targetId = "B${bank}_S$slot"
        val reg = registrationsList.value.firstOrNull { it.id == targetId }
        if (reg != null) {
            loadRegistration(reg)
        }
    }

    fun saveCurrentToRegistration(slot: Int, name: String) {
        playUiSound(SoundEffectType.SUCCESS_CHIME)
        val bank = _currentBank.value
        val entity = RegistrationEntity(
            id = "B${bank}_S$slot",
            bankNumber = bank,
            slotNumber = slot,
            name = name.ifBlank { "Reg $bank-$slot" },
            styleId = arrangerEngine.currentStyle.value.id,
            tempo = arrangerEngine.tempo.value,
            voiceRight1Id = _voiceRight1.value.id,
            voiceRight2Id = _voiceRight2.value.id,
            voiceLeftId = _voiceLeft.value.id,
            isSplit = _isSplitEnabled.value,
            isLayer = _isLayerEnabled.value,
            splitPoint = _splitPoint.value,
            transpose = _transpose.value,
            octave = _octaveShift.value,
            masterVolume = _masterVolume.value,
            balance = _balance.value,
            reverbSend = _effectsState.value.reverb.sendLevel,
            reverbRoom = _effectsState.value.reverb.roomSize,
            chorusEnabled = _effectsState.value.chorus.enabled,
            delayEnabled = _effectsState.value.delay.enabled
        )
        viewModelScope.launch {
            repository.saveRegistration(entity)
            _activeRegistrationSlot.value = slot
        }
    }

    private fun loadRegistration(reg: RegistrationEntity) {
        _voiceRight1.value = VoicePresets.getById(reg.voiceRight1Id)
        _voiceRight2.value = VoicePresets.getById(reg.voiceRight2Id)
        _voiceLeft.value = VoicePresets.getById(reg.voiceLeftId)
        _isSplitEnabled.value = reg.isSplit
        _isLayerEnabled.value = reg.isLayer
        _splitPoint.value = reg.splitPoint
        _transpose.value = reg.transpose
        _octaveShift.value = reg.octave
        _masterVolume.value = reg.masterVolume
        _balance.value = reg.balance

        val currentEffects = _effectsState.value
        _effectsState.value = currentEffects.copy(
            reverb = currentEffects.reverb.copy(sendLevel = reg.reverbSend, roomSize = reg.reverbRoom),
            chorus = currentEffects.chorus.copy(enabled = reg.chorusEnabled),
            delay = currentEffects.delay.copy(enabled = reg.delayEnabled)
        )

        arrangerEngine.setStyle(StylePresets.getById(reg.styleId))
        arrangerEngine.setTempo(reg.tempo)

        syncAudioEngineState()
        saveConsoleSettings()
    }

    // --- SONG RECORDER ---

    fun toggleRecording() {
        playUiSound(SoundEffectType.BUTTON_HEAVY)
        if (songRecorder.status.value == RecorderStatus.RECORDING) {
            val song = songRecorder.stopRecording(arrangerEngine.tempo.value, arrangerEngine.currentStyle.value.id)
            if (song != null) {
                viewModelScope.launch {
                    repository.saveSong(song)
                }
            }
        } else {
            songRecorder.startRecording()
        }
    }

    fun playRecordedSong(entity: RecordedSongEntity) {
        playUiSound(SoundEffectType.BUTTON_HEAVY)
        val song = repository.parseSongEntity(entity)
        songRecorder.playSong(song)
    }

    fun deleteRecordedSong(id: String) {
        playUiSound(SoundEffectType.BUTTON_CLICK)
        viewModelScope.launch {
            repository.deleteSong(id)
        }
    }

    // --- DATA ENTRY WHEEL & BUTTONS ---

    fun onDataDialRotated(delta: Int) {
        playUiSound(SoundEffectType.DIAL_TICK)
        // Context-aware value tweak based on current screen
        when (_currentScreen.value) {
            ConsoleDisplayScreen.HOME, ConsoleDisplayScreen.STYLE -> {
                arrangerEngine.setTempo(arrangerEngine.tempo.value + delta)
            }
            ConsoleDisplayScreen.VOICE -> {
                val allVoices = VoicePresets.ALL_VOICES
                val currentIdx = allVoices.indexOfFirst { it.id == _voiceRight1.value.id }
                val newIdx = (currentIdx + delta).coerceIn(0, allVoices.size - 1)
                setVoiceRight1(allVoices[newIdx])
            }
            ConsoleDisplayScreen.MIXER -> {
                setMasterVolume(_masterVolume.value + (delta * 0.02f))
            }
            ConsoleDisplayScreen.REGISTRATION -> {
                setBank(_currentBank.value + delta)
            }
            ConsoleDisplayScreen.SETTINGS -> {
                setTranspose(_transpose.value + delta)
            }
            else -> {
                arrangerEngine.setTempo(arrangerEngine.tempo.value + delta)
            }
        }
    }

    fun setKeyboardTotalKeys(keys: Int) {
        _keyboardTotalKeys.value = keys
    }

    fun toggleNoteLabels() {
        _showNoteLabels.value = !_showNoteLabels.value
    }

    fun setChordDetectMode(mode: String) {
        _chordDetectMode.value = mode
    }

    private fun saveConsoleSettings() {
        viewModelScope.launch {
            val settings = ConsoleSettingsEntity(
                styleId = arrangerEngine.currentStyle.value.id,
                tempo = arrangerEngine.tempo.value,
                voiceR1Id = _voiceRight1.value.id,
                voiceR2Id = _voiceRight2.value.id,
                voiceLeftId = _voiceLeft.value.id,
                isSplit = _isSplitEnabled.value,
                isLayer = _isLayerEnabled.value,
                splitPoint = _splitPoint.value,
                transpose = _transpose.value,
                octave = _octaveShift.value,
                masterVolume = _masterVolume.value,
                balance = _balance.value,
                micVolume = _micVolume.value,
                activeScreen = _currentScreen.value.name
            )
            repository.saveSettings(settings)
        }
    }
}
