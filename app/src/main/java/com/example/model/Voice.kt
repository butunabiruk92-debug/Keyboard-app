package com.example.model

enum class VoiceCategory(val displayName: String) {
    PIANO("Grand Piano"),
    E_PIANO("E. Piano"),
    ORGAN("Organ"),
    STRINGS("Strings"),
    BRASS("Brass"),
    WOODWIND("Woodwind"),
    PAD("Synth Pad"),
    SYNTH("Lead Synth"),
    BASS("Bass"),
    DRUMS("Drum Kit")
}

enum class Waveform {
    SINE,
    SAWTOOTH,
    SQUARE,
    TRIANGLE,
    FM_TINE,
    ORGAN_DRAWBAR,
    STRINGS_ENSEMBLE,
    BRASS_SAW,
    PAD_LUSH,
    BASS_PUNCH,
    NOISE
}

data class Voice(
    val id: String,
    val name: String,
    val category: VoiceCategory,
    val waveform: Waveform,
    val attackMs: Float = 10f,
    val decayMs: Float = 300f,
    val sustainLevel: Float = 0.6f,
    val releaseMs: Float = 400f,
    val brightness: Float = 1.0f,
    val harmonicsCount: Int = 4,
    val detuneAmount: Float = 0.0f,
    val vibratoRate: Float = 5.0f,
    val vibratoDepth: Float = 0.0f
)

