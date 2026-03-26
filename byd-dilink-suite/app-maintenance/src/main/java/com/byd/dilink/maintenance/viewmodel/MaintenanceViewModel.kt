package com.byd.dilink.maintenance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byd.dilink.core.data.dao.MaintenanceDao
import com.byd.dilink.core.data.dao.VehicleProfileDao
import com.byd.dilink.core.data.entities.MaintenanceCategory
import com.byd.dilink.core.data.entities.ServiceRecord
import com.byd.dilink.core.data.entities.VehicleProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class MaintenanceStatus {
    OK, SOON, OVERDUE
}

data class CategoryWithStatus(
    val category: MaintenanceCategory,
    val status: MaintenanceStatus,
    val lastServiceDate: Long? = null,
    val lastServiceOdometer: Int = 0,
    val nextDueKm: Int? = null,
    val nextDueDate: Long? = null
)

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val maintenanceDao: MaintenanceDao,
    private val vehicleProfileDao: VehicleProfileDao
) : ViewModel() {

    private val _vehicleProfile = MutableStateFlow<VehicleProfile?>(null)
    val vehicleProfile: StateFlow<VehicleProfile?> = _vehicleProfile.asStateFlow()

    private val _categoriesWithStatus = MutableStateFlow<List<CategoryWithStatus>>(emptyList())
    val categoriesWithStatus: StateFlow<List<CategoryWithStatus>> = _categoriesWithStatus.asStateFlow()

    private val _totalCost = MutableStateFlow(0.0)
    val totalCost: StateFlow<Double> = _totalCost.asStateFlow()

    private val _totalServices = MutableStateFlow(0)
    val totalServices: StateFlow<Int> = _totalServices.asStateFlow()

    init {
        viewModelScope.launch {
            ensureDefaultsExist()
        }
        viewModelScope.launch {
            vehicleProfileDao.getProfile().collect { profile ->
                val p = profile ?: VehicleProfile().also {
                    vehicleProfileDao.insert(it)
                }
                _vehicleProfile.value = p
            }
        }
        viewModelScope.launch {
            maintenanceDao.getAllCategories().collect { categories ->
                val profile = _vehicleProfile.value ?: VehicleProfile()
                val items = categories.map { cat ->
                    val latestService = maintenanceDao.getLatestServiceForCategory(cat.id)
                    calculateCategoryStatus(cat, latestService, profile)
                }
                _categoriesWithStatus.value = items
            }
        }
        // Recalculate when vehicle profile changes
        viewModelScope.launch {
            _vehicleProfile.collect { profile ->
                if (profile != null) {
                    recalculateStatuses()
                }
            }
        }
        // Track service stats
        viewModelScope.launch {
            maintenanceDao.getAllServiceRecords().collect { records ->
                _totalServices.value = records.size
                _totalCost.value = records.mapNotNull { it.cost }.sum()
            }
        }
    }

    private suspend fun recalculateStatuses() {
        val profile = _vehicleProfile.value ?: return
        val currentCategories = _categoriesWithStatus.value
        if (currentCategories.isEmpty()) return
        val updated = currentCategories.map { item ->
            val latestService = maintenanceDao.getLatestServiceForCategory(item.category.id)
            calculateCategoryStatus(item.category, latestService, profile)
        }
        _categoriesWithStatus.value = updated
    }

    private fun calculateCategoryStatus(
        category: MaintenanceCategory,
        latestService: ServiceRecord?,
        vehicleProfile: VehicleProfile
    ): CategoryWithStatus {
        if (latestService == null) {
            return CategoryWithStatus(
                category = category,
                status = MaintenanceStatus.OVERDUE,
                lastServiceDate = null,
                lastServiceOdometer = 0,
                nextDueKm = category.intervalKm,
                nextDueDate = null
            )
        }

        val currentOdometer = vehicleProfile.currentOdometerKm
        val now = System.currentTimeMillis()

        val kmSinceService = currentOdometer - latestService.odometerKm
        val daysSinceService = TimeUnit.MILLISECONDS.toDays(now - latestService.datePerformed)

        var isOverdue = false
        var isSoon = false

        val nextDueKm = category.intervalKm?.let { latestService.odometerKm + it }
        val nextDueDate = category.intervalMonths?.let {
            addMonths(latestService.datePerformed, it.toLong())
        }

        // Check km-based
        category.intervalKm?.let { intervalKm ->
            if (kmSinceService >= intervalKm) {
                isOverdue = true
            } else if (kmSinceService >= intervalKm - 500) {
                isSoon = true
            }
        }

        // Check date-based
        category.intervalMonths?.let { intervalMonths ->
            val intervalDays = intervalMonths * 30L
            if (daysSinceService >= intervalDays) {
                isOverdue = true
            } else if (daysSinceService >= intervalDays - 30) {
                isSoon = true
            }
        }

        val status = when {
            isOverdue -> MaintenanceStatus.OVERDUE
            isSoon -> MaintenanceStatus.SOON
            else -> MaintenanceStatus.OK
        }

        return CategoryWithStatus(
            category = category,
            status = status,
            lastServiceDate = latestService.datePerformed,
            lastServiceOdometer = latestService.odometerKm,
            nextDueKm = nextDueKm,
            nextDueDate = nextDueDate
        )
    }

    private fun addMonths(timestamp: Long, months: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            add(Calendar.MONTH, months.toInt())
        }
        return calendar.timeInMillis
    }

    fun getServicesForCategory(categoryId: Long): Flow<List<ServiceRecord>> {
        return maintenanceDao.getServiceRecordsByCategory(categoryId)
    }

    fun addService(
        categoryId: Long,
        datePerformed: Long,
        odometerKm: Int,
        cost: Double?,
        shopName: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            maintenanceDao.insertServiceRecord(
                ServiceRecord(
                    categoryId = categoryId,
                    datePerformed = datePerformed,
                    odometerKm = odometerKm,
                    cost = cost,
                    shopName = shopName,
                    notes = notes
                )
            )
            // Update odometer if this reading is higher
            val currentProfile = _vehicleProfile.value
            if (currentProfile != null && odometerKm > currentProfile.currentOdometerKm) {
                vehicleProfileDao.updateOdometer(odometerKm)
            }
            recalculateStatuses()
        }
    }

    fun deleteService(record: ServiceRecord) {
        viewModelScope.launch {
            maintenanceDao.deleteServiceRecord(record)
            recalculateStatuses()
        }
    }

    fun updateOdometer(newOdometer: Int) {
        viewModelScope.launch {
            vehicleProfileDao.updateOdometer(newOdometer)
        }
    }

    fun updateVehicleProfile(profile: VehicleProfile) {
        viewModelScope.launch {
            vehicleProfileDao.update(profile)
        }
    }

    fun addCategory(
        name: String,
        iconName: String,
        intervalKm: Int?,
        intervalMonths: Int?
    ) {
        viewModelScope.launch {
            maintenanceDao.insertCategory(
                MaintenanceCategory(
                    name = name,
                    iconName = iconName,
                    intervalKm = intervalKm,
                    intervalMonths = intervalMonths,
                    isCustom = true,
                    sortOrder = 100
                )
            )
        }
    }

    fun deleteCategory(category: MaintenanceCategory) {
        viewModelScope.launch {
            maintenanceDao.deleteCategory(category)
        }
    }

    private suspend fun ensureDefaultsExist() {
        val count = maintenanceDao.getCategoryCount()
        if (count == 0) {
            maintenanceDao.insertDefaultCategories()
        }
        vehicleProfileDao.ensureProfileExists()
    }
}
