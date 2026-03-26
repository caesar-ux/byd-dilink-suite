package com.byd.dilink.extras.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.byd.dilink.extras.core.data.entities.ChargeRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ChargeRecord): Long

    @Update
    suspend fun update(record: ChargeRecord)

    @Delete
    suspend fun delete(record: ChargeRecord)

    @Query("SELECT * FROM charge_records ORDER BY date DESC")
    fun getAllByDate(): Flow<List<ChargeRecord>>

    @Query("SELECT * FROM charge_records ORDER BY date DESC")
    suspend fun getAllByDateOnce(): List<ChargeRecord>

    @Query("SELECT * FROM charge_records ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): ChargeRecord?

    @Query("SELECT COALESCE(SUM(kwhCharged), 0.0) FROM charge_records")
    fun getTotalKwh(): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalCostIqd), 0.0) FROM charge_records")
    fun getTotalCost(): Flow<Double>
}
