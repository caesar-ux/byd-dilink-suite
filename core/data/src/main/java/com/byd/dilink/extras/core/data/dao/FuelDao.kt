package com.byd.dilink.extras.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.byd.dilink.extras.core.data.entities.FuelRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FuelRecord): Long

    @Update
    suspend fun update(record: FuelRecord)

    @Delete
    suspend fun delete(record: FuelRecord)

    @Query("SELECT * FROM fuel_records ORDER BY date DESC")
    fun getAllByDate(): Flow<List<FuelRecord>>

    @Query("SELECT * FROM fuel_records ORDER BY date DESC")
    suspend fun getAllByDateOnce(): List<FuelRecord>

    @Query("SELECT * FROM fuel_records ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): FuelRecord?

    @Query("SELECT COALESCE(SUM(liters), 0.0) FROM fuel_records")
    fun getTotalLiters(): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalCostIqd), 0.0) FROM fuel_records")
    fun getTotalCost(): Flow<Double>
}