object VoicePresets {
    val ALL_VOICES = listOf(
        // Grand Pianos
        Voice("grand_concert", "Concert Grand", VoiceCategory.PIANO, Waveform.SINE, attackMs = 5f, decayMs = 1200f, sustainLevel = 0.35f, releaseMs = 600f, brightness = 1.0f, harmonicsCount = 8),
        Voice("grand_bright", "Bright Studio Grand", VoiceCategory.PIANO, Waveform.SINE, attackMs = 3f, decayMs = 900f, sustainLevel = 0.4f, releaseMs = 500f, brightness = 1.4f, harmonicsCount = 9),
        Voice("grand_warm", "Warm Mellow Grand", VoiceCategory.PIANO, Waveform.SINE, attackMs = 8f, decayMs = 1400f, sustainLevel = 0.3f, releaseMs = 700f, brightness = 0.8f, harmonicsCount = 6),
        Voice("grand_honky", "Honky Tonk Piano", VoiceCategory.PIANO, Waveform.SINE, attackMs = 4f, decayMs = 800f, sustainLevel = 0.4f, releaseMs = 400f, brightness = 1.5f, harmonicsCount = 7, detuneAmount = 0.04f),

        // Electric Pianos
        Voice("ep_rhodes", "Vintage Suitcase Tine", VoiceCategory.E_PIANO, Waveform.FM_TINE, attackMs = 5f, decayMs = 1000f, sustainLevel = 0.45f, releaseMs = 500f, brightness = 1.1f, harmonicsCount = 5),
        Voice("ep_dx", "80s Digital DX Tine", VoiceCategory.E_PIANO, Waveform.FM_TINE, attackMs = 3f, decayMs = 850f, sustainLevel = 0.5f, releaseMs = 450f, brightness = 1.6f, harmonicsCount = 7),
        Voice("ep_wurlitzer", "Classic Reed Piano", VoiceCategory.E_PIANO, Waveform.SQUARE, attackMs = 6f, decayMs = 700f, sustainLevel = 0.4f, releaseMs = 400f, brightness = 1.2f, harmonicsCount = 5),
        Voice("ep_clav", "Funk Clavinet D6", VoiceCategory.E_PIANO, Waveform.SAWTOOTH, attackMs = 2f, decayMs = 350f, sustainLevel = 0.15f, releaseMs = 150f, brightness = 1.8f, harmonicsCount = 7),

        // Organs
        Voice("organ_jazz", "Jazz Tonewheel B3", VoiceCategory.ORGAN, Waveform.ORGAN_DRAWBAR, attackMs = 15f, decayMs = 100f, sustainLevel = 0.95f, releaseMs = 100f, brightness = 1.2f, harmonicsCount = 9, vibratoRate = 6.2f, vibratoDepth = 0.15f),
        Voice("organ_rock", "Rock Overdriven Organ", VoiceCategory.ORGAN, Waveform.ORGAN_DRAWBAR, attackMs = 10f, decayMs = 80f, sustainLevel = 1.0f, releaseMs = 90f, brightness = 1.7f, harmonicsCount = 9, detuneAmount = 0.02f),
        Voice("organ_pipe", "Cathedral Pipe Organ", VoiceCategory.ORGAN, Waveform.ORGAN_DRAWBAR, attackMs = 40f, decayMs = 200f, sustainLevel = 0.9f, releaseMs = 400f, brightness = 1.0f, harmonicsCount = 8),
        Voice("organ_gospel", "Gospel Chorus Rotary", VoiceCategory.ORGAN, Waveform.ORGAN_DRAWBAR, attackMs = 20f, decayMs = 120f, sustainLevel = 0.92f, releaseMs = 120f, brightness = 1.3f, harmonicsCount = 9, vibratoRate = 4.8f, vibratoDepth = 0.25f),

        // Strings
        Voice("strings_symphony", "Symphonic Ensemble", VoiceCategory.STRINGS, Waveform.STRINGS_ENSEMBLE, attackMs = 180f, decayMs = 400f, sustainLevel = 0.85f, releaseMs = 700f, brightness = 1.1f, harmonicsCount = 6, detuneAmount = 0.03f),
        Voice("strings_chamber", "Chamber Slow Strings", VoiceCategory.STRINGS, Waveform.STRINGS_ENSEMBLE, attackMs = 260f, decayMs = 500f, sustainLevel = 0.8f, releaseMs = 900f, brightness = 0.9f, harmonicsCount = 5, detuneAmount = 0.025f),
        Voice("strings_pizzicato", "Pizzicato Strings", VoiceCategory.STRINGS, Waveform.SAWTOOTH, attackMs = 3f, decayMs = 280f, sustainLevel = 0.0f, releaseMs = 150f, brightness = 1.4f, harmonicsCount = 6),
        Voice("strings_solo_violin", "Expressive Solo Violin", VoiceCategory.STRINGS, Waveform.SAWTOOTH, attackMs = 90f, decayMs = 300f, sustainLevel = 0.85f, releaseMs = 400f, brightness = 1.3f, harmonicsCount = 6, vibratoRate = 5.5f, vibratoDepth = 0.2f),

        // Brass
        Voice("brass_section", "Power Brass Section", VoiceCategory.BRASS, Waveform.BRASS_SAW, attackMs = 35f, decayMs = 350f, sustainLevel = 0.75f, releaseMs = 280f, brightness = 1.5f, harmonicsCount = 7, detuneAmount = 0.02f),
        Voice("brass_trumpet", "Lead Studio Trumpet", VoiceCategory.BRASS, Waveform.BRASS_SAW, attackMs = 25f, decayMs = 300f, sustainLevel = 0.8f, releaseMs = 200f, brightness = 1.6f, harmonicsCount = 6),
        Voice("brass_french_horn", "Majestic French Horn", VoiceCategory.BRASS, Waveform.BRASS_SAW, attackMs = 70f, decayMs = 450f, sustainLevel = 0.85f, releaseMs = 450f, brightness = 0.9f, harmonicsCount = 5),
        Voice("brass_synth", "80s Analog Synth Brass", VoiceCategory.BRASS, Waveform.BRASS_SAW, attackMs = 40f, decayMs = 400f, sustainLevel = 0.7f, releaseMs = 350f, brightness = 1.8f, harmonicsCount = 8, detuneAmount = 0.035f),

        // Woodwind
        Voice("ww_flute", "Concert Silver Flute", VoiceCategory.WOODWIND, Waveform.SINE, attackMs = 45f, decayMs = 250f, sustainLevel = 0.85f, releaseMs = 250f, brightness = 1.2f, harmonicsCount = 4, vibratoRate = 5.2f, vibratoDepth = 0.15f),
        Voice("ww_clarinet", "Warm Studio Clarinet", VoiceCategory.WOODWIND, Waveform.SQUARE, attackMs = 35f, decayMs = 300f, sustainLevel = 0.8f, releaseMs = 220f, brightness = 1.0f, harmonicsCount = 5),
        Voice("ww_oboe", "Orchestral Oboe", VoiceCategory.WOODWIND, Waveform.SAWTOOTH, attackMs = 30f, decayMs = 320f, sustainLevel = 0.85f, releaseMs = 260f, brightness = 1.4f, harmonicsCount = 6, vibratoRate = 5.0f, vibratoDepth = 0.18f),
        Voice("ww_saxophone", "Tenor Saxophone", VoiceCategory.WOODWIND, Waveform.SAWTOOTH, attackMs = 30f, decayMs = 350f, sustainLevel = 0.82f, releaseMs = 250f, brightness = 1.5f, harmonicsCount = 7, vibratoRate = 5.6f, vibratoDepth = 0.22f),

        // Synth Pads
        Voice("pad_warm", "Warm Analog Lush Pad", VoiceCategory.PAD, Waveform.PAD_LUSH, attackMs = 320f, decayMs = 600f, sustainLevel = 0.9f, releaseMs = 1200f, brightness = 1.0f, harmonicsCount = 6, detuneAmount = 0.04f),
        Voice("pad_shimmer", "Shimmer Crystal Pad", VoiceCategory.PAD, Waveform.PAD_LUSH, attackMs = 280f, decayMs = 700f, sustainLevel = 0.85f, releaseMs = 1400f, brightness = 1.7f, harmonicsCount = 7, detuneAmount = 0.05f),
        Voice("pad_ambient", "Deep Space Atmosphere", VoiceCategory.PAD, Waveform.PAD_LUSH, attackMs = 500f, decayMs = 800f, sustainLevel = 0.95f, releaseMs = 1800f, brightness = 0.8f, harmonicsCount = 5, detuneAmount = 0.06f),
        Voice("pad_sweeping", "Resonant Sweep Pad", VoiceCategory.PAD, Waveform.PAD_LUSH, attackMs = 350f, decayMs = 650f, sustainLevel = 0.88f, releaseMs = 1100f, brightness = 1.4f, harmonicsCount = 6, vibratoRate = 1.2f, vibratoDepth = 0.3f),

        // Lead Synths
        Voice("synth_saw_lead", "Hyper Saw Solo Lead", VoiceCategory.SYNTH, Waveform.SAWTOOTH, attackMs = 12f, decayMs = 350f, sustainLevel = 0.85f, releaseMs = 300f, brightness = 1.8f, harmonicsCount = 8, detuneAmount = 0.03f),
        Voice("synth_square_lead", "Retro 8-Bit Square Lead", VoiceCategory.SYNTH, Waveform.SQUARE, attackMs = 8f, decayMs = 250f, sustainLevel = 0.8f, releaseMs = 200f, brightness = 1.5f, harmonicsCount = 6),
        Voice("synth_sync_lead", "Cutting Hard Sync Lead", VoiceCategory.SYNTH, Waveform.SAWTOOTH, attackMs = 10f, decayMs = 300f, sustainLevel = 0.8f, releaseMs = 250f, brightness = 2.0f, harmonicsCount = 9),

        // Bass
        Voice("bass_acoustic", "Acoustic Upright Bass", VoiceCategory.BASS, Waveform.BASS_PUNCH, attackMs = 15f, decayMs = 800f, sustainLevel = 0.45f, releaseMs = 350f, brightness = 0.8f, harmonicsCount = 5),
        Voice("bass_electric", "Fingered Electric Bass", VoiceCategory.BASS, Waveform.BASS_PUNCH, attackMs = 10f, decayMs = 950f, sustainLevel = 0.5f, releaseMs = 300f, brightness = 1.1f, harmonicsCount = 6),
        Voice("bass_slap", "Funk Slap Bass", VoiceCategory.BASS, Waveform.BASS_PUNCH, attackMs = 4f, decayMs = 500f, sustainLevel = 0.35f, releaseMs = 200f, brightness = 1.7f, harmonicsCount = 7),
        Voice("bass_synth", "Moog Analog Sub Bass", VoiceCategory.BASS, Waveform.SAWTOOTH, attackMs = 8f, decayMs = 700f, sustainLevel = 0.65f, releaseMs = 250f, brightness = 1.3f, harmonicsCount = 5),

        // Drum Kits
        Voice("drum_standard", "Studio Live Acoustic Kit", VoiceCategory.DRUMS, Waveform.NOISE, attackMs = 2f, decayMs = 250f, sustainLevel = 0.0f, releaseMs = 150f),
        Voice("drum_electronic", "Analog 808/909 Synth Kit", VoiceCategory.DRUMS, Waveform.NOISE, attackMs = 1f, decayMs = 300f, sustainLevel = 0.0f, releaseMs = 200f)
    )

    fun getById(id: String): Voice {
        return ALL_VOICES.firstOrNull { it.id == id } ?: ALL_VOICES.first()
    }
}
