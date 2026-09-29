package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import com.example.model.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.*

class AudioEngine {
    companion object {
        const val SAMPLE_RATE = 44100
        const val MAX_VOICES = 32
        const val DRUM_CHANNEL_ID = 9
    }

    private var audioTrack: AudioTrack? = null
    private var renderThread: Thread? = null
    private val isRunning = AtomicBoolean(false)

    // Master & Performance controls
    @Volatile var masterVolume: Float = 1.0f
    @Volatile var balance: Float = 0.5f // 0.0 = all style, 1.0 = all keyboard
    @Volatile var pitchBendSemitones: Float = 0.0f // -2.0 to +2.0
    @Volatile var modulationDepth: Float = 0.0f     // 0.0 to 1.0
    @Volatile var sustainPedal: Boolean = false

    // Mixer & Effects states
    @Volatile var mixerState: MixerState = MixerState()
    @Volatile var effectsState: EffectsState = EffectsState()

    // Current voices assigned to parts
    @Volatile var voiceRight1: Voice = VoicePresets.ALL_VOICES[0] // Concert Grand
    @Volatile var voiceRight2: Voice = VoicePresets.ALL_VOICES[8] // Symphonic Strings
    @Volatile var voiceLeft: Voice = VoicePresets.ALL_VOICES[16]   // Acoustic Bass

    // Active synthesis voices pool
    private val activeVoices = Array(MAX_VOICES) { SynthVoice() }

    // Dedicated pool for loud, immediate UI, tactile button, and pad sound effects
    private val uiSoundVoices = Array(8) { UiSoundVoice() }

    // Physical key press tracking for sustain pedal logic (note -> isPhysicalKeyDown)
    private val physicalKeysHeld = ConcurrentHashMap<Pair<Int, KeyboardPart>, Boolean>()

    // Effects processors
    private val reverb = SchroederReverb()
    private val chorus = StereoChorus()
    private val delay = StereoDelay()
    private val eq = ThreeBandEq()

    // Modulation LFO phase
    private var modLfoPhase = 0.0

    init {
        setupAudioTrack()
    }

