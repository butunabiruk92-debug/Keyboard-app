package com.example.model

enum class StyleCategory(val displayName: String) {
    POP("Pop"),
    ROCK("Rock"),
    BALLAD("Ballad"),
    DANCE("Dance"),
    LATIN("Latin"),
    JAZZ("Jazz"),
    RNB("R&B"),
    WORLD("World")
}

enum class StyleSection(val displayName: String) {
    INTRO("Intro"),
    MAIN_A("Main A"),
    MAIN_B("Main B"),
    MAIN_C("Main C"),
    MAIN_D("Main D"),
    FILL_IN("Fill In"),
    BREAK("Break"),
    ENDING("Ending")
}

enum class DrumSound(val midiNote: Int) {
    KICK(36),
    SNARE(38),
    SIDE_STICK(37),
    CLAP(39),
    HIHAT_CLOSED(42),
    HIHAT_OPEN(46),
    HIHAT_PEDAL(44),
    LOW_TOM(41),
    MID_TOM(47),
    HIGH_TOM(50),
    CRASH(49),
    RIDE(51),
    TAMBOURINE(54),
    SHAKER(70)
}

data class DrumTrigger(
    val sound: DrumSound,
    val velocity: Float = 0.85f
)

data class BassTrigger(
    val intervalFromRoot: Int, // 0 = root, 7 = fifth, 12 = octave, etc.
    val octaveOffset: Int = 0,
    val durationSteps: Int = 2,
    val velocity: Float = 0.8f
)

data class ChordTrigger(
    val durationSteps: Int = 4,
    val inversion: Int = 0,
    val velocity: Float = 0.75f,
    val spread: Boolean = false
)

data class StyleSectionData(
    val stepsPerMeasure: Int = 16,
    val measuresCount: Int = 1,
    // Step index -> list of drum hits
    val drumTriggers: Map<Int, List<DrumTrigger>> = emptyMap(),
    // Step index -> bass note
    val bassTriggers: Map<Int, BassTrigger> = emptyMap(),
    // Step index -> chord 1 (rhythmic comping)
    val chord1Triggers: Map<Int, ChordTrigger> = emptyMap(),
    // Step index -> chord 2 (arpeggios/counterpoint)
    val chord2Triggers: Map<Int, ChordTrigger> = emptyMap(),
    // Step index -> pad trigger
    val padTriggers: Map<Int, ChordTrigger> = emptyMap()
)

data class Style(
    val id: String,
    val name: String,
    val category: StyleCategory,
    val tempo: Int = 120,
    val timeSignatureNumerator: Int = 4,
    val timeSignatureDenominator: Int = 4,
    val sections: Map<StyleSection, StyleSectionData>
)

