package com.byd.dilink.extras.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.byd.dilink.extras.core.data.entities.TireRotationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TireRotationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TireRotationRecord): Long

    @Query("SELECT * FROM tire_rotation_records ORDER BY date DESC LIMIT 1")
    fun getLatest(): Flow<TireRotationRecord?>

    @Query("SELECT * FROM tire_rotation_records ORDER BY date ASC")
    fun getAll(): Flow<List<TireRotationRecord>>
}
