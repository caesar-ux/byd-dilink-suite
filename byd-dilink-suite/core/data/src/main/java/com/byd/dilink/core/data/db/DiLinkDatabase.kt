package com.byd.dilink.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.byd.dilink.core.data.dao.MaintenanceCategoryDao
import com.byd.dilink.core.data.dao.PlaylistEntryDao
import com.byd.dilink.core.data.dao.ServiceRecordDao
import com.byd.dilink.core.data.dao.VehicleProfileDao

@Database(
    entities = [
        MaintenanceCategory::class,
        ServiceRecord::class,
        VehicleProfile::class,
        PlaylistEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DiLinkDatabase : RoomDatabase() {
    abstract fun maintenanceCategoryDao(): MaintenanceCategoryDao
    abstract fun serviceRecordDao(): ServiceRecordDao
    abstract fun vehicleProfileDao(): VehicleProfileDao
    abstract fun playlistEntryDao(): PlaylistEntryDao
}
