package com.example.model

enum class ChannelId(val displayName: String, val shortName: String) {
    RHYTHM("Rhythm", "RHY"),
    BASS("Bass", "BASS"),
    CHORD1("Chord 1", "CHD1"),
    CHORD2("Chord 2", "CHD2"),
    PAD("Pad", "PAD"),
    PHRASE1("Phrase 1", "PHR1"),
    PHRASE2("Phrase 2", "PHR2"),
    LEFT("Left Part", "LEFT"),
    RIGHT1("Right 1", "R1"),
    RIGHT2("Right 2", "R2"),
    RIGHT3("Right 3", "R3")
}

data class ChannelStrip(
    val id: ChannelId,
    val volume: Float = 0.8f, // 0.0 to 1.0
    val pan: Float = 0.0f,     // -1.0 (Left) to +1.0 (Right)
    val isMuted: Boolean = false,
    val isSolo: Boolean = false
)

data class ReverbSettings(
    val enabled: Boolean = true,
    val roomSize: Float = 0.65f, // 0.0 to 1.0
    val damping: Float = 0.4f,   // 0.0 to 1.0
    val sendLevel: Float = 0.35f  // 0.0 to 1.0
)

data class ChorusSettings(
    val enabled: Boolean = true,
    val rateHz: Float = 1.2f,    // 0.1 to 5.0 Hz
    val depth: Float = 0.45f,    // 0.0 to 1.0
    val mix: Float = 0.3f        // 0.0 to 1.0
)

data class DelaySettings(
    val enabled: Boolean = false,
    val timeMs: Float = 350f,    // 50 to 1000 ms
    val feedback: Float = 0.35f, // 0.0 to 0.85
    val mix: Float = 0.25f       // 0.0 to 1.0
)

data class EqSettings(
    val lowGainDb: Float = 1.0f,  // -12 to +12 dB
    val midGainDb: Float = 0.0f,  // -12 to +12 dB
    val highGainDb: Float = 2.0f  // -12 to +12 dB
)

data class EffectsState(
    val reverb: ReverbSettings = ReverbSettings(),
    val chorus: ChorusSettings = ChorusSettings(),
    val delay: DelaySettings = DelaySettings(),
    val eq: EqSettings = EqSettings()
)

data class MixerState(
    val masterVolume: Float = 1.0f,
    val balance: Float = 0.5f, // 0.0 = all style accompaniment, 1.0 = all keyboard parts, 0.5 = equal
    val micVolume: Float = 0.8f,
    val channels: Map<ChannelId, ChannelStrip> = ChannelId.values().associateWith { id ->
        when (id) {
            ChannelId.RHYTHM -> ChannelStrip(id, volume = 1.0f, pan = 0.0f)
            ChannelId.BASS -> ChannelStrip(id, volume = 1.0f, pan = 0.0f)
            ChannelId.CHORD1 -> ChannelStrip(id, volume = 0.9f, pan = -0.2f)
            ChannelId.CHORD2 -> ChannelStrip(id, volume = 0.85f, pan = 0.25f)
            ChannelId.PAD -> ChannelStrip(id, volume = 0.8f, pan = -0.1f)
            ChannelId.PHRASE1 -> ChannelStrip(id, volume = 0.9f, pan = 0.3f)
            ChannelId.PHRASE2 -> ChannelStrip(id, volume = 0.85f, pan = -0.3f)
            ChannelId.LEFT -> ChannelStrip(id, volume = 0.95f, pan = -0.15f)
            ChannelId.RIGHT1 -> ChannelStrip(id, volume = 1.0f, pan = 0.0f)
            ChannelId.RIGHT2 -> ChannelStrip(id, volume = 0.9f, pan = 0.2f)
            ChannelId.RIGHT3 -> ChannelStrip(id, volume = 0.8f, pan = -0.2f)
        }
    }
)

data class RegistrationMemory(
    val slotNumber: Int, // 1 to 8
    val bankNumber: Int = 1,
    val name: String,
    val styleId: String,
    val tempo: Int,
    val voiceRight1Id: String,
    val voiceRight2Id: String,
    val voiceLeftId: String,
    val isSplit: Boolean,
    val isLayer: Boolean,
    val splitPoint: Int,
    val transpose: Int,
    val octave: Int,
    val mixerState: MixerState,
    val effectsState: EffectsState
)