object StylePresets {
    private fun createPopSection(section: StyleSection): StyleSectionData {
        val drums = mutableMapOf<Int, MutableList<DrumTrigger>>()
        val bass = mutableMapOf<Int, BassTrigger>()
        val chord1 = mutableMapOf<Int, ChordTrigger>()
        val chord2 = mutableMapOf<Int, ChordTrigger>()
        val pad = mutableMapOf<Int, ChordTrigger>()

        fun addDrum(step: Int, sound: DrumSound, vel: Float = 0.85f) {
            drums.getOrPut(step) { mutableListOf() }.add(DrumTrigger(sound, vel))
        }

        when (section) {
            StyleSection.INTRO -> {
                for (s in 0 until 16 step 2) addDrum(s, DrumSound.HIHAT_CLOSED, 0.7f)
                addDrum(0, DrumSound.KICK, 0.9f)
                addDrum(8, DrumSound.KICK, 0.8f)
                addDrum(12, DrumSound.SNARE, 0.7f)
                addDrum(14, DrumSound.SNARE, 0.85f)
                pad[0] = ChordTrigger(16, 0, 0.7f)
            }
            StyleSection.MAIN_A -> {
                // Light groove: Kick on 0, 8, 10; Snare on 4, 12; 8th note hi-hats
                addDrum(0, DrumSound.KICK, 0.9f)
                addDrum(8, DrumSound.KICK, 0.85f)
                addDrum(10, DrumSound.KICK, 0.75f)
                addDrum(4, DrumSound.SNARE, 0.85f)
                addDrum(12, DrumSound.SNARE, 0.9f)
                for (s in 0 until 16 step 2) addDrum(s, DrumSound.HIHAT_CLOSED, if (s % 4 == 0) 0.8f else 0.6f)

                bass[0] = BassTrigger(0, 0, 3, 0.85f)
                bass[6] = BassTrigger(0, 0, 2, 0.75f)
                bass[8] = BassTrigger(7, 0, 2, 0.8f)
                bass[10] = BassTrigger(0, 0, 4, 0.85f)

                chord1[4] = ChordTrigger(2, 0, 0.7f)
                chord1[12] = ChordTrigger(3, 0, 0.75f)
                pad[0] = ChordTrigger(16, 0, 0.6f)
            }
            StyleSection.MAIN_B -> {
                // Slightly fuller: 16th hats, extra kick syncopation
                addDrum(0, DrumSound.KICK, 0.9f)
                addDrum(6, DrumSound.KICK, 0.75f)
                addDrum(8, DrumSound.KICK, 0.85f)
                addDrum(10, DrumSound.KICK, 0.8f)
                addDrum(4, DrumSound.SNARE, 0.9f)
                addDrum(12, DrumSound.SNARE, 0.9f)
                for (s in 0 until 16) {
                    if (s == 14) addDrum(s, DrumSound.HIHAT_OPEN, 0.7f)
                    else addDrum(s, DrumSound.HIHAT_CLOSED, if (s % 2 == 0) 0.8f else 0.5f)
                }

                bass[0] = BassTrigger(0, 0, 2, 0.9f)
                bass[3] = BassTrigger(0, 0, 2, 0.7f)
                bass[6] = BassTrigger(7, 0, 2, 0.8f)
                bass[8] = BassTrigger(0, 0, 2, 0.85f)
                bass[11] = BassTrigger(12, 0, 2, 0.75f)
                bass[14] = BassTrigger(7, 0, 2, 0.7f)

                chord1[2] = ChordTrigger(2, 0, 0.7f)
                chord1[4] = ChordTrigger(2, 0, 0.8f)
                chord1[10] = ChordTrigger(2, 0, 0.75f)
                chord1[12] = ChordTrigger(3, 0, 0.8f)
                chord2[0] = ChordTrigger(4, 1, 0.6f)
                chord2[8] = ChordTrigger(4, 0, 0.6f)
                pad[0] = ChordTrigger(16, 0, 0.65f)
            }
            StyleSection.MAIN_C -> {
                // Dynamic drive with ride cymbal and tambourine
                addDrum(0, DrumSound.KICK, 0.95f)
                addDrum(4, DrumSound.SNARE, 0.95f)
                addDrum(7, DrumSound.KICK, 0.8f)
                addDrum(8, DrumSound.KICK, 0.9f)
                addDrum(10, DrumSound.KICK, 0.85f)
                addDrum(12, DrumSound.SNARE, 0.95f)
                for (s in 0 until 16 step 2) addDrum(s, DrumSound.RIDE, 0.8f)
                for (s in 2 until 16 step 4) addDrum(s, DrumSound.TAMBOURINE, 0.6f)

                bass[0] = BassTrigger(0, 0, 2, 0.9f)
                bass[2] = BassTrigger(12, 0, 2, 0.75f)
                bass[4] = BassTrigger(0, 0, 2, 0.85f)
                bass[7] = BassTrigger(7, 0, 2, 0.8f)
                bass[8] = BassTrigger(0, 0, 2, 0.9f)
                bass[10] = BassTrigger(12, 0, 2, 0.8f)
                bass[12] = BassTrigger(7, 0, 2, 0.85f)
                bass[14] = BassTrigger(10, 0, 2, 0.8f)

                chord1[0] = ChordTrigger(3, 0, 0.75f)
                chord1[4] = ChordTrigger(3, 0, 0.85f)
                chord1[8] = ChordTrigger(3, 0, 0.8f)
                chord1[12] = ChordTrigger(3, 0, 0.85f)
                for (s in 0 until 16 step 4) chord2[s + 2] = ChordTrigger(2, 1, 0.65f)
                pad[0] = ChordTrigger(16, 0, 0.7f)
            }
            StyleSection.MAIN_D -> {
                // Maximum energy: crash accents, driving kick
                addDrum(0, DrumSound.CRASH, 0.85f)
                for (s in 0 until 16 step 4) addDrum(s, DrumSound.KICK, 0.95f)
                addDrum(4, DrumSound.SNARE, 0.95f)
                addDrum(6, DrumSound.KICK, 0.8f)
                addDrum(10, DrumSound.KICK, 0.85f)
                addDrum(12, DrumSound.SNARE, 0.95f)
                addDrum(14, DrumSound.SNARE, 0.75f)
                for (s in 0 until 16) addDrum(s, DrumSound.RIDE, 0.85f)

                for (s in 0 until 16 step 2) {
                    val interval = if (s % 4 == 0) 0 else if (s % 8 == 2) 7 else 12
                    bass[s] = BassTrigger(interval, 0, 2, 0.85f)
                }

                for (s in 0 until 16 step 2) {
                    chord1[s] = ChordTrigger(2, 0, 0.8f)
                }
                chord2[0] = ChordTrigger(8, 1, 0.7f)
                chord2[8] = ChordTrigger(8, 0, 0.7f)
                pad[0] = ChordTrigger(16, 0, 0.75f)
            }
            StyleSection.FILL_IN -> {
                addDrum(0, DrumSound.KICK, 0.9f)
                addDrum(4, DrumSound.SNARE, 0.85f)
                addDrum(8, DrumSound.HIGH_TOM, 0.9f)
                addDrum(10, DrumSound.MID_TOM, 0.9f)
                addDrum(12, DrumSound.LOW_TOM, 0.9f)
                addDrum(14, DrumSound.SNARE, 0.95f)
                addDrum(15, DrumSound.SNARE, 0.95f)
                bass[0] = BassTrigger(0, 0, 4, 0.9f)
                bass[8] = BassTrigger(7, 0, 4, 0.85f)
                chord1[0] = ChordTrigger(8, 0, 0.8f)
            }
            StyleSection.BREAK -> {
                addDrum(0, DrumSound.KICK, 0.95f)
                addDrum(0, DrumSound.CRASH, 0.85f)
                bass[0] = BassTrigger(0, 0, 4, 0.9f)
                chord1[0] = ChordTrigger(4, 0, 0.85f)
                // Rest of measure is silent
            }
            StyleSection.ENDING -> {
                addDrum(0, DrumSound.CRASH, 0.9f)
                addDrum(0, DrumSound.KICK, 0.95f)
                addDrum(4, DrumSound.SNARE, 0.9f)
                addDrum(8, DrumSound.KICK, 0.95f)
                addDrum(12, DrumSound.SNARE, 0.95f)
                addDrum(12, DrumSound.CRASH, 0.9f)
                bass[0] = BassTrigger(0, 0, 6, 0.95f)
                bass[8] = BassTrigger(7, 0, 4, 0.9f)
                bass[12] = BassTrigger(0, 0, 4, 1.0f)
                chord1[0] = ChordTrigger(6, 0, 0.85f)
                chord1[12] = ChordTrigger(8, 0, 0.95f)
                pad[0] = ChordTrigger(16, 0, 0.8f)
            }
        }

        return StyleSectionData(16, 1, drums, bass, chord1, chord2, pad)
    }

