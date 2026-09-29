package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.*

@Entity(tableName = "registrations")
data class RegistrationEntity(
    @PrimaryKey val id: String, // e.g. "B1_S1"
    val bankNumber: Int,
    val slotNumber: Int,
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
    val masterVolume: Float,
    val balance: Float,
    val reverbSend: Float,
    val reverbRoom: Float,
    val chorusEnabled: Boolean,
    val delayEnabled: Boolean
)

@Entity(tableName = "recorded_songs")
data class RecordedSongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val timestamp: Long,
    val durationMs: Long,
    val tempo: Int,
    val styleId: String,
    val eventsJson: String
)

@Entity(tableName = "console_settings")
data class ConsoleSettingsEntity(
    @PrimaryKey val id: String = "active_settings",
    val styleId: String = "pop_8beat",
    val tempo: Int = 118,
    val voiceR1Id: String = "grand_concert",
    val voiceR2Id: String = "strings_symphony",
    val voiceLeftId: String = "bass_electric",
    val isSplit: Boolean = true,
    val isLayer: Boolean = false,
    val splitPoint: Int = 60, // C3
    val transpose: Int = 0,
    val octave: Int = 0,
    val masterVolume: Float = 0.85f,
    val balance: Float = 0.5f,
    val micVolume: Float = 0.7f,
    val activeScreen: String = "HOME"
)
