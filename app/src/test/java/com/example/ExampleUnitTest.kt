package com.example

import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testChordDetector_majorTriad() {
        // C Major = C (60), E (64), G (67)
        val chord = ChordDetector.detectChord(setOf(60, 64, 67))
        assertNotNull(chord)
        assertEquals("C", chord?.rootName)
        assertEquals(ChordType.MAJOR, chord?.type)
        assertEquals("C", chord?.displayName)
    }

    @Test
    fun testChordDetector_minorTriad() {
        // A Minor = A (57), C (60), E (64)
        val chord = ChordDetector.detectChord(setOf(57, 60, 64))
        assertNotNull(chord)
        assertEquals("A", chord?.rootName)
        assertEquals(ChordType.MINOR, chord?.type)
        assertEquals("Am", chord?.displayName)
    }

    @Test
    fun testChordDetector_dominant7th() {
        // G7 = G (55), B (59), D (62), F (65)
        val chord = ChordDetector.detectChord(setOf(55, 59, 62, 65))
        assertNotNull(chord)
        assertEquals("G", chord?.rootName)
        assertEquals(ChordType.DOMINANT_7, chord?.type)
        assertEquals("G7", chord?.displayName)
    }

    @Test
    fun testChordDetector_singleFinger() {
        val chord = ChordDetector.detectChord(setOf(60), singleFingerMode = true)
        assertNotNull(chord)
        assertEquals("C", chord?.rootName)
        assertEquals(ChordType.MAJOR, chord?.type)
    }

    @Test
    fun testVoicePresets() {
        assertTrue(VoicePresets.ALL_VOICES.isNotEmpty())
        val categories = VoicePresets.ALL_VOICES.map { it.category }.toSet()
        assertEquals(VoiceCategory.values().toSet(), categories)
    }

    @Test
    fun testStylePresets() {
        assertTrue(StylePresets.ALL_STYLES.isNotEmpty())
        val firstStyle = StylePresets.ALL_STYLES.first()
        assertTrue(firstStyle.sections.containsKey(StyleSection.MAIN_A))
        assertTrue(firstStyle.sections.containsKey(StyleSection.INTRO))
        assertTrue(firstStyle.sections.containsKey(StyleSection.ENDING))
    }
}
