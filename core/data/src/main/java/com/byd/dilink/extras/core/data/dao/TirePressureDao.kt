package com.byd.dilink.extras.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.byd.dilink.extras.core.data.entities.TirePressureRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TirePressureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TirePressureRecord): Long

    @Query("SELECT * FROM tire_pressure_records ORDER BY date DESC LIMIT 1")
    fun getLatest(): Flow<TirePressureRecord?>

    @Query("SELECT * FROM tire_pressure_records ORDER BY date DESC")
    fun getAllByDate(): Flow<List<TirePressureRecord>>

    @Query("SELECT * FROM tire_pressure_records ORDER BY date DESC")
    suspend fun getAllByDateOnce(): List<TirePressureRecord>

    @Query("SELECT * FROM tire_pressure_records WHERE flBar > 0 ORDER BY date DESC")
    fun getForFrontLeft(): Flow<List<TirePressureRecord>>

    @Query("SELECT * FROM tire_pressure_records WHERE frBar > 0 ORDER BY date DESC")
    fun getForFrontRight(): Flow<List<TirePressureRecord>>

    @Query("SELECT * FROM tire_pressure_records WHERE rlBar > 0 ORDER BY date DESC")
    fun getForRearLeft(): Flow<List<TirePressureRecord>>

    @Query("SELECT * FROM tire_pressure_records WHERE rrBar > 0 ORDER BY date DESC")
    fun getForRearRight(): Flow<List<TirePressureRecord>>
}
