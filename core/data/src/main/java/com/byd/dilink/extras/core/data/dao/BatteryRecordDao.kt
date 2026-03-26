package com.byd.dilink.extras.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.byd.dilink.extras.core.data.entities.BatteryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BatteryRecord): Long

    @Query("SELECT * FROM battery_records ORDER BY date DESC LIMIT 1")
    fun getLatest(): Flow<BatteryRecord?>

    @Query("SELECT * FROM battery_records ORDER BY date DESC")
    fun getAllByDate(): Flow<List<BatteryRecord>>

    @Query("SELECT * FROM battery_records ORDER BY date DESC")
    suspend fun getAllByDateOnce(): List<BatteryRecord>
}
