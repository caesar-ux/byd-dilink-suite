package com.byd.dilink.core.data.repository

import com.byd.dilink.core.data.dao.MaintenanceDao
import com.byd.dilink.core.data.dao.VehicleProfileDao
import com.byd.dilink.core.data.entities.MaintenanceCategory
import com.byd.dilink.core.data.entities.ServiceRecord
import com.byd.dilink.core.data.entities.VehicleProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

enum class MaintenanceStatus {
    OK, SOON, OVERDUE
}

data class MaintenanceStatusInfo(
    val category: MaintenanceCategory,
    val lastService: ServiceRecord?,
    val nextDueKm: Int?,
    val nextDueDate: Long?,
    val status: MaintenanceStatus
)

@Singleton
class MaintenanceRepository @Inject constructor(
    private val maintenanceDao: MaintenanceDao,
    private val vehicleProfileDao: VehicleProfileDao
) {
    val allCategories: Flow<List<MaintenanceCategory>> = maintenanceDao.getAllCategories()
    val allServiceRecords: Flow<List<ServiceRecord>> = maintenanceDao.getAllServiceRecords()

    fun getServiceRecordsByCategory(categoryId: Long): Flow<List<ServiceRecord>> {
        return maintenanceDao.getServiceRecordsByCategory(categoryId)
    }

    suspend fun ensureDefaultCategories() {
        val count = maintenanceDao.getCategoryCount()
        if (count == 0) {
            maintenanceDao.insertDefaultCategories()
        }
    }

    suspend fun ensureVehicleProfile() {
        vehicleProfileDao.ensureProfileExists()
    }

    suspend fun addServiceRecord(record: ServiceRecord): Long {
        return maintenanceDao.insertServiceRecord(record)
    }

    suspend fun updateServiceRecord(record: ServiceRecord) {
        maintenanceDao.updateServiceRecord(record)
    }

    suspend fun deleteServiceRecord(record: ServiceRecord) {
        maintenanceDao.deleteServiceRecord(record)
    }

    suspend fun addCategory(category: MaintenanceCategory): Long {
        return maintenanceDao.insertCategory(category)
    }

    suspend fun updateCategory(category: MaintenanceCategory) {
        maintenanceDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: MaintenanceCategory) {
        maintenanceDao.deleteCategory(category)
    }

    fun getVehicleProfile(): Flow<VehicleProfile?> {
        return vehicleProfileDao.getProfile()
    }

    suspend fun updateVehicleProfile(profile: VehicleProfile) {
        vehicleProfileDao.update(profile)
    }

    suspend fun updateOdometer(km: Int) {
        vehicleProfileDao.updateOdometer(km)
    }

    suspend fun calculateStatus(category: MaintenanceCategory): MaintenanceStatusInfo {
        val lastService = maintenanceDao.getLatestServiceForCategory(category.id)
        val profile = vehicleProfileDao.getProfileOnce()
        val currentKm = profile?.currentOdometerKm ?: 0
        val now = System.currentTimeMillis()

        var nextDueKm: Int? = null
        var nextDueDate: Long? = null
        var status = MaintenanceStatus.OK

        if (lastService != null) {
            // Calculate next due km
            if (category.intervalKm != null) {
                nextDueKm = lastService.odometerKm + category.intervalKm
            }
            // Calculate next due date
            if (category.intervalMonths != null) {
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = lastService.datePerformed
                cal.add(java.util.Calendar.MONTH, category.intervalMonths)
                nextDueDate = cal.timeInMillis
            }

            // Determine status
            val kmRemaining = if (nextDueKm != null) nextDueKm - currentKm else Int.MAX_VALUE
            val daysRemaining = if (nextDueDate != null) {
                ((nextDueDate - now) / (1000L * 60 * 60 * 24)).toInt()
            } else {
                Int.MAX_VALUE
            }

            status = when {
                kmRemaining < 0 || daysRemaining < 0 -> MaintenanceStatus.OVERDUE
                kmRemaining < 500 || daysRemaining < 30 -> MaintenanceStatus.SOON
                else -> MaintenanceStatus.OK
            }
        } else {
            // No service recorded — status is overdue if we have any odometer reading
            status = if (currentKm > 0) MaintenanceStatus.SOON else MaintenanceStatus.OK
        }

        return MaintenanceStatusInfo(
            category = category,
            lastService = lastService,
            nextDueKm = nextDueKm,
            nextDueDate = nextDueDate,
            status = status
        )
    }
}