    private fun setupAudioTrack() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = max(minBufferSize, 2048)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build().apply {
                setVolume(AudioTrack.getMaxVolume())
            }
    }

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)
        audioTrack?.play()
        audioTrack?.setVolume(AudioTrack.getMaxVolume())

        renderThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            val bufferFrames = 512
            val shortBuffer = ShortArray(bufferFrames * 2) // Stereo
            val floatBufferL = FloatArray(bufferFrames)
            val floatBufferR = FloatArray(bufferFrames)

            while (isRunning.get()) {
                renderBlock(floatBufferL, floatBufferR, bufferFrames)

                // Convert float to 16-bit PCM with soft clipping
                for (i in 0 until bufferFrames) {
                    val sampleL = (floatBufferL[i].coerceIn(-1.0f, 1.0f) * 32767.0f).toInt()
                    val sampleR = (floatBufferR[i].coerceIn(-1.0f, 1.0f) * 32767.0f).toInt()
                    shortBuffer[i * 2] = sampleL.toShort()
                    shortBuffer[i * 2 + 1] = sampleR.toShort()
                }

                audioTrack?.write(shortBuffer, 0, shortBuffer.size)
            }
        }, "ArrangerAudioEngine").apply { start() }
    }

    fun stop() {
        isRunning.set(false)
        renderThread?.join(500)
        renderThread = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    // --- NOTE ON / OFF INTERFACE ---

    fun noteOn(note: Int, velocity: Float, part: KeyboardPart, customVoice: Voice? = null) {
        val targetVoice = customVoice ?: when (part) {
            KeyboardPart.RIGHT1 -> voiceRight1
            KeyboardPart.RIGHT2 -> voiceRight2
            KeyboardPart.LEFT -> voiceLeft
            KeyboardPart.ACCOMPANIMENT -> voiceRight1
        }

        val channelId = when (part) {
            KeyboardPart.RIGHT1 -> ChannelId.RIGHT1
            KeyboardPart.RIGHT2 -> ChannelId.RIGHT2
            KeyboardPart.LEFT -> ChannelId.LEFT
            KeyboardPart.ACCOMPANIMENT -> ChannelId.CHORD1
        }

        physicalKeysHeld[Pair(note, part)] = true

        // Find available voice slot or steal
        val voice = findFreeVoice()
        voice.startNote(note, velocity, targetVoice, part, channelId)
    }

    fun noteOff(note: Int, part: KeyboardPart) {
        physicalKeysHeld.remove(Pair(note, part))

        if (sustainPedal) {
            // Keep sustaining, marked as waiting for sustain release
            for (v in activeVoices) {
                if (v.isActive && v.note == note && v.part == part) {
                    v.isKeyReleased = true
                }
            }
        } else {
            for (v in activeVoices) {
                if (v.isActive && v.note == note && v.part == part) {
                    v.release()
                }
            }
        }
    }

    fun triggerDrum(sound: DrumSound, velocity: Float) {
        val voice = findFreeVoice()
        voice.startDrum(sound, velocity)
    }

    fun triggerArrangerNote(note: Int, velocity: Float, voice: Voice, channelId: ChannelId, durationMs: Float) {
        val freeVoice = findFreeVoice()
        freeVoice.startArrangerNote(note, velocity, voice, channelId, durationMs)
    }

    fun setSustain(enabled: Boolean) {
        sustainPedal = enabled
        if (!enabled) {
            // Release all voices whose physical keys are not held
            for (v in activeVoices) {
                if (v.isActive && v.isKeyReleased) {
                    val isHeld = physicalKeysHeld[Pair(v.note, v.part)] == true
                    if (!isHeld) {
                        v.release()
                    }
                }
            }
        }
    }

    fun allNotesOff() {
        physicalKeysHeld.clear()
        for (v in activeVoices) {
            v.stop()
        }
    }

    fun playUiSound(type: SoundEffectType, volume: Float = 1.0f) {
        var target: UiSoundVoice? = null
        for (v in uiSoundVoices) {
            if (!v.isActive) {
                target = v
                break
            }
        }
        if (target == null) {
            target = uiSoundVoices[0]
        }
        target.trigger(type, volume)
    }

    private fun findFreeVoice(): SynthVoice {
        var oldestIdx = 0
        var oldestAge = 0L

        for (i in activeVoices.indices) {
            if (!activeVoices[i].isActive) {
                return activeVoices[i]
            }
            if (activeVoices[i].state == EnvelopeState.RELEASE) {
                return activeVoices[i]
            }
            if (activeVoices[i].age > oldestAge) {
                oldestAge = activeVoices[i].age
                oldestIdx = i
            }
        }
        return activeVoices[oldestIdx] // Voice steal
    }

    // --- AUDIO RENDERING LOOP ---

    private fun renderBlock(outL: FloatArray, outR: FloatArray, frames: Int) {
        outL.fill(0f)
        outR.fill(0f)

        // Read dynamic controls
        val pBend = pitchBendSemitones
        val modDepth = modulationDepth
        val currentMixer = mixerState
        val currentEffects = effectsState

        // Compute balance multipliers: at 0.5f both are 1.0f full volume
        val styleMultiplier = if (balance <= 0.5f) 1.0f else ((1.0f - balance) * 2.0f).coerceIn(0f, 1f)
        val keyboardMultiplier = if (balance >= 0.5f) 1.0f else (balance * 2.0f).coerceIn(0f, 1f)

        // Update modulation LFO
        val lfoFreq = 5.5
        val lfoInc = (2.0 * Math.PI * lfoFreq) / SAMPLE_RATE

        for (f in 0 until frames) {
            modLfoPhase += lfoInc
            if (modLfoPhase > 2.0 * Math.PI) modLfoPhase -= 2.0 * Math.PI
            val lfoVal = sin(modLfoPhase).toFloat() * modDepth

            var blockL = 0f
            var blockR = 0f

            // 1. Synthesize instrument parts & accompaniment
            for (voice in activeVoices) {
                if (!voice.isActive) continue

                val sample = voice.renderSample(pBend, lfoVal)

                val chStrip = currentMixer.channels[voice.channelId]
                if (chStrip != null) {
                    if (chStrip.isMuted) continue

                    // Check solo
                    val anySolo = currentMixer.channels.values.any { it.isSolo }
                    if (anySolo && !chStrip.isSolo) continue

                    val partMultiplier = when (voice.part) {
                        KeyboardPart.RIGHT1, KeyboardPart.RIGHT2, KeyboardPart.LEFT -> keyboardMultiplier
                        KeyboardPart.ACCOMPANIMENT -> styleMultiplier
                    }

                    val vol = chStrip.volume * partMultiplier
                    // Equal loudness panning: center pan is 1.0 on both L & R
                    val panL = (1.0f - chStrip.pan.coerceAtLeast(0f))
                    val panR = (1.0f + chStrip.pan.coerceAtMost(0f))

                    blockL += sample * vol * panL
                    blockR += sample * vol * panR
                } else {
                    blockL += sample * 1.0f
                    blockR += sample * 1.0f
                }
            }

            // 2. Synthesize UI and Button sound effects (tactile clicks, pads, beeps)
            for (uiVoice in uiSoundVoices) {
                if (!uiVoice.isActive) continue
                val uiSample = uiVoice.renderSample()
                blockL += uiSample
                blockR += uiSample
            }

            outL[f] = blockL
            outR[f] = blockR
        }

        // Apply DSP Effects Rack
        // 1. Equalizer
        eq.process(outL, outR, frames, currentEffects.eq)

        // 2. Chorus
        if (currentEffects.chorus.enabled) {
            chorus.process(outL, outR, frames, currentEffects.chorus)
        }

        // 3. Delay
        if (currentEffects.delay.enabled) {
            delay.process(outL, outR, frames, currentEffects.delay)
        }

        // 4. Schroeder Reverb
        if (currentEffects.reverb.enabled) {
            reverb.process(outL, outR, frames, currentEffects.reverb)
        }

        // Apply Master Volume and Analog Saturation / Limiter for loud, clear, punchy output
        val mVol = masterVolume
        for (i in 0 until frames) {
            val amplifiedL = outL[i] * mVol * 2.6f
            val amplifiedR = outR[i] * mVol * 2.6f
            outL[i] = tanh(amplifiedL.toDouble()).toFloat()
            outR[i] = tanh(amplifiedR.toDouble()).toFloat()
        }
    }

    // --- SYNTH VOICE IMPLEMENTATION ---

    enum class EnvelopeState { IDLE, ATTACK, DECAY, SUSTAIN, RELEASE }

    class SynthVoice {
        var isActive = false
        var isKeyReleased = false
        var note: Int = 60
        var velocity: Float = 0.8f
        var part: KeyboardPart = KeyboardPart.RIGHT1
        var channelId: ChannelId = ChannelId.RIGHT1
        var voice: Voice = VoicePresets.ALL_VOICES[0]
        var age: Long = 0L

        var state = EnvelopeState.IDLE
        private var envLevel = 0.0f
        private var attackRate = 0.0f
        private var decayRate = 0.0f
        private var releaseRate = 0.0f
        private var sustainLevel = 0.5f

        private var phase = 0.0
        private var phaseInc = 0.0
        private var baseFreq = 440.0

        // Drum synthesis fields
        private var isDrum = false
        private var drumSound = DrumSound.KICK
        private var drumSampleCounter = 0
        private var drumDuration = 4410

        // Auto release for arranger patterns
        private var autoReleaseCounter = -1

        fun startNote(noteNum: Int, vel: Float, v: Voice, p: KeyboardPart, ch: ChannelId) {
            isActive = true
            isKeyReleased = false
            note = noteNum
            velocity = vel
            voice = v
            part = p
            channelId = ch
            age = System.currentTimeMillis()
            isDrum = false
            autoReleaseCounter = -1

            baseFreq = 440.0 * 2.0.pow((note - 69) / 12.0)
            phase = 0.0
            phaseInc = (2.0 * Math.PI * baseFreq) / SAMPLE_RATE

            val attackSamples = max(1f, (voice.attackMs / 1000f) * SAMPLE_RATE)
            val decaySamples = max(1f, (voice.decayMs / 1000f) * SAMPLE_RATE)
            val releaseSamples = max(1f, (voice.releaseMs / 1000f) * SAMPLE_RATE)

            attackRate = 1.0f / attackSamples
            sustainLevel = voice.sustainLevel
            decayRate = (1.0f - sustainLevel) / decaySamples
            releaseRate = sustainLevel / releaseSamples

            state = EnvelopeState.ATTACK
            envLevel = 0.0f
        }

        fun startArrangerNote(noteNum: Int, vel: Float, v: Voice, ch: ChannelId, durationMs: Float) {
            startNote(noteNum, vel, v, KeyboardPart.ACCOMPANIMENT, ch)
            autoReleaseCounter = ((durationMs / 1000f) * SAMPLE_RATE).toInt()
        }

        fun startDrum(sound: DrumSound, vel: Float) {
            isActive = true
            isKeyReleased = true
            note = sound.midiNote
            velocity = vel
            part = KeyboardPart.ACCOMPANIMENT
            channelId = ChannelId.RHYTHM
            isDrum = true
            drumSound = sound
            drumSampleCounter = 0
            age = System.currentTimeMillis()
            state = EnvelopeState.ATTACK

            drumDuration = when (sound) {
                DrumSound.KICK -> (SAMPLE_RATE * 0.25f).toInt()
                DrumSound.SNARE -> (SAMPLE_RATE * 0.22f).toInt()
                DrumSound.SIDE_STICK -> (SAMPLE_RATE * 0.08f).toInt()
                DrumSound.CLAP -> (SAMPLE_RATE * 0.18f).toInt()
                DrumSound.HIHAT_CLOSED -> (SAMPLE_RATE * 0.06f).toInt()
                DrumSound.HIHAT_OPEN -> (SAMPLE_RATE * 0.45f).toInt()
                DrumSound.HIHAT_PEDAL -> (SAMPLE_RATE * 0.08f).toInt()
                DrumSound.LOW_TOM, DrumSound.MID_TOM, DrumSound.HIGH_TOM -> (SAMPLE_RATE * 0.35f).toInt()
                DrumSound.CRASH, DrumSound.RIDE -> (SAMPLE_RATE * 1.2f).toInt()
                DrumSound.TAMBOURINE, DrumSound.SHAKER -> (SAMPLE_RATE * 0.12f).toInt()
            }
        }

        fun release() {
            if (isActive && state != EnvelopeState.IDLE) {
                state = EnvelopeState.RELEASE
            }
        }

        fun stop() {
            isActive = false
            state = EnvelopeState.IDLE
            envLevel = 0f
        }

        fun renderSample(pitchBendSemi: Float, lfoMod: Float): Float {
            if (!isActive) return 0f

            if (isDrum) {
                return renderDrumSample()
            }

            if (autoReleaseCounter > 0) {
                autoReleaseCounter--
                if (autoReleaseCounter == 0) {
                    release()
                }
            }

            // Envelope progression
            when (state) {
                EnvelopeState.ATTACK -> {
                    envLevel += attackRate
                    if (envLevel >= 1.0f) {
                        envLevel = 1.0f
                        state = EnvelopeState.DECAY
                    }
                }
                EnvelopeState.DECAY -> {
                    envLevel -= decayRate
                    if (envLevel <= sustainLevel) {
                        envLevel = sustainLevel
                        state = EnvelopeState.SUSTAIN
                    }
                }
                EnvelopeState.SUSTAIN -> {
                    envLevel = sustainLevel
                }
                EnvelopeState.RELEASE -> {
                    envLevel -= releaseRate
                    if (envLevel <= 0.001f) {
                        envLevel = 0.0f
                        stop()
                        return 0f
                    }
                }
                EnvelopeState.IDLE -> return 0f
            }

            // Pitch bend and vibrato LFO calculation
            val vibrato = lfoMod * 0.035f + (sin(phase * voice.vibratoRate / 50.0).toFloat() * voice.vibratoDepth * 0.02f)
            val totalBend = pitchBendSemi + vibrato
            val bentFreq = baseFreq * 2.0.pow(totalBend / 12.0)
            val currentPhaseInc = (2.0 * Math.PI * bentFreq) / SAMPLE_RATE

            phase += currentPhaseInc
            if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI

            // Procedural Timbre Synthesis per Waveform
            var rawSample = 0.0f

            when (voice.waveform) {
                Waveform.SINE -> {
                    // Grand Piano harmonic model: fundamental + bright acoustic harmonics + transient bite
                    val p = phase
                    val hammerBite = if (envLevel > 0.85f) (envLevel - 0.85f) * 1.8f else 0f
                    rawSample = sin(p).toFloat() * 0.95f +
                            sin(p * 2.0).toFloat() * (0.45f * envLevel + hammerBite) +
                            sin(p * 3.0).toFloat() * (0.28f * envLevel * envLevel) +
                            sin(p * 4.0).toFloat() * (0.16f * envLevel * envLevel) +
                            sin(p * 5.0).toFloat() * (0.10f * envLevel)
                }
                Waveform.FM_TINE -> {
                    // FM Electric Piano (Rhodes/DX)
                    val modIndex = 2.8f * envLevel * voice.brightness
                    val modPhase = phase * 7.0 // Modulator at 7x frequency
                    val modulator = sin(modPhase).toFloat() * modIndex
                    rawSample = sin(phase + modulator).toFloat() * 0.95f +
                            sin(phase * 0.5).toFloat() * 0.4f // sub body
                }
                Waveform.ORGAN_DRAWBAR -> {
                    // Additive drawbar organ: 16', 8', 4', 2' + Leslie rotary
                    val p = phase
                    rawSample = (sin(p * 0.5) * 0.4 + // 16'
                            sin(p) * 0.48 +           // 8'
                            sin(p * 2.0) * 0.32 +      // 4'
                            sin(p * 3.0) * 0.22 +      // 2 2/3'
                            sin(p * 4.0) * 0.18).toFloat() // 2'
                }
                Waveform.STRINGS_ENSEMBLE -> {
                    // Multi-voice detuned saws
                    val saw1 = (phase / Math.PI - 1.0).toFloat()
                    val phase2 = (phase * 1.008) % (2.0 * Math.PI)
                    val saw2 = (phase2 / Math.PI - 1.0).toFloat()
                    val phase3 = (phase * 0.992) % (2.0 * Math.PI)
                    val saw3 = (phase3 / Math.PI - 1.0).toFloat()
                    rawSample = (saw1 * 0.5f + saw2 * 0.45f + saw3 * 0.45f)
                }
                Waveform.BRASS_SAW -> {
                    // Bright resonant sawtooth with filter attack bite
                    val saw = (phase / Math.PI - 1.0).toFloat()
                    val filterCutoff = envLevel * voice.brightness
                    rawSample = saw * filterCutoff.coerceIn(0.35f, 1.0f) * 1.35f
                }
                Waveform.PAD_LUSH -> {
                    // Warm pad (triangle + soft saw)
                    val tri = (2.0 * abs(phase / Math.PI - 1.0) - 1.0).toFloat()
                    val sawDetuned = ((phase * 1.005) % (2.0 * Math.PI) / Math.PI - 1.0).toFloat()
                    rawSample = tri * 0.72f + sawDetuned * 0.55f
                }
                Waveform.BASS_PUNCH -> {
                    // Sub sine + saturated saw for bass warmth
                    val sub = sin(phase).toFloat() * 1.0f
                    val saw = ((phase * 2.0) % (2.0 * Math.PI) / Math.PI - 1.0).toFloat() * 0.55f
                    rawSample = sub + saw
                }
                Waveform.SAWTOOTH -> {
                    rawSample = (phase / Math.PI - 1.0).toFloat() * 1.15f
                }
                Waveform.SQUARE -> {
                    rawSample = if (phase < Math.PI) 0.85f else -0.85f
                }
                Waveform.TRIANGLE -> {
                    rawSample = (2.0 * abs(phase / Math.PI - 1.0) - 1.0).toFloat() * 1.25f
                }
                Waveform.NOISE -> {
                    rawSample = (Math.random() * 2.0 - 1.0).toFloat() * 1.15f
                }
            }

            return rawSample * envLevel * velocity
        }

        private fun renderDrumSample(): Float {
            drumSampleCounter++
            if (drumSampleCounter >= drumDuration) {
                stop()
                return 0f
            }

            val progress = drumSampleCounter.toFloat() / drumDuration.toFloat()
            val ampEnv = (1.0f - progress).pow(2.0f)

            var drumOut = 0f

            when (drumSound) {
                DrumSound.KICK -> {
                    // Punchy pitch drop from 180Hz to 48Hz
                    val kickFreq = 180f * (1.0f - progress).pow(3.0f) + 48f
                    phase += (2.0 * Math.PI * kickFreq) / SAMPLE_RATE
                    val click = if (drumSampleCounter < 90) (Math.random() * 0.55).toFloat() else 0f
                    drumOut = (sin(phase).toFloat() * ampEnv * 1.35f + click)
                }
                DrumSound.SNARE -> {
                    val noise = (Math.random() * 2.0 - 1.0).toFloat()
                    phase += (2.0 * Math.PI * 185.0) / SAMPLE_RATE
                    val body = sin(phase).toFloat() * (1.0f - progress).pow(4.0f)
                    drumOut = (noise * 0.85f * ampEnv + body * 0.75f) * 1.3f
                }
                DrumSound.SIDE_STICK -> {
                    phase += (2.0 * Math.PI * 880.0) / SAMPLE_RATE
                    drumOut = sin(phase).toFloat() * ampEnv * 1.4f
                }
                DrumSound.CLAP -> {
                    val burstProgress = (drumSampleCounter % 400).toFloat() / 400f
                    val noise = (Math.random() * 2.0 - 1.0).toFloat()
                    drumOut = noise * (1.0f - burstProgress) * ampEnv * 1.3f
                }
                DrumSound.HIHAT_CLOSED -> {
                    val noise = (Math.random() * 2.0 - 1.0).toFloat()
                    drumOut = noise * ampEnv * 0.95f
                }
                DrumSound.HIHAT_OPEN -> {
                    val noise = (Math.random() * 2.0 - 1.0).toFloat()
                    drumOut = noise * (1.0f - progress).pow(1.3f) * 1.0f
                }
                DrumSound.HIHAT_PEDAL -> {
                    val noise = (Math.random() * 2.0 - 1.0).toFloat()
                    drumOut = noise * ampEnv * 0.75f
                }
                DrumSound.LOW_TOM, DrumSound.MID_TOM, DrumSound.HIGH_TOM -> {
                    val baseF = when (drumSound) {
                        DrumSound.LOW_TOM -> 90.0
                        DrumSound.MID_TOM -> 130.0
                        else -> 175.0
                    }
                    val tomFreq = baseF * (1.0 - progress * 0.3)
                    phase += (2.0 * Math.PI * tomFreq) / SAMPLE_RATE
                    drumOut = sin(phase).toFloat() * ampEnv * 1.35f
                }
                DrumSound.CRASH, DrumSound.RIDE -> {
                    val noise = (Math.random() * 2.0 - 1.0).toFloat()
                    phase += (2.0 * Math.PI * 3400.0) / SAMPLE_RATE
                    val ring = sin(phase).toFloat() * 0.4f
                    drumOut = (noise * 0.85f + ring) * ampEnv * 1.0f
                }
                DrumSound.TAMBOURINE, DrumSound.SHAKER -> {
                    val noise = (Math.random() * 2.0 - 1.0).toFloat()
                    drumOut = noise * ampEnv * 0.85f
                }
            }

            return drumOut * velocity
        }
    }

    // --- DSP PROCESSORS ---

    // Schroeder Reverberator (4 Comb Filters + 2 All-Pass Filters)
    class SchroederReverb {
        private val combDelays = intArrayOf(1557, 1617, 1491, 1422)
        private val combBuffers = Array(4) { FloatArray(combDelays[it]) }
        private val combIndices = IntArray(4)
        private val allpassDelay1 = 225
        private val allpassBuffer1 = FloatArray(allpassDelay1)
        private var allpassIdx1 = 0
        private val allpassDelay2 = 556
        private val allpassBuffer2 = FloatArray(allpassDelay2)
        private var allpassIdx2 = 0

        fun process(bufL: FloatArray, bufR: FloatArray, frames: Int, settings: ReverbSettings) {
            val room = settings.roomSize * 0.28f + 0.7f
            val damp = settings.damping * 0.4f
            val wet = settings.sendLevel
            val dry = 1.0f - wet * 0.5f

            for (i in 0 until frames) {
                val input = (bufL[i] + bufR[i]) * 0.5f

                // 4 Comb filters in parallel
                var combSum = 0f
                for (c in 0 until 4) {
                    val buf = combBuffers[c]
                    val idx = combIndices[c]
                    val delayed = buf[idx]
                    buf[idx] = input + delayed * room * (1.0f - damp)
                    combIndices[c] = (idx + 1) % combDelays[c]
                    combSum += delayed
                }
                combSum *= 0.25f

                // Allpass 1
                val apDelayed1 = allpassBuffer1[allpassIdx1]
                val apOut1 = -0.5f * combSum + apDelayed1
                allpassBuffer1[allpassIdx1] = combSum + 0.5f * apOut1
                allpassIdx1 = (allpassIdx1 + 1) % allpassDelay1

                // Allpass 2
                val apDelayed2 = allpassBuffer2[allpassIdx2]
                val apOut2 = -0.5f * apOut1 + apDelayed2
                allpassBuffer2[allpassIdx2] = apOut1 + 0.5f * apOut2
                allpassIdx2 = (allpassIdx2 + 1) % allpassDelay2

                bufL[i] = bufL[i] * dry + apOut2 * wet
                bufR[i] = bufR[i] * dry + apOut2 * wet
            }
        }
    }

    // Modulated Stereo Chorus
    class StereoChorus {
        private val maxDelay = 1024
        private val bufferL = FloatArray(maxDelay)
        private val bufferR = FloatArray(maxDelay)
        private var writeIdx = 0
        private var lfoPhase = 0.0

        fun process(bufL: FloatArray, bufR: FloatArray, frames: Int, settings: ChorusSettings) {
            val lfoInc = (2.0 * Math.PI * settings.rateHz) / SAMPLE_RATE
            val depthSamples = settings.depth * 300f
            val baseDelay = 400f
            val mix = settings.mix

            for (i in 0 until frames) {
                lfoPhase += lfoInc
                if (lfoPhase > 2.0 * Math.PI) lfoPhase -= 2.0 * Math.PI

                bufferL[writeIdx] = bufL[i]
                bufferR[writeIdx] = bufR[i]

                val modL = (sin(lfoPhase) * depthSamples + baseDelay).toFloat()
                val modR = (cos(lfoPhase) * depthSamples + baseDelay).toFloat()

                val readIdxL = ((writeIdx - modL.toInt() + maxDelay) % maxDelay)
                val readIdxR = ((writeIdx - modR.toInt() + maxDelay) % maxDelay)

                bufL[i] = bufL[i] * (1.0f - mix) + bufferL[readIdxL] * mix
                bufR[i] = bufR[i] * (1.0f - mix) + bufferR[readIdxR] * mix

                writeIdx = (writeIdx + 1) % maxDelay
            }
        }
    }

    // Stereo Ping-Pong / Echo Delay
    class StereoDelay {
        private val maxDelay = SAMPLE_RATE * 2
        private val delayBufferL = FloatArray(maxDelay)
        private val delayBufferR = FloatArray(maxDelay)
        private var writeIdx = 0

        fun process(bufL: FloatArray, bufR: FloatArray, frames: Int, settings: DelaySettings) {
            val delaySamples = ((settings.timeMs / 1000f) * SAMPLE_RATE).toInt().coerceIn(100, maxDelay - 1)
            val fb = settings.feedback
            val mix = settings.mix

            for (i in 0 until frames) {
                val readIdx = (writeIdx - delaySamples + maxDelay) % maxDelay
                val delayedL = delayBufferL[readIdx]
                val delayedR = delayBufferR[readIdx]

                delayBufferL[writeIdx] = bufL[i] + delayedR * fb
                delayBufferR[writeIdx] = bufR[i] + delayedL * fb

                bufL[i] = bufL[i] * (1.0f - mix) + delayedL * mix
                bufR[i] = bufR[i] * (1.0f - mix) + delayedR * mix

                writeIdx = (writeIdx + 1) % maxDelay
            }
        }
    }

    // 3-Band Parametric Equalizer (Biquad Low Shelf, Mid Peaking, High Shelf)
    class ThreeBandEq {
        private var x1L = 0f; private var x2L = 0f; private var y1L = 0f; private var y2L = 0f
        private var x1R = 0f; private var x2R = 0f; private var y1R = 0f; private var y2R = 0f

        fun process(bufL: FloatArray, bufR: FloatArray, frames: Int, settings: EqSettings) {
            val lowMult = 10.0.pow(settings.lowGainDb / 20.0).toFloat()
            val highMult = 10.0.pow(settings.highGainDb / 20.0).toFloat()

            // Simple fast tone shaping
            for (i in 0 until frames) {
                // Low shelf filter
                val lowL = (bufL[i] + x1L) * 0.5f
                x1L = bufL[i]
                bufL[i] = bufL[i] + lowL * (lowMult - 1.0f) * 0.4f

                val lowR = (bufR[i] + x1R) * 0.5f
                x1R = bufR[i]
                bufR[i] = bufR[i] + lowR * (lowMult - 1.0f) * 0.4f

                // High shelf
                val highL = bufL[i] - lowL
                bufL[i] += highL * (highMult - 1.0f) * 0.35f

                val highR = bufR[i] - lowR
                bufR[i] += highR * (highMult - 1.0f) * 0.35f
            }
        }
    }

    // High-volume, real-time procedural synthesizer for UI buttons, dials, pads, and tactile sounds
    class UiSoundVoice {
        var isActive = false
        var type = SoundEffectType.BUTTON_CLICK
        var sampleCounter = 0
        var totalSamples = 0
        var volume = 1.0f
        var phase1 = 0.0
        var phase2 = 0.0

        fun trigger(effect: SoundEffectType, vol: Float = 1.0f) {
            type = effect
            volume = vol
            sampleCounter = 0
            phase1 = 0.0
            phase2 = 0.0
            totalSamples = when (effect) {
                SoundEffectType.BUTTON_CLICK -> (SAMPLE_RATE * 0.045f).toInt()
                SoundEffectType.BUTTON_HEAVY -> (SAMPLE_RATE * 0.08f).toInt()
                SoundEffectType.BUTTON_BEEP -> (SAMPLE_RATE * 0.14f).toInt()
                SoundEffectType.DIAL_TICK -> (SAMPLE_RATE * 0.025f).toInt()
                SoundEffectType.PAD_FX_1 -> (SAMPLE_RATE * 0.65f).toInt()
                SoundEffectType.PAD_FX_2 -> (SAMPLE_RATE * 0.70f).toInt()
                SoundEffectType.PAD_FX_3 -> (SAMPLE_RATE * 0.55f).toInt()
                SoundEffectType.PAD_FX_4 -> (SAMPLE_RATE * 0.60f).toInt()
                SoundEffectType.METRONOME_HIGH -> (SAMPLE_RATE * 0.05f).toInt()
                SoundEffectType.METRONOME_LOW -> (SAMPLE_RATE * 0.04f).toInt()
                SoundEffectType.SUCCESS_CHIME -> (SAMPLE_RATE * 0.35f).toInt()
                SoundEffectType.ERROR_ALERT -> (SAMPLE_RATE * 0.18f).toInt()
            }
            isActive = true
        }

        fun renderSample(): Float {
            if (!isActive) return 0f
            sampleCounter++
            if (sampleCounter >= totalSamples) {
                isActive = false
                return 0f
            }

            val progress = sampleCounter.toFloat() / totalSamples.toFloat()
            val env = (1.0f - progress).pow(2.0f)

            return when (type) {
                SoundEffectType.BUTTON_CLICK -> {
                    // Crisp, snappy hardware tactile button switch click
                    phase1 += (2.0 * Math.PI * (2800.0 * (1.0 - progress * 0.6))) / SAMPLE_RATE
                    val noise = (Math.random() * 2.0 - 1.0).toFloat() * (1.0f - progress).pow(4.0f) * 0.45f
                    val click = sin(phase1).toFloat() * env * 0.85f
                    (click + noise) * volume * 1.6f
                }
                SoundEffectType.BUTTON_HEAVY -> {
                    // Deep solid mechanical relay latch / power switch
                    phase1 += (2.0 * Math.PI * (240.0 * (1.0 - progress * 0.8))) / SAMPLE_RATE
                    val noise = (Math.random() * 2.0 - 1.0).toFloat() * (1.0f - progress).pow(3.0f) * 0.5f
                    val body = sin(phase1).toFloat() * env * 1.2f
                    (body + noise) * volume * 1.7f
                }
                SoundEffectType.BUTTON_BEEP -> {
                    // High-tech two-tone beep: 1046Hz (C6) -> 1318Hz (E6)
                    val freq = if (progress < 0.5f) 1046.5 else 1318.5
                    phase1 += (2.0 * Math.PI * freq) / SAMPLE_RATE
                    val beepEnv = sin(progress * Math.PI).toFloat().pow(0.5f)
                    sin(phase1).toFloat() * beepEnv * volume * 1.35f
                }
                SoundEffectType.DIAL_TICK -> {
                    // Short metallic mechanical detent click
                    phase1 += (2.0 * Math.PI * 3800.0) / SAMPLE_RATE
                    val tickEnv = (1.0f - progress).pow(3.0f)
                    sin(phase1).toFloat() * tickEnv * volume * 1.5f
                }
                SoundEffectType.PAD_FX_1 -> {
                    // Synth Brass Fanfare chord hit (C major triad: 523Hz, 659Hz, 784Hz)
                    phase1 += (2.0 * Math.PI * 523.25) / SAMPLE_RATE
                    phase2 += (2.0 * Math.PI * 659.25) / SAMPLE_RATE
                    val saw1 = (phase1 / Math.PI - 1.0).toFloat()
                    val saw2 = (phase2 / Math.PI - 1.0).toFloat()
                    (saw1 * 0.55f + saw2 * 0.55f) * env * volume * 1.6f
                }
                SoundEffectType.PAD_FX_2 -> {
                    // Electro laser sweep: 1800Hz down to 120Hz
                    val sweepFreq = 1800.0 * (1.0 - progress).pow(2.0) + 120.0
                    phase1 += (2.0 * Math.PI * sweepFreq) / SAMPLE_RATE
                    sin(phase1).toFloat() * env * volume * 1.6f
                }
                SoundEffectType.PAD_FX_3 -> {
                    // Orchestral Tutti hit (Damped sawtooth cluster + kick impact)
                    phase1 += (2.0 * Math.PI * 130.8) / SAMPLE_RATE
                    phase2 += (2.0 * Math.PI * 196.0) / SAMPLE_RATE
                    val noise = (Math.random() * 2.0 - 1.0).toFloat() * (1.0f - progress).pow(4.0f) * 0.5f
                    val body = (sin(phase1) + sin(phase2)).toFloat() * 0.55f * env
                    (body + noise) * volume * 1.7f
                }
                SoundEffectType.PAD_FX_4 -> {
                    // DJ Vinyl Scratch / Beat Hit
                    val scratchFreq = 400.0 + 800.0 * sin(progress * Math.PI * 4.0)
                    phase1 += (2.0 * Math.PI * scratchFreq) / SAMPLE_RATE
                    val noise = (Math.random() * 2.0 - 1.0).toFloat() * 0.35f
                    (sin(phase1).toFloat() * 0.75f + noise) * env * volume * 1.6f
                }
                SoundEffectType.METRONOME_HIGH -> {
                    // Sharp high woodblock / rim click
                    phase1 += (2.0 * Math.PI * 2400.0) / SAMPLE_RATE
                    sin(phase1).toFloat() * env * volume * 1.7f
                }
                SoundEffectType.METRONOME_LOW -> {
                    // Mid woodblock / click
                    phase1 += (2.0 * Math.PI * 1600.0) / SAMPLE_RATE
                    sin(phase1).toFloat() * env * volume * 1.5f
                }
                SoundEffectType.SUCCESS_CHIME -> {
                    // Rising two-tone major third: 880Hz -> 1108Hz
                    val freq = if (progress < 0.5f) 880.0 else 1108.7
                    phase1 += (2.0 * Math.PI * freq) / SAMPLE_RATE
                    val subEnv = (1.0f - (progress % 0.5f) * 2.0f).pow(1.5f)
                    sin(phase1).toFloat() * subEnv * volume * 1.4f
                }
                SoundEffectType.ERROR_ALERT -> {
                    // Low warning buzz: 220Hz
                    phase1 += (2.0 * Math.PI * 220.0) / SAMPLE_RATE
                    val sq = if ((phase1 % (2.0 * Math.PI)) < Math.PI) 0.65f else -0.65f
                    sq * env * volume * 1.3f
                }
            }
        }
    }
}
