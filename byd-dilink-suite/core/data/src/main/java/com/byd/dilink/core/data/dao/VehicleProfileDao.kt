package com.byd.dilink.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.byd.dilink.core.data.entities.VehicleProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleProfileDao {

    @Query("SELECT * FROM vehicle_profile WHERE id = 1")
    fun getProfile(): Flow<VehicleProfile?>

    @Query("SELECT * FROM vehicle_profile WHERE id = 1")
    suspend fun getProfileOnce(): VehicleProfile?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(profile: VehicleProfile)

    @Update
    suspend fun update(profile: VehicleProfile)

    @Query("UPDATE vehicle_profile SET current_odometer_km = :odometer, last_odometer_update = :timestamp WHERE id = 1")
    suspend fun updateOdometerWithTimestamp(odometer: Int, timestamp: Long)

    suspend fun updateOdometer(km: Int) {
        updateOdometerWithTimestamp(km, System.currentTimeMillis())
    }

    suspend fun ensureProfileExists() {
        val existing = getProfileOnce()
        if (existing == null) {
            insert(VehicleProfile())
        }
    }
}
