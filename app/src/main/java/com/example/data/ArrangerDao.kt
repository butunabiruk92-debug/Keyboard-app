package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArrangerDao {
    // Registrations
    @Query("SELECT * FROM registrations WHERE bankNumber = :bank ORDER BY slotNumber ASC")
    fun getRegistrationsForBank(bank: Int): Flow<List<RegistrationEntity>>

    @Query("SELECT * FROM registrations")
    fun getAllRegistrations(): Flow<List<RegistrationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistration(registration: RegistrationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistrations(registrations: List<RegistrationEntity>)

    @Query("DELETE FROM registrations WHERE id = :id")
    suspend fun deleteRegistration(id: String)

    // Songs
    @Query("SELECT * FROM recorded_songs ORDER BY timestamp DESC")
    fun getAllSongs(): Flow<List<RecordedSongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: RecordedSongEntity)

    @Query("DELETE FROM recorded_songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    // Console Settings
    @Query("SELECT * FROM console_settings WHERE id = 'active_settings' LIMIT 1")
    fun getConsoleSettings(): Flow<ConsoleSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConsoleSettings(settings: ConsoleSettingsEntity)
}
