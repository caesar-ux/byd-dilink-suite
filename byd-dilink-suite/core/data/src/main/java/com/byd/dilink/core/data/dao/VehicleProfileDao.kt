package com.byd.dilink.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.byd.dilink.core.data.db.VehicleProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleProfileDao {

    @Query("SELECT * FROM vehicle_profile WHERE id = 1")
    fun getVehicleProfile(): Flow<VehicleProfile?>

    @Query("SELECT * FROM vehicle_profile WHERE id = 1")
    suspend fun getVehicleProfileSync(): VehicleProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: VehicleProfile)

    @Update
    suspend fun update(profile: VehicleProfile)

    @Query("UPDATE vehicle_profile SET currentOdometerKm = :odometer, lastOdometerUpdate = :timestamp WHERE id = 1")
    suspend fun updateOdometer(odometer: Int, timestamp: Long)
}
