package com.byd.dilink.extras.fuelcost.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byd.dilink.extras.core.data.entities.ChargeRecord
import com.byd.dilink.extras.core.data.entities.FuelRecord
import com.byd.dilink.extras.core.data.entities.OdometerEntry
import com.byd.dilink.extras.core.data.preferences.ExtrasPreferences
import com.byd.dilink.extras.core.data.repository.FuelCostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val costPerKmFuel: Double = 0.0,
    val costPerKmElectric: Double = 0.0,
    val monthlySavings: Double = 0.0,
    val evPercentage: Double = 0.0,
    val currentOdometer: Int = 0,
    val currency: String = "IQD",
    val recentFuelRecords: List<FuelRecord> = emptyList(),
    val recentChargeRecords: List<ChargeRecord> = emptyList(),
    val batteryCapacityKwh: Double = 8.3,
    val defaultFuelPrice: Double = 750.0,
    val defaultElectricityPrice: Double = 100.0,
    val benchmarkConsumption: Double = 7.0
)

data class StatisticsUiState(
    val monthlyBreakdowns: List<FuelCostRepository.MonthlyBreakdown> = emptyList(),
    val lifetimeTotals: FuelCostRepository.LifetimeTotals? = null,
    val allFuelRecords: List<FuelRecord> = emptyList(),
    val allChargeRecords: List<ChargeRecord> = emptyList()
)

@HiltViewModel
class FuelCostViewModel @Inject constructor(
    private val repository: FuelCostRepository,
    private val preferences: ExtrasPreferences
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardUiState())
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    private val _statisticsState = MutableStateFlow(StatisticsUiState())
    val statisticsState: StateFlow<StatisticsUiState> = _statisticsState.asStateFlow()

    init {
        // Observe preferences
        viewModelScope.launch {
            combine(
                preferences.currency,
                preferences.batteryCapacityKwh,
                preferences.defaultFuelPrice,
                preferences.defaultElectricityPrice,
                preferences.benchmarkConsumption
            ) { currency, battery, fuelPrice, elecPrice, benchmark ->
                _dashboardState.value = _dashboardState.value.copy(
                    currency = currency,
                    batteryCapacityKwh = battery,
                    defaultFuelPrice = fuelPrice,
                    defaultElectricityPrice = elecPrice,
                    benchmarkConsumption = benchmark
                )
            }.collect { }
        }

        // Observe odometer
        viewModelScope.launch {
            repository.getLatestOdometer().collect { entry ->
                _dashboardState.value = _dashboardState.value.copy(
                    currentOdometer = entry?.odometerKm ?: 0
                )
            }
        }

        // Observe fuel records
        viewModelScope.launch {
            repository.getAllFuelRecords().collect { records ->
                _dashboardState.value = _dashboardState.value.copy(
                    recentFuelRecords = records.take(3)
                )
                _statisticsState.value = _statisticsState.value.copy(
                    allFuelRecords = records
                )
            }
        }

        // Observe charge records
        viewModelScope.launch {
            repository.getAllChargeRecords().collect { records ->
                _dashboardState.value = _dashboardState.value.copy(
                    recentChargeRecords = records.take(3)
                )
                _statisticsState.value = _statisticsState.value.copy(
                    allChargeRecords = records
                )
            }
        }

        // Calculate metrics
        refreshMetrics()
    }

    fun refreshMetrics() {
        viewModelScope.launch {
            val fuelCostPerKm = repository.calculateFuelCostPerKm()
            val electricCostPerKm = repository.calculateElectricCostPerKm()
            val savings = repository.calculateMonthlySavings()
            val evPct = repository.calculateEvPercentage()

            _dashboardState.value = _dashboardState.value.copy(
                costPerKmFuel = fuelCostPerKm,
                costPerKmElectric = electricCostPerKm,
                monthlySavings = savings,
                evPercentage = evPct
            )
        }
    }

    fun loadStatistics() {
        viewModelScope.launch {
            val breakdowns = repository.getMonthlyBreakdowns()
            val totals = repository.getLifetimeTotals()

            _statisticsState.value = _statisticsState.value.copy(
                monthlyBreakdowns = breakdowns,
                lifetimeTotals = totals
            )
        }
    }

    fun insertFuelRecord(record: FuelRecord) {
        viewModelScope.launch {
            repository.insertFuel(record)
            // Also insert odometer entry
            repository.insertOdometer(
                OdometerEntry(date = record.date, odometerKm = record.odometerKm)
            )
            refreshMetrics()
        }
    }

    fun insertChargeRecord(record: ChargeRecord) {
        viewModelScope.launch {
            repository.insertCharge(record)
            repository.insertOdometer(
                OdometerEntry(date = record.date, odometerKm = record.odometerKm)
            )
            refreshMetrics()
        }
    }

    fun updateOdometer(km: Int) {
        viewModelScope.launch {
            repository.insertOdometer(
                OdometerEntry(date = System.currentTimeMillis(), odometerKm = km)
            )
            refreshMetrics()
        }
    }

    // Settings
    suspend fun updateBatteryCapacity(kwh: Double) {
        preferences.set(ExtrasPreferences.FuelKeys.BATTERY_CAPACITY_KWH, kwh)
    }

    suspend fun updateDefaultFuelPrice(price: Double) {
        preferences.set(ExtrasPreferences.FuelKeys.DEFAULT_FUEL_PRICE, price)
    }

    suspend fun updateDefaultElectricityPrice(price: Double) {
        preferences.set(ExtrasPreferences.FuelKeys.DEFAULT_ELECTRICITY_PRICE, price)
    }

    suspend fun updateBenchmarkConsumption(lPer100: Double) {
        preferences.set(ExtrasPreferences.FuelKeys.BENCHMARK_CONSUMPTION, lPer100)
    }

    suspend fun updateCurrency(currency: String) {
        preferences.set(ExtrasPreferences.FuelKeys.CURRENCY, currency)
    }
}
