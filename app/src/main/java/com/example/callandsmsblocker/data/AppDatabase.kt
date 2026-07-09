package com.example.callandsmsblocker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room

@Database(entities = [InterceptLogEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    abstract fun interceptLogDao(): InterceptLogDao

    companion object {
        private const val NAME = "call_blocker_db"
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME)
                .allowMainThreadQueries() // Simplified for sample; use background threads in production
                .build().also { INSTANCE = it }
        }
    }
}
