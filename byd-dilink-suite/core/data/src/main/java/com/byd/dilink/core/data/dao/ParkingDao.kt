package com.byd.dilink.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.byd.dilink.core.data.entities.ParkingRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ParkingRecord): Long

    @Update
    suspend fun update(record: ParkingRecord)

    @Delete
    suspend fun delete(record: ParkingRecord)

    @Query("SELECT * FROM parking_records WHERE cleared_at IS NULL ORDER BY parked_at DESC LIMIT 1")
    fun getActive(): Flow<ParkingRecord?>

    @Query("SELECT * FROM parking_records WHERE cleared_at IS NULL ORDER BY parked_at DESC LIMIT 1")
    suspend fun getActiveOnce(): ParkingRecord?

    @Query("SELECT * FROM parking_records WHERE cleared_at IS NOT NULL ORDER BY parked_at DESC LIMIT 100")
    fun getHistory(): Flow<List<ParkingRecord>>

    @Query("UPDATE parking_records SET cleared_at = :clearedAt WHERE id = :id")
    suspend fun clearParking(id: Long, clearedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM parking_records WHERE cleared_at IS NOT NULL")
    suspend fun clearAllHistory()

    @Query("DELETE FROM parking_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
