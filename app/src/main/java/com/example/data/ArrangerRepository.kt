package com.example.data

import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ArrangerRepository(private val dao: ArrangerDao) {
    val registrations: Flow<List<RegistrationEntity>> = dao.getAllRegistrations()
    val recordedSongs: Flow<List<RecordedSongEntity>> = dao.getAllSongs()
    val savedSettings: Flow<ConsoleSettingsEntity?> = dao.getConsoleSettings()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = dao.getAllRegistrations().firstOrNull()
            if (existing.isNullOrEmpty()) {
                seedFactoryRegistrations()
            }
        }
    }

    private suspend fun seedFactoryRegistrations() {
        val factory = listOf(
            RegistrationEntity(
                id = "B1_S1", bankNumber = 1, slotNumber = 1, name = "Grand Ballad",
                styleId = "ballad_acoustic", tempo = 76, voiceRight1Id = "grand_concert",
                voiceRight2Id = "strings_symphony", voiceLeftId = "bass_acoustic",
                isSplit = true, isLayer = true, splitPoint = 60, transpose = 0, octave = 0,
                masterVolume = 0.85f, balance = 0.5f, reverbSend = 0.4f, reverbRoom = 0.7f,
                chorusEnabled = true, delayEnabled = false
            ),
            RegistrationEntity(
                id = "B1_S2", bankNumber = 1, slotNumber = 2, name = "Classic Rock",
                styleId = "rock_driving", tempo = 132, voiceRight1Id = "organ_rock",
                voiceRight2Id = "brass_section", voiceLeftId = "bass_slap",
                isSplit = true, isLayer = false, splitPoint = 60, transpose = 0, octave = 0,
                masterVolume = 0.9f, balance = 0.5f, reverbSend = 0.25f, reverbRoom = 0.5f,
                chorusEnabled = false, delayEnabled = true
            ),
            RegistrationEntity(
                id = "B1_S3", bankNumber = 1, slotNumber = 3, name = "Jazz Lounge",
                styleId = "jazz_swing", tempo = 140, voiceRight1Id = "ep_rhodes",
                voiceRight2Id = "ww_saxophone", voiceLeftId = "bass_acoustic",
                isSplit = true, isLayer = false, splitPoint = 60, transpose = 0, octave = 0,
                masterVolume = 0.8f, balance = 0.55f, reverbSend = 0.45f, reverbRoom = 0.75f,
                chorusEnabled = true, delayEnabled = false
            ),
            RegistrationEntity(
                id = "B1_S4", bankNumber = 1, slotNumber = 4, name = "EDM Anthem",
                styleId = "dance_club", tempo = 128, voiceRight1Id = "synth_saw_lead",
                voiceRight2Id = "pad_shimmer", voiceLeftId = "bass_synth",
                isSplit = true, isLayer = true, splitPoint = 55, transpose = 0, octave = 0,
                masterVolume = 0.88f, balance = 0.5f, reverbSend = 0.35f, reverbRoom = 0.6f,
                chorusEnabled = true, delayEnabled = true
            ),
            RegistrationEntity(
                id = "B1_S5", bankNumber = 1, slotNumber = 5, name = "Pop 8-Beat Hit",
                styleId = "pop_8beat", tempo = 118, voiceRight1Id = "grand_bright",
                voiceRight2Id = "pad_warm", voiceLeftId = "bass_electric",
                isSplit = true, isLayer = true, splitPoint = 60, transpose = 0, octave = 0,
                masterVolume = 0.85f, balance = 0.5f, reverbSend = 0.35f, reverbRoom = 0.65f,
                chorusEnabled = true, delayEnabled = false
            ),
            RegistrationEntity(
                id = "B1_S6", bankNumber = 1, slotNumber = 6, name = "Bossa Cafe",
                styleId = "latin_bossa", tempo = 124, voiceRight1Id = "ww_flute",
                voiceRight2Id = "strings_chamber", voiceLeftId = "bass_acoustic",
                isSplit = true, isLayer = false, splitPoint = 60, transpose = 0, octave = 0,
                masterVolume = 0.82f, balance = 0.5f, reverbSend = 0.5f, reverbRoom = 0.8f,
                chorusEnabled = true, delayEnabled = false
            ),
            RegistrationEntity(
                id = "B1_S7", bankNumber = 1, slotNumber = 7, name = "Gospel Praise",
                styleId = "rnb_groove", tempo = 90, voiceRight1Id = "organ_gospel",
                voiceRight2Id = "brass_section", voiceLeftId = "bass_electric",
                isSplit = true, isLayer = true, splitPoint = 60, transpose = 0, octave = 0,
                masterVolume = 0.85f, balance = 0.5f, reverbSend = 0.4f, reverbRoom = 0.75f,
                chorusEnabled = true, delayEnabled = false
            ),
            RegistrationEntity(
                id = "B1_S8", bankNumber = 1, slotNumber = 8, name = "Island Sunset",
                styleId = "world_reggae", tempo = 82, voiceRight1Id = "ep_clav",
                voiceRight2Id = "organ_jazz", voiceLeftId = "bass_electric",
                isSplit = true, isLayer = false, splitPoint = 60, transpose = 0, octave = 0,
                masterVolume = 0.85f, balance = 0.5f, reverbSend = 0.35f, reverbRoom = 0.6f,
                chorusEnabled = false, delayEnabled = true
            )
        )
        dao.insertRegistrations(factory)
    }

    suspend fun saveRegistration(reg: RegistrationEntity) {
        dao.insertRegistration(reg)
    }

    suspend fun saveSettings(settings: ConsoleSettingsEntity) {
        dao.saveConsoleSettings(settings)
    }

    suspend fun saveSong(song: SongRecording) {
        val jsonArray = JSONArray()
        for (ev in song.events) {
            val obj = JSONObject()
            obj.put("t", ev.timestampMs)
            obj.put("n", ev.note)
            obj.put("v", ev.velocity.toDouble())
            obj.put("on", ev.isNoteOn)
            obj.put("p", ev.part.name)
            jsonArray.put(obj)
        }
        val entity = RecordedSongEntity(
            id = song.id,
            title = song.title,
            timestamp = song.timestamp,
            durationMs = song.durationMs,
            tempo = song.tempo,
            styleId = song.styleId,
            eventsJson = jsonArray.toString()
        )
        dao.insertSong(entity)
    }

    suspend fun deleteSong(id: String) {
        dao.deleteSong(id)
    }

    fun parseSongEntity(entity: RecordedSongEntity): SongRecording {
        val events = mutableListOf<RecordedNoteEvent>()
        try {
            val jsonArray = JSONArray(entity.eventsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val partStr = obj.optString("p", "RIGHT1")
                val part = try { KeyboardPart.valueOf(partStr) } catch (_: Exception) { KeyboardPart.RIGHT1 }
                events.add(
                    RecordedNoteEvent(
                        timestampMs = obj.getLong("t"),
                        note = obj.getInt("n"),
                        velocity = obj.getDouble("v").toFloat(),
                        isNoteOn = obj.getBoolean("on"),
                        part = part
                    )
                )
            }
        } catch (_: Exception) {}
        return SongRecording(
            id = entity.id,
            title = entity.title,
            timestamp = entity.timestamp,
            durationMs = entity.durationMs,
            tempo = entity.tempo,
            styleId = entity.styleId,
            events = events
        )
    }
}
