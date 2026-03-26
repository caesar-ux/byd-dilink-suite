package com.byd.dilink.extras.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.byd.dilink.extras.core.data.entities.OdometerEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface OdometerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: OdometerEntry): Long

    @Query("SELECT * FROM odometer_entries ORDER BY date DESC LIMIT 1")
    fun getLatest(): Flow<OdometerEntry?>

    @Query("SELECT * FROM odometer_entries ORDER BY date DESC LIMIT 1")
    suspend fun getLatestOnce(): OdometerEntry?

    @Query("SELECT * FROM odometer_entries ORDER BY date ASC")
    fun getAll(): Flow<List<OdometerEntry>>

    @Query("SELECT * FROM odometer_entries ORDER BY date ASC")
    suspend fun getAllOnce(): List<OdometerEntry>
}