    private fun createRockSection(section: StyleSection): StyleSectionData {
        val drums = mutableMapOf<Int, MutableList<DrumTrigger>>()
        val bass = mutableMapOf<Int, BassTrigger>()
        val chord1 = mutableMapOf<Int, ChordTrigger>()
        val chord2 = mutableMapOf<Int, ChordTrigger>()
        val pad = mutableMapOf<Int, ChordTrigger>()

        fun addDrum(step: Int, sound: DrumSound, vel: Float = 0.9f) {
            drums.getOrPut(step) { mutableListOf() }.add(DrumTrigger(sound, vel))
        }

        when (section) {
            StyleSection.MAIN_A, StyleSection.MAIN_B -> {
                // Hard hitting driving rock
                addDrum(0, DrumSound.KICK, 1.0f)
                addDrum(4, DrumSound.SNARE, 1.0f)
                addDrum(8, DrumSound.KICK, 1.0f)
                addDrum(10, DrumSound.KICK, 0.85f)
                addDrum(12, DrumSound.SNARE, 1.0f)
                for (s in 0 until 16 step 2) addDrum(s, DrumSound.HIHAT_CLOSED, 0.8f)

                // 8th note driving bassline
                for (s in 0 until 16 step 2) {
                    val note = if (s == 14) 7 else 0
                    bass[s] = BassTrigger(note, 0, 2, 0.9f)
                }

                // Power chords on 0, 6, 8, 12
                chord1[0] = ChordTrigger(4, 0, 0.9f)
                chord1[6] = ChordTrigger(2, 0, 0.85f)
                chord1[8] = ChordTrigger(4, 0, 0.9f)
                chord1[12] = ChordTrigger(4, 0, 0.85f)
            }
            StyleSection.MAIN_C, StyleSection.MAIN_D -> {
                addDrum(0, DrumSound.CRASH, 0.95f)
                for (s in 0 until 16 step 4) addDrum(s, DrumSound.KICK, 1.0f)
                addDrum(4, DrumSound.SNARE, 1.0f)
                addDrum(6, DrumSound.KICK, 0.9f)
                addDrum(12, DrumSound.SNARE, 1.0f)
                addDrum(14, DrumSound.KICK, 0.9f)
                for (s in 0 until 16 step 2) addDrum(s, DrumSound.RIDE, 0.9f)

                for (s in 0 until 16 step 2) {
                    val note = if (s in 6..10) 7 else 0
                    bass[s] = BassTrigger(note, 0, 2, 0.95f)
                }

                for (s in 0 until 16 step 4) {
                    chord1[s] = ChordTrigger(3, 0, 0.95f)
                    chord2[s + 2] = ChordTrigger(2, 1, 0.85f)
                }
                pad[0] = ChordTrigger(16, 0, 0.6f)
            }
            StyleSection.FILL_IN -> {
                addDrum(0, DrumSound.KICK, 1.0f)
                addDrum(4, DrumSound.SNARE, 0.9f)
                addDrum(6, DrumSound.SNARE, 0.95f)
                addDrum(8, DrumSound.HIGH_TOM, 0.95f)
                addDrum(10, DrumSound.MID_TOM, 0.95f)
                addDrum(12, DrumSound.LOW_TOM, 1.0f)
                addDrum(14, DrumSound.SNARE, 1.0f)
                bass[0] = BassTrigger(0, 0, 8, 1.0f)
                chord1[0] = ChordTrigger(8, 0, 0.9f)
            }
            else -> {
                return createPopSection(section)
            }
        }
        return StyleSectionData(16, 1, drums, bass, chord1, chord2, pad)
    }

