package com.byd.dilink.extras.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.byd.dilink.extras.core.data.entities.HazardRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HazardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HazardRecord): Long

    @Delete
    suspend fun delete(record: HazardRecord)

    @Query("DELETE FROM hazards WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM hazards ORDER BY timestamp DESC")
    fun getAll(): Flow<List<HazardRecord>>

    @Query("SELECT * FROM hazards")
    suspend fun getAllOnce(): List<HazardRecord>

    @Query("SELECT * FROM hazards WHERE type = :type ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<HazardRecord>>

    @Query("DELETE FROM hazards WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)

    @Query("SELECT COUNT(*) FROM hazards")
    fun getCount(): Flow<Int>

    @Query("SELECT * FROM hazards")
    suspend fun getNearby(): List<HazardRecord>

    @Query("DELETE FROM hazards")
    suspend fun deleteAll()
}
