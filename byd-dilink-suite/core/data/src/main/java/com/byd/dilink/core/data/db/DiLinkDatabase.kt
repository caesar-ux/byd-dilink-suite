package com.byd.dilink.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.byd.dilink.core.data.dao.FavoriteLocationDao
import com.byd.dilink.core.data.dao.MaintenanceDao
import com.byd.dilink.core.data.dao.ParkingDao
import com.byd.dilink.core.data.dao.PlaylistDao
import com.byd.dilink.core.data.dao.VehicleProfileDao
import com.byd.dilink.core.data.entities.FavoriteLocation
import com.byd.dilink.core.data.entities.MaintenanceCategory
import com.byd.dilink.core.data.entities.ParkingRecord
import com.byd.dilink.core.data.entities.PlaylistEntry
import com.byd.dilink.core.data.entities.ServiceRecord
import com.byd.dilink.core.data.entities.VehicleProfile

@Database(
    entities = [
        ParkingRecord::class,
        FavoriteLocation::class,
        MaintenanceCategory::class,
        ServiceRecord::class,
        VehicleProfile::class,
        PlaylistEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DiLinkDatabase : RoomDatabase() {
    abstract fun parkingDao(): ParkingDao
    abstract fun favoriteLocationDao(): FavoriteLocationDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun vehicleProfileDao(): VehicleProfileDao
    abstract fun playlistDao(): PlaylistDao
}