    private fun createLatinSection(section: StyleSection): StyleSectionData {
        val drums = mutableMapOf<Int, MutableList<DrumTrigger>>()
        val bass = mutableMapOf<Int, BassTrigger>()
        val chord1 = mutableMapOf<Int, ChordTrigger>()
        val chord2 = mutableMapOf<Int, ChordTrigger>()
        val pad = mutableMapOf<Int, ChordTrigger>()

        fun addDrum(step: Int, sound: DrumSound, vel: Float = 0.8f) {
            drums.getOrPut(step) { mutableListOf() }.add(DrumTrigger(sound, vel))
        }

        when (section) {
            StyleSection.MAIN_A, StyleSection.MAIN_B, StyleSection.MAIN_C, StyleSection.MAIN_D -> {
                // Bossa / Latin syncopation
                addDrum(0, DrumSound.KICK, 0.85f)
                addDrum(6, DrumSound.KICK, 0.8f)
                addDrum(8, DrumSound.KICK, 0.85f)
                addDrum(14, DrumSound.KICK, 0.8f)
                // Cross stick clave
                addDrum(0, DrumSound.SIDE_STICK, 0.85f)
                addDrum(6, DrumSound.SIDE_STICK, 0.8f)
                addDrum(10, DrumSound.SIDE_STICK, 0.85f)
                addDrum(12, DrumSound.SIDE_STICK, 0.85f)
                // Shaker / Hi-Hat
                for (s in 0 until 16) addDrum(s, DrumSound.SHAKER, if (s % 2 == 1) 0.75f else 0.5f)

                // Root - Fifth anticipatory bass
                bass[0] = BassTrigger(0, 0, 4, 0.85f)
                bass[6] = BassTrigger(7, 0, 2, 0.8f)
                bass[8] = BassTrigger(7, 0, 4, 0.85f)
                bass[14] = BassTrigger(0, 0, 2, 0.8f)

                // Piano syncopated comping
                chord1[0] = ChordTrigger(2, 0, 0.7f)
                chord1[5] = ChordTrigger(3, 0, 0.75f)
                chord1[8] = ChordTrigger(2, 0, 0.7f)
                chord1[11] = ChordTrigger(3, 0, 0.75f)
                pad[0] = ChordTrigger(16, 0, 0.55f)
            }
            else -> return createPopSection(section)
        }
        return StyleSectionData(16, 1, drums, bass, chord1, chord2, pad)
    }

