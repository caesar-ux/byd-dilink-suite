package com.byd.dilink.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.byd.dilink.core.data.entities.MaintenanceCategory
import com.byd.dilink.core.data.entities.ServiceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {

    // --- MaintenanceCategory ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: MaintenanceCategory): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<MaintenanceCategory>)

    @Update
    suspend fun updateCategory(category: MaintenanceCategory)

    @Delete
    suspend fun deleteCategory(category: MaintenanceCategory)

    @Query("SELECT * FROM maintenance_categories ORDER BY sort_order ASC")
    fun getAllCategories(): Flow<List<MaintenanceCategory>>

    @Query("SELECT * FROM maintenance_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): MaintenanceCategory?

    @Query("SELECT COUNT(*) FROM maintenance_categories")
    suspend fun getCategoryCount(): Int

    // --- ServiceRecord ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceRecord(record: ServiceRecord): Long

    @Update
    suspend fun updateServiceRecord(record: ServiceRecord)

    @Delete
    suspend fun deleteServiceRecord(record: ServiceRecord)

    @Query("SELECT * FROM service_records WHERE category_id = :categoryId ORDER BY date_performed DESC")
    fun getServiceRecordsByCategory(categoryId: Long): Flow<List<ServiceRecord>>

    @Query("SELECT * FROM service_records WHERE category_id = :categoryId ORDER BY date_performed DESC LIMIT 1")
    suspend fun getLatestServiceForCategory(categoryId: Long): ServiceRecord?

    @Query("SELECT * FROM service_records ORDER BY date_performed DESC")
    fun getAllServiceRecords(): Flow<List<ServiceRecord>>

    @Query("DELETE FROM service_records WHERE id = :id")
    suspend fun deleteServiceRecordById(id: Long)

    suspend fun insertDefaultCategories() {
        val defaults = listOf(
            MaintenanceCategory(name = "Engine Oil", iconName = "oil_barrel", intervalKm = 7500, intervalMonths = 6, sortOrder = 1),
            MaintenanceCategory(name = "Oil Filter", iconName = "filter_alt", intervalKm = 7500, intervalMonths = 6, sortOrder = 2),
            MaintenanceCategory(name = "Air Filter", iconName = "air", intervalKm = 15000, intervalMonths = 12, sortOrder = 3),
            MaintenanceCategory(name = "Cabin Air Filter", iconName = "air", intervalKm = 15000, intervalMonths = 12, sortOrder = 4),
            MaintenanceCategory(name = "Brake Fluid", iconName = "water_drop", intervalKm = 40000, intervalMonths = 24, sortOrder = 5),
            MaintenanceCategory(name = "Brake Pads (Front)", iconName = "disc_full", intervalKm = 30000, intervalMonths = null, sortOrder = 6),
            MaintenanceCategory(name = "Brake Pads (Rear)", iconName = "disc_full", intervalKm = 50000, intervalMonths = null, sortOrder = 7),
            MaintenanceCategory(name = "Coolant", iconName = "thermostat", intervalKm = 40000, intervalMonths = 24, sortOrder = 8),
            MaintenanceCategory(name = "Transmission Fluid", iconName = "settings", intervalKm = 60000, intervalMonths = 48, sortOrder = 9),
            MaintenanceCategory(name = "Spark Plugs", iconName = "electric_bolt", intervalKm = 30000, intervalMonths = 24, sortOrder = 10),
            MaintenanceCategory(name = "Tire Rotation", iconName = "tire_repair", intervalKm = 10000, intervalMonths = 6, sortOrder = 11),
            MaintenanceCategory(name = "Battery (12V)", iconName = "battery_full", intervalKm = null, intervalMonths = 36, sortOrder = 12),
            MaintenanceCategory(name = "HV Battery Check", iconName = "battery_charging_full", intervalKm = null, intervalMonths = 12, sortOrder = 13),
            MaintenanceCategory(name = "Wiper Blades", iconName = "water", intervalKm = null, intervalMonths = 12, sortOrder = 14),
            MaintenanceCategory(name = "Wheel Alignment", iconName = "straighten", intervalKm = 20000, intervalMonths = 12, sortOrder = 15)
        )
        insertCategories(defaults)
    }
}
