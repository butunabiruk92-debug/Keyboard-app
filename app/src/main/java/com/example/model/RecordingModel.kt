package com.example.model

enum class KeyboardPart {
    RIGHT1,
    RIGHT2,
    LEFT,
    ACCOMPANIMENT
}

data class RecordedNoteEvent(
    val timestampMs: Long,
    val note: Int,
    val velocity: Float,
    val isNoteOn: Boolean,
    val part: KeyboardPart
)

data class SongRecording(
    val id: String,
    val title: String,
    val timestamp: Long,
    val durationMs: Long,
    val tempo: Int,
    val styleId: String,
    val events: List<RecordedNoteEvent>
)

enum class MultiPadId(val index: Int, val displayName: String) {
    PAD_1(1, "Pad 1"),
    PAD_2(2, "Pad 2"),
    PAD_3(3, "Pad 3"),
    PAD_4(4, "Pad 4")
}

data class MultiPadPhrase(
    val id: MultiPadId,
    val name: String,
    val soundVoiceId: String,
    // Note offsets from chord root and beat offsets (in steps of 16th)
    val notes: List<Pair<Int, Int>> // (stepOffset, noteIntervalFromRoot)
)

object MultiPadPresets {
    val PHRASES = mapOf(
        MultiPadId.PAD_1 to MultiPadPhrase(
            MultiPadId.PAD_1, "Guitar Strum", "grand_concert",
            listOf(0 to 0, 1 to 4, 2 to 7, 3 to 12, 8 to 0, 9 to 4, 10 to 7)
        ),
        MultiPadId.PAD_2 to MultiPadPhrase(
            MultiPadId.PAD_2, "Synth Arp", "synth_saw_lead",
            listOf(0 to 0, 2 to 7, 4 to 12, 6 to 16, 8 to 12, 10 to 7, 12 to 4, 14 to 0)
        ),
        MultiPadId.PAD_3 to MultiPadPhrase(
            MultiPadId.PAD_3, "Brass Fanfare", "brass_section",
            listOf(0 to 0, 0 to 7, 4 to 4, 4 to 11, 8 to 7, 8 to 12, 12 to 12, 12 to 16)
        ),
        MultiPadId.PAD_4 to MultiPadPhrase(
            MultiPadId.PAD_4, "Percussion Roll", "drum_standard",
            listOf(0 to 38, 2 to 38, 4 to 38, 6 to 38, 8 to 47, 10 to 47, 12 to 41, 14 to 49)
        )
    )
}