    private fun createJazzSection(section: StyleSection): StyleSectionData {
        val drums = mutableMapOf<Int, MutableList<DrumTrigger>>()
        val bass = mutableMapOf<Int, BassTrigger>()
        val chord1 = mutableMapOf<Int, ChordTrigger>()
        val chord2 = mutableMapOf<Int, ChordTrigger>()
        val pad = mutableMapOf<Int, ChordTrigger>()

        fun addDrum(step: Int, sound: DrumSound, vel: Float = 0.75f) {
            drums.getOrPut(step) { mutableListOf() }.add(DrumTrigger(sound, vel))
        }

        when (section) {
            StyleSection.MAIN_A, StyleSection.MAIN_B, StyleSection.MAIN_C, StyleSection.MAIN_D -> {
                // Jazz Ride pattern: 1, 2, 2-and, 3, 4, 4-and (in 16ths: 0, 4, 7, 8, 12, 15)
                addDrum(0, DrumSound.RIDE, 0.8f)
                addDrum(4, DrumSound.RIDE, 0.85f)
                addDrum(7, DrumSound.RIDE, 0.7f)
                addDrum(8, DrumSound.RIDE, 0.8f)
                addDrum(12, DrumSound.RIDE, 0.85f)
                addDrum(15, DrumSound.RIDE, 0.7f)
                // Hi-hat pedal on 2 and 4 (steps 4 and 12)
                addDrum(4, DrumSound.HIHAT_PEDAL, 0.9f)
                addDrum(12, DrumSound.HIHAT_PEDAL, 0.9f)
                // Feathered kick on all four beats
                addDrum(0, DrumSound.KICK, 0.5f)
                addDrum(4, DrumSound.KICK, 0.45f)
                addDrum(8, DrumSound.KICK, 0.5f)
                addDrum(12, DrumSound.KICK, 0.45f)

                // Walking bass: quarter notes on 0, 4, 8, 12
                bass[0] = BassTrigger(0, 0, 4, 0.85f)
                bass[4] = BassTrigger(4, 0, 4, 0.8f)
                bass[8] = BassTrigger(7, 0, 4, 0.85f)
                bass[12] = BassTrigger(10, 0, 4, 0.8f)

                // Freddie Green / Bill Evans style comping on offbeats
                chord1[3] = ChordTrigger(2, 0, 0.7f)
                chord1[7] = ChordTrigger(3, 0, 0.75f)
                chord1[11] = ChordTrigger(2, 0, 0.7f)
            }
            else -> return createPopSection(section)
        }
        return StyleSectionData(16, 1, drums, bass, chord1, chord2, pad)
    }

