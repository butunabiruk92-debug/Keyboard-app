package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RegistrationEntity::class,
        RecordedSongEntity::class,
        ConsoleSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ArrangerDatabase : RoomDatabase() {
    abstract fun arrangerDao(): ArrangerDao

    companion object {
        @Volatile
        private var INSTANCE: ArrangerDatabase? = null

        fun getDatabase(context: Context): ArrangerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArrangerDatabase::class.java,
                    "arranger_pro_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
