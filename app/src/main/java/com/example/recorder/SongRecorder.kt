package com.example.recorder

import com.example.audio.AudioEngine
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class RecorderStatus {
    STOPPED,
    RECORDING,
    PLAYING,
    PAUSED
}

class SongRecorder(
    private val audioEngine: AudioEngine
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var playbackJob: Job? = null

    private val _status = MutableStateFlow(RecorderStatus.STOPPED)
    val status: StateFlow<RecorderStatus> = _status.asStateFlow()

    private val _recordedDurationMs = MutableStateFlow(0L)
    val recordedDurationMs: StateFlow<Long> = _recordedDurationMs.asStateFlow()

    private val _currentPlayheadMs = MutableStateFlow(0L)
    val currentPlayheadMs: StateFlow<Long> = _currentPlayheadMs.asStateFlow()

    private val currentEvents = mutableListOf<RecordedNoteEvent>()
    private var recordingStartTime = 0L
    private var currentSongToPlay: SongRecording? = null
    private var playbackStartTime = 0L
    private var pauseOffsetMs = 0L

    fun startRecording() {
        if (_status.value == RecorderStatus.PLAYING) stopPlayback()
        currentEvents.clear()
        recordingStartTime = System.currentTimeMillis()
        _status.value = RecorderStatus.RECORDING
        _recordedDurationMs.value = 0L

        scope.launch {
            while (_status.value == RecorderStatus.RECORDING) {
                _recordedDurationMs.value = System.currentTimeMillis() - recordingStartTime
                delay(100)
            }
        }
    }

    fun recordNoteEvent(note: Int, velocity: Float, isNoteOn: Boolean, part: KeyboardPart) {
        if (_status.value != RecorderStatus.RECORDING) return
        val timestamp = System.currentTimeMillis() - recordingStartTime
        synchronized(currentEvents) {
            currentEvents.add(RecordedNoteEvent(timestamp, note, velocity, isNoteOn, part))
        }
    }

    fun stopRecording(tempo: Int = 120, styleId: String = "pop_8beat"): SongRecording? {
        if (_status.value != RecorderStatus.RECORDING) return null
        val duration = System.currentTimeMillis() - recordingStartTime
        _status.value = RecorderStatus.STOPPED
        _recordedDurationMs.value = duration

        if (currentEvents.isEmpty()) return null

        val song = SongRecording(
            id = UUID.randomUUID().toString(),
            title = "Recording ${System.currentTimeMillis() % 10000}",
            timestamp = System.currentTimeMillis(),
            durationMs = duration,
            tempo = tempo,
            styleId = styleId,
            events = synchronized(currentEvents) { currentEvents.toList() }
        )
        currentSongToPlay = song
        return song
    }

    fun playSong(song: SongRecording) {
        stopAll()
        currentSongToPlay = song
        _status.value = RecorderStatus.PLAYING
        playbackStartTime = System.currentTimeMillis()
        pauseOffsetMs = 0L

        playbackJob = scope.launch {
            val events = song.events.sortedBy { it.timestampMs }
            var eventIndex = 0

            while (isActive && _status.value == RecorderStatus.PLAYING) {
                val elapsed = (System.currentTimeMillis() - playbackStartTime) + pauseOffsetMs
                _currentPlayheadMs.value = elapsed

                while (eventIndex < events.size && events[eventIndex].timestampMs <= elapsed) {
                    val ev = events[eventIndex]
                    if (ev.isNoteOn) {
                        audioEngine.noteOn(ev.note, ev.velocity, ev.part)
                    } else {
                        audioEngine.noteOff(ev.note, ev.part)
                    }
                    eventIndex++
                }

                if (elapsed >= song.durationMs && eventIndex >= events.size) {
                    break
                }

                delay(10)
            }

            stopPlayback()
        }
    }

    fun pausePlayback() {
        if (_status.value == RecorderStatus.PLAYING) {
            playbackJob?.cancel()
            pauseOffsetMs = _currentPlayheadMs.value
            _status.value = RecorderStatus.PAUSED
            audioEngine.allNotesOff()
        }
    }

    fun resumePlayback() {
        val song = currentSongToPlay ?: return
        if (_status.value == RecorderStatus.PAUSED) {
            _status.value = RecorderStatus.PLAYING
            playbackStartTime = System.currentTimeMillis()

            playbackJob = scope.launch {
                val events = song.events.sortedBy { it.timestampMs }
                var eventIndex = events.indexOfFirst { it.timestampMs >= pauseOffsetMs }.coerceAtLeast(0)

                while (isActive && _status.value == RecorderStatus.PLAYING) {
                    val elapsed = (System.currentTimeMillis() - playbackStartTime) + pauseOffsetMs
                    _currentPlayheadMs.value = elapsed

                    while (eventIndex < events.size && events[eventIndex].timestampMs <= elapsed) {
                        val ev = events[eventIndex]
                        if (ev.isNoteOn) {
                            audioEngine.noteOn(ev.note, ev.velocity, ev.part)
                        } else {
                            audioEngine.noteOff(ev.note, ev.part)
                        }
                        eventIndex++
                    }

                    if (elapsed >= song.durationMs && eventIndex >= events.size) {
                        break
                    }

                    delay(10)
                }

                stopPlayback()
            }
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _status.value = RecorderStatus.STOPPED
        _currentPlayheadMs.value = 0L
        pauseOffsetMs = 0L
        audioEngine.allNotesOff()
    }

    fun stopAll() {
        if (_status.value == RecorderStatus.RECORDING) {
            _status.value = RecorderStatus.STOPPED
        }
        stopPlayback()
    }
}