    private fun createDanceSection(section: StyleSection): StyleSectionData {
        val drums = mutableMapOf<Int, MutableList<DrumTrigger>>()
        val bass = mutableMapOf<Int, BassTrigger>()
        val chord1 = mutableMapOf<Int, ChordTrigger>()
        val chord2 = mutableMapOf<Int, ChordTrigger>()
        val pad = mutableMapOf<Int, ChordTrigger>()

        fun addDrum(step: Int, sound: DrumSound, vel: Float = 0.9f) {
            drums.getOrPut(step) { mutableListOf() }.add(DrumTrigger(sound, vel))
        }

        when (section) {
            StyleSection.MAIN_A, StyleSection.MAIN_B, StyleSection.MAIN_C, StyleSection.MAIN_D -> {
                // Four-on-the-floor
                for (s in 0 until 16 step 4) addDrum(s, DrumSound.KICK, 1.0f)
                addDrum(4, DrumSound.CLAP, 0.9f)
                addDrum(12, DrumSound.CLAP, 0.95f)
                // Offbeat open hi-hat
                for (s in 2 until 16 step 4) addDrum(s, DrumSound.HIHAT_OPEN, 0.85f)
                for (s in 0 until 16 step 2) addDrum(s, DrumSound.HIHAT_CLOSED, 0.6f)

                // Rolling 16th synth bass
                for (s in 0 until 16) {
                    val note = if (s % 4 == 0) 0 else if (s % 2 == 1) 12 else 7
                    bass[s] = BassTrigger(note, 0, 1, if (s % 4 == 0) 0.9f else 0.7f)
                }

                // Plucked arpeggio / stabs
                chord1[2] = ChordTrigger(2, 0, 0.8f)
                chord1[6] = ChordTrigger(2, 0, 0.8f)
                chord1[10] = ChordTrigger(2, 0, 0.8f)
                chord1[14] = ChordTrigger(2, 0, 0.8f)
                pad[0] = ChordTrigger(16, 0, 0.65f)
            }
            else -> return createPopSection(section)
        }
        return StyleSectionData(16, 1, drums, bass, chord1, chord2, pad)
    }

