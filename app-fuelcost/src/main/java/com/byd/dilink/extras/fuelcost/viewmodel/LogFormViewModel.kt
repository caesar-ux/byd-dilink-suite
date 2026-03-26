package com.byd.dilink.extras.fuelcost.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class FuelFormState(
    val date: Long = System.currentTimeMillis(),
    val odometerKm: String = "",
    val liters: String = "",
    val totalCost: String = "",
    val pricePerLiter: String = "",
    val fuelType: String = "Regular",
    val isFullTank: Boolean = false,
    val stationName: String = "",
    val notes: String = "",
    val isAutoCalcCost: Boolean = false,
    val isAutoCalcPrice: Boolean = false
)

data class ChargeFormState(
    val date: Long = System.currentTimeMillis(),
    val odometerKm: String = "",
    val kwhCharged: String = "",
    val startSocPercent: String = "",
    val endSocPercent: String = "",
    val totalCost: String = "",
    val costPerKwh: String = "",
    val source: String = "Home",
    val durationMin: String = "",
    val notes: String = "",
    val isAutoCalcKwh: Boolean = false
)

@HiltViewModel
class LogFormViewModel @Inject constructor() : ViewModel() {

    private val _fuelForm = MutableStateFlow(FuelFormState())
    val fuelForm: StateFlow<FuelFormState> = _fuelForm.asStateFlow()

    private val _chargeForm = MutableStateFlow(ChargeFormState())
    val chargeForm: StateFlow<ChargeFormState> = _chargeForm.asStateFlow()

    // Fuel form updates
    fun updateFuelDate(date: Long) { _fuelForm.value = _fuelForm.value.copy(date = date) }
    fun updateFuelOdometer(value: String) { _fuelForm.value = _fuelForm.value.copy(odometerKm = value) }
    fun updateFuelType(type: String) { _fuelForm.value = _fuelForm.value.copy(fuelType = type) }
    fun updateFullTank(value: Boolean) { _fuelForm.value = _fuelForm.value.copy(isFullTank = value) }
    fun updateFuelStation(value: String) { _fuelForm.value = _fuelForm.value.copy(stationName = value) }
    fun updateFuelNotes(value: String) { _fuelForm.value = _fuelForm.value.copy(notes = value) }

    fun updateFuelLiters(value: String) {
        _fuelForm.value = _fuelForm.value.copy(liters = value)
        autoCalculateFuelCost()
    }

    fun updateFuelTotalCost(value: String) {
        _fuelForm.value = _fuelForm.value.copy(totalCost = value, isAutoCalcCost = false)
        autoCalculateFuelPrice()
    }

    fun updateFuelPricePerLiter(value: String) {
        _fuelForm.value = _fuelForm.value.copy(pricePerLiter = value, isAutoCalcPrice = false)
        autoCalculateFuelCost()
    }

    private fun autoCalculateFuelCost() {
        val state = _fuelForm.value
        val liters = state.liters.toDoubleOrNull() ?: return
        val price = state.pricePerLiter.toDoubleOrNull() ?: return
        if (liters > 0 && price > 0) {
            _fuelForm.value = state.copy(
                totalCost = String.format("%.0f", liters * price),
                isAutoCalcCost = true
            )
        }
    }

    private fun autoCalculateFuelPrice() {
        val state = _fuelForm.value
        val liters = state.liters.toDoubleOrNull() ?: return
        val cost = state.totalCost.toDoubleOrNull() ?: return
        if (liters > 0 && cost > 0) {
            _fuelForm.value = state.copy(
                pricePerLiter = String.format("%.1f", cost / liters),
                isAutoCalcPrice = true
            )
        }
    }

    fun resetFuelForm() {
        _fuelForm.value = FuelFormState()
    }

    // Charge form updates
    fun updateChargeDate(date: Long) { _chargeForm.value = _chargeForm.value.copy(date = date) }
    fun updateChargeOdometer(value: String) { _chargeForm.value = _chargeForm.value.copy(odometerKm = value) }
    fun updateChargeSource(source: String) { _chargeForm.value = _chargeForm.value.copy(source = source) }
    fun updateChargeDuration(value: String) { _chargeForm.value = _chargeForm.value.copy(durationMin = value) }
    fun updateChargeNotes(value: String) { _chargeForm.value = _chargeForm.value.copy(notes = value) }

    fun updateChargeKwh(value: String) {
        _chargeForm.value = _chargeForm.value.copy(kwhCharged = value, isAutoCalcKwh = false)
        autoCalculateChargeCostPerKwh()
    }

    fun updateChargeStartSoc(value: String) {
        _chargeForm.value = _chargeForm.value.copy(startSocPercent = value)
    }

    fun updateChargeEndSoc(value: String) {
        _chargeForm.value = _chargeForm.value.copy(endSocPercent = value)
    }

    fun autoCalculateKwhFromSoc(batteryCapacityKwh: Double) {
        val state = _chargeForm.value
        val startSoc = state.startSocPercent.toIntOrNull() ?: return
        val endSoc = state.endSocPercent.toIntOrNull() ?: return
        if (endSoc > startSoc) {
            val kwh = (endSoc - startSoc) / 100.0 * batteryCapacityKwh
            _chargeForm.value = state.copy(
                kwhCharged = String.format("%.2f", kwh),
                isAutoCalcKwh = true
            )
        }
    }

    fun updateChargeTotalCost(value: String) {
        _chargeForm.value = _chargeForm.value.copy(totalCost = value)
        autoCalculateChargeCostPerKwh()
    }

    fun updateChargeCostPerKwh(value: String) {
        _chargeForm.value = _chargeForm.value.copy(costPerKwh = value)
        autoCalculateChargeTotalCost()
    }

    private fun autoCalculateChargeCostPerKwh() {
        val state = _chargeForm.value
        val kwh = state.kwhCharged.toDoubleOrNull() ?: return
        val cost = state.totalCost.toDoubleOrNull() ?: return
        if (kwh > 0 && cost > 0) {
            _chargeForm.value = state.copy(
                costPerKwh = String.format("%.1f", cost / kwh)
            )
        }
    }

    private fun autoCalculateChargeTotalCost() {
        val state = _chargeForm.value
        val kwh = state.kwhCharged.toDoubleOrNull() ?: return
        val costPerKwh = state.costPerKwh.toDoubleOrNull() ?: return
        if (kwh > 0 && costPerKwh > 0) {
            _chargeForm.value = state.copy(
                totalCost = String.format("%.0f", kwh * costPerKwh)
            )
        }
    }

    fun resetChargeForm() {
        _chargeForm.value = ChargeFormState()
    }
}
