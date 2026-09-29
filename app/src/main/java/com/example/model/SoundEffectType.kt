package com.example.model

enum class SoundEffectType {
    BUTTON_CLICK,     // Crisp mechanical switch snap (tactile hardware buttons)
    BUTTON_HEAVY,     // Heavy latch / relay thunk (Power, Start/Stop, Sync Start, Rec)
    BUTTON_BEEP,      // High-tech registration/OTS confirmation chime (880Hz / 1760Hz double pip)
    DIAL_TICK,        // Metallic mechanical detent click (rotary dials and knobs)
    PAD_FX_1,         // Multi-pad 1: Synth Brass Fanfare / chord hit
    PAD_FX_2,         // Multi-pad 2: Rising electro sweep / laser drop
    PAD_FX_3,         // Multi-pad 3: Orchestral Tutti Stab
    PAD_FX_4,         // Multi-pad 4: DJ scratch / groove beat
    METRONOME_HIGH,   // Metronome beat 1 woodblock/beep
    METRONOME_LOW,    // Metronome beats 2-4 woodblock/beep
    SUCCESS_CHIME,    // Save success two-tone chime
    ERROR_ALERT       // Boundary alert beep
}