    val ALL_STYLES = listOf(
        Style(
            id = "pop_8beat",
            name = "Pop 8-Beat Hit",
            category = StyleCategory.POP,
            tempo = 118,
            sections = mapOf(
                StyleSection.INTRO to createPopSection(StyleSection.INTRO),
                StyleSection.MAIN_A to createPopSection(StyleSection.MAIN_A),
                StyleSection.MAIN_B to createPopSection(StyleSection.MAIN_B),
                StyleSection.MAIN_C to createPopSection(StyleSection.MAIN_C),
                StyleSection.MAIN_D to createPopSection(StyleSection.MAIN_D),
                StyleSection.FILL_IN to createPopSection(StyleSection.FILL_IN),
                StyleSection.BREAK to createPopSection(StyleSection.BREAK),
                StyleSection.ENDING to createPopSection(StyleSection.ENDING)
            )
        ),
        Style(
            id = "rock_driving",
            name = "Classic Rock Drive",
            category = StyleCategory.ROCK,
            tempo = 132,
            sections = mapOf(
                StyleSection.INTRO to createRockSection(StyleSection.INTRO),
                StyleSection.MAIN_A to createRockSection(StyleSection.MAIN_A),
                StyleSection.MAIN_B to createRockSection(StyleSection.MAIN_B),
                StyleSection.MAIN_C to createRockSection(StyleSection.MAIN_C),
                StyleSection.MAIN_D to createRockSection(StyleSection.MAIN_D),
                StyleSection.FILL_IN to createRockSection(StyleSection.FILL_IN),
                StyleSection.BREAK to createRockSection(StyleSection.BREAK),
                StyleSection.ENDING to createRockSection(StyleSection.ENDING)
            )
        ),
        Style(
            id = "ballad_acoustic",
            name = "Warm 16-Beat Ballad",
            category = StyleCategory.BALLAD,
            tempo = 76,
            sections = mapOf(
                StyleSection.INTRO to createPopSection(StyleSection.INTRO),
                StyleSection.MAIN_A to createPopSection(StyleSection.MAIN_A),
                StyleSection.MAIN_B to createPopSection(StyleSection.MAIN_B),
                StyleSection.MAIN_C to createPopSection(StyleSection.MAIN_C),
                StyleSection.MAIN_D to createPopSection(StyleSection.MAIN_D),
                StyleSection.FILL_IN to createPopSection(StyleSection.FILL_IN),
                StyleSection.BREAK to createPopSection(StyleSection.BREAK),
                StyleSection.ENDING to createPopSection(StyleSection.ENDING)
            )
        ),
        Style(
            id = "dance_club",
            name = "EDM Club Floor",
            category = StyleCategory.DANCE,
            tempo = 128,
            sections = mapOf(
                StyleSection.INTRO to createDanceSection(StyleSection.INTRO),
                StyleSection.MAIN_A to createDanceSection(StyleSection.MAIN_A),
                StyleSection.MAIN_B to createDanceSection(StyleSection.MAIN_B),
                StyleSection.MAIN_C to createDanceSection(StyleSection.MAIN_C),
                StyleSection.MAIN_D to createDanceSection(StyleSection.MAIN_D),
                StyleSection.FILL_IN to createDanceSection(StyleSection.FILL_IN),
                StyleSection.BREAK to createDanceSection(StyleSection.BREAK),
                StyleSection.ENDING to createDanceSection(StyleSection.ENDING)
            )
        ),
        Style(
            id = "latin_bossa",
            name = "Bossa Nova Breeze",
            category = StyleCategory.LATIN,
            tempo = 124,
            sections = mapOf(
                StyleSection.INTRO to createLatinSection(StyleSection.INTRO),
                StyleSection.MAIN_A to createLatinSection(StyleSection.MAIN_A),
                StyleSection.MAIN_B to createLatinSection(StyleSection.MAIN_B),
                StyleSection.MAIN_C to createLatinSection(StyleSection.MAIN_C),
                StyleSection.MAIN_D to createLatinSection(StyleSection.MAIN_D),
                StyleSection.FILL_IN to createLatinSection(StyleSection.FILL_IN),
                StyleSection.BREAK to createLatinSection(StyleSection.BREAK),
                StyleSection.ENDING to createLatinSection(StyleSection.ENDING)
            )
        ),
        Style(
            id = "jazz_swing",
            name = "Acoustic Jazz Trio",
            category = StyleCategory.JAZZ,
            tempo = 140,
            sections = mapOf(
                StyleSection.INTRO to createJazzSection(StyleSection.INTRO),
                StyleSection.MAIN_A to createJazzSection(StyleSection.MAIN_A),
                StyleSection.MAIN_B to createJazzSection(StyleSection.MAIN_B),
                StyleSection.MAIN_C to createJazzSection(StyleSection.MAIN_C),
                StyleSection.MAIN_D to createJazzSection(StyleSection.MAIN_D),
                StyleSection.FILL_IN to createJazzSection(StyleSection.FILL_IN),
                StyleSection.BREAK to createJazzSection(StyleSection.BREAK),
                StyleSection.ENDING to createJazzSection(StyleSection.ENDING)
            )
        ),
        Style(
            id = "rnb_groove",
            name = "Neo-Soul R&B Pocket",
            category = StyleCategory.RNB,
            tempo = 90,
            sections = mapOf(
                StyleSection.INTRO to createPopSection(StyleSection.INTRO),
                StyleSection.MAIN_A to createPopSection(StyleSection.MAIN_A),
                StyleSection.MAIN_B to createPopSection(StyleSection.MAIN_B),
                StyleSection.MAIN_C to createPopSection(StyleSection.MAIN_C),
                StyleSection.MAIN_D to createPopSection(StyleSection.MAIN_D),
                StyleSection.FILL_IN to createPopSection(StyleSection.FILL_IN),
                StyleSection.BREAK to createPopSection(StyleSection.BREAK),
                StyleSection.ENDING to createPopSection(StyleSection.ENDING)
            )
        ),
        Style(
            id = "world_reggae",
            name = "Sunshine Reggae One-Drop",
            category = StyleCategory.WORLD,
            tempo = 82,
            sections = mapOf(
                StyleSection.INTRO to createPopSection(StyleSection.INTRO),
                StyleSection.MAIN_A to createPopSection(StyleSection.MAIN_A),
                StyleSection.MAIN_B to createPopSection(StyleSection.MAIN_B),
                StyleSection.MAIN_C to createPopSection(StyleSection.MAIN_C),
                StyleSection.MAIN_D to createPopSection(StyleSection.MAIN_D),
                StyleSection.FILL_IN to createPopSection(StyleSection.FILL_IN),
                StyleSection.BREAK to createPopSection(StyleSection.BREAK),
                StyleSection.ENDING to createPopSection(StyleSection.ENDING)
            )
        )
    )

    fun getById(id: String): Style {
        return ALL_STYLES.firstOrNull { it.id == id } ?: ALL_STYLES.first()
    }
}
