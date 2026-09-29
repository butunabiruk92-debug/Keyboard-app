package com.example.model

enum class ChordType(val symbol: String, val intervals: List<Int>) {
    MAJOR("", listOf(0, 4, 7)),
    MINOR("m", listOf(0, 3, 7)),
    DOMINANT_7("7", listOf(0, 4, 7, 10)),
    MINOR_7("m7", listOf(0, 3, 7, 10)),
    MAJOR_7("maj7", listOf(0, 4, 7, 11)),
    DIMINISHED("dim", listOf(0, 3, 6)),
    SUS_4("sus4", listOf(0, 5, 7)),
    FIFTH("5", listOf(0, 7))
}

data class ChordInfo(
    val rootNote: Int, // 0 to 11 (0 = C, 1 = C#, etc.)
    val type: ChordType = ChordType.MAJOR,
    val bassNote: Int = rootNote // 0 to 11
) {
    val rootName: String get() = NOTE_NAMES[rootNote % 12]
    val bassName: String get() = NOTE_NAMES[bassNote % 12]
    val displayName: String get() {
        val base = "$rootName${type.symbol}"
        return if (bassNote != rootNote) "$base/$bassName" else base
    }

    /**
     * Returns absolute MIDI notes for this chord in octave 4 (or baseOctave)
     */
    fun getVoicingNotes(baseOctave: Int = 4): List<Int> {
        val rootMidi = (baseOctave + 1) * 12 + rootNote
        return type.intervals.map { rootMidi + it }
    }

    companion object {
        val NOTE_NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val DEFAULT = ChordInfo(0, ChordType.MAJOR, 0)
    }
}

object ChordDetector {
    /**
     * Detects chord from a set of currently held MIDI note numbers.
     */
    fun detectChord(notes: Set<Int>, singleFingerMode: Boolean = false): ChordInfo? {
        if (notes.isEmpty()) return null

        val sorted = notes.sorted()

        // Single finger mode: 1 note = Major, 2 notes = check for 7th or Minor
        if (singleFingerMode && sorted.size == 1) {
            val root = sorted[0] % 12
            return ChordInfo(root, ChordType.MAJOR, root)
        }

        if (sorted.size == 1) {
            val root = sorted[0] % 12
            return ChordInfo(root, ChordType.MAJOR, root)
        }

        val lowestMidi = sorted.first()
        val bassNote = lowestMidi % 12

        // Normalized pitch classes (0..11)
        val pitchClasses = sorted.map { it % 12 }.toSet()

        // Check each possible pitch class as candidate root
        for (candidateRoot in pitchClasses) {
            val relativeIntervals = pitchClasses.map { (it - candidateRoot + 12) % 12 }.toSet()

            // Check against known chord types
            if (relativeIntervals.containsAll(listOf(0, 4, 7, 11))) {
                return ChordInfo(candidateRoot, ChordType.MAJOR_7, bassNote)
            }
            if (relativeIntervals.containsAll(listOf(0, 4, 7, 10))) {
                return ChordInfo(candidateRoot, ChordType.DOMINANT_7, bassNote)
            }
            if (relativeIntervals.containsAll(listOf(0, 3, 7, 10))) {
                return ChordInfo(candidateRoot, ChordType.MINOR_7, bassNote)
            }
            if (relativeIntervals.containsAll(listOf(0, 4, 7))) {
                return ChordInfo(candidateRoot, ChordType.MAJOR, bassNote)
            }
            if (relativeIntervals.containsAll(listOf(0, 3, 7))) {
                return ChordInfo(candidateRoot, ChordType.MINOR, bassNote)
            }
            if (relativeIntervals.containsAll(listOf(0, 3, 6))) {
                return ChordInfo(candidateRoot, ChordType.DIMINISHED, bassNote)
            }
            if (relativeIntervals.containsAll(listOf(0, 5, 7))) {
                return ChordInfo(candidateRoot, ChordType.SUS_4, bassNote)
            }
        }

        // Two notes chord
        if (pitchClasses.size == 2) {
            val list = pitchClasses.toList()
            val diff1 = (list[1] - list[0] + 12) % 12
            val diff2 = (list[0] - list[1] + 12) % 12
            if (diff1 == 7) return ChordInfo(list[0], ChordType.FIFTH, bassNote)
            if (diff2 == 7) return ChordInfo(list[1], ChordType.FIFTH, bassNote)
            if (diff1 == 4) return ChordInfo(list[0], ChordType.MAJOR, bassNote)
            if (diff1 == 3) return ChordInfo(list[0], ChordType.MINOR, bassNote)
        }

        // Fallback: root is lowest note, major triad
        return ChordInfo(bassNote, ChordType.MAJOR, bassNote)
    }
}
