package com.example.callandsmsblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intercept_logs")
data class InterceptLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "call" or "sms"
    val number: String,
    val timestamp: Long,
    val content: String?
)
