package com.example.callandsmsblocker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface InterceptLogDao {
    @Insert
    fun insert(entry: InterceptLogEntity)

    @Query("SELECT * FROM intercept_logs ORDER BY timestamp DESC")
    fun getAll(): List<InterceptLogEntity>

    @Query("SELECT * FROM intercept_logs WHERE type = :type ORDER BY timestamp DESC")
    fun getByType(type: String): List<InterceptLogEntity>

    @Query("SELECT * FROM intercept_logs WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getByTimeRange(from: Long, to: Long): List<InterceptLogEntity>

    @Query("SELECT * FROM intercept_logs WHERE type = :type AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getByTypeAndTime(type: String, from: Long, to: Long): List<InterceptLogEntity>

    @Query("DELETE FROM intercept_logs")
    fun clearAll()
}
