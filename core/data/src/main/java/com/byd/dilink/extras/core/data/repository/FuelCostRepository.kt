package com.byd.dilink.extras.core.data.repository

import com.byd.dilink.extras.core.data.dao.ChargeDao
import com.byd.dilink.extras.core.data.dao.FuelDao
import com.byd.dilink.extras.core.data.dao.OdometerDao
import com.byd.dilink.extras.core.data.entities.ChargeRecord
import com.byd.dilink.extras.core.data.entities.FuelRecord
import com.byd.dilink.extras.core.data.entities.OdometerEntry
import com.byd.dilink.extras.core.data.preferences.ExtrasPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.Calendar

class FuelCostRepository(
    private val fuelDao: FuelDao,
    private val chargeDao: ChargeDao,
    private val odometerDao: OdometerDao,
    private val preferences: ExtrasPreferences
) {

    // Fuel records
    fun getAllFuelRecords(): Flow<List<FuelRecord>> = fuelDao.getAllByDate()
    suspend fun getAllFuelRecordsOnce(): List<FuelRecord> = fuelDao.getAllByDateOnce()
    suspend fun insertFuel(record: FuelRecord): Long = fuelDao.insert(record)
    suspend fun updateFuel(record: FuelRecord) = fuelDao.update(record)
    suspend fun deleteFuel(record: FuelRecord) = fuelDao.delete(record)
    suspend fun getLatestFuel(): FuelRecord? = fuelDao.getLatest()
    fun getTotalFuelLiters(): Flow<Double> = fuelDao.getTotalLiters()
    fun getTotalFuelCost(): Flow<Double> = fuelDao.getTotalCost()

    // Charge records
    fun getAllChargeRecords(): Flow<List<ChargeRecord>> = chargeDao.getAllByDate()
    suspend fun getAllChargeRecordsOnce(): List<ChargeRecord> = chargeDao.getAllByDateOnce()
    suspend fun insertCharge(record: ChargeRecord): Long = chargeDao.insert(record)
    suspend fun updateCharge(record: ChargeRecord) = chargeDao.update(record)
    suspend fun deleteCharge(record: ChargeRecord) = chargeDao.delete(record)
    suspend fun getLatestCharge(): ChargeRecord? = chargeDao.getLatest()
    fun getTotalChargeKwh(): Flow<Double> = chargeDao.getTotalKwh()
    fun getTotalChargeCost(): Flow<Double> = chargeDao.getTotalCost()

    // Odometer
    fun getLatestOdometer(): Flow<OdometerEntry?> = odometerDao.getLatest()
    suspend fun getLatestOdometerOnce(): OdometerEntry? = odometerDao.getLatestOnce()
    suspend fun insertOdometer(entry: OdometerEntry): Long = odometerDao.insert(entry)
    fun getAllOdometer(): Flow<List<OdometerEntry>> = odometerDao.getAll()
    suspend fun getAllOdometerOnce(): List<OdometerEntry> = odometerDao.getAllOnce()

    /**
     * Calculate cost per km for fuel driving.
     * Uses all fuel records to compute total fuel cost / total fuel-attributed km.
     */
    suspend fun calculateFuelCostPerKm(): Double {
        val fuelRecords = fuelDao.getAllByDateOnce().sortedBy { it.odometerKm }
        if (fuelRecords.size < 2) return 0.0

        val totalCost = fuelRecords.sumOf { it.totalCostIqd }
        val kmDriven = fuelRecords.last().odometerKm - fuelRecords.first().odometerKm
        if (kmDriven <= 0) return 0.0
        return totalCost / kmDriven
    }

    /**
     * Calculate cost per km for electric driving.
     * Uses all charge records to compute total charge cost / total electric-attributed km.
     */
    suspend fun calculateElectricCostPerKm(): Double {
        val chargeRecords = chargeDao.getAllByDateOnce().sortedBy { it.odometerKm }
        if (chargeRecords.size < 2) return 0.0

        val totalCost = chargeRecords.sumOf { it.totalCostIqd }
        val kmDriven = chargeRecords.last().odometerKm - chargeRecords.first().odometerKm
        if (kmDriven <= 0) return 0.0
        return totalCost / kmDriven
    }

    /**
     * Calculate total savings vs a pure petrol car.
     * Savings = (totalKm * benchmarkL/100 * fuelPrice) - actualTotalCost
     */
    suspend fun calculateSavings(): Double {
        val benchmark = preferences.benchmarkConsumption.first()
        val fuelPrice = preferences.defaultFuelPrice.first()

        val fuelRecords = fuelDao.getAllByDateOnce()
        val chargeRecords = chargeDao.getAllByDateOnce()
        val odometerEntries = odometerDao.getAllOnce()

        val allOdometers = mutableListOf<Int>()
        fuelRecords.forEach { allOdometers.add(it.odometerKm) }
        chargeRecords.forEach { allOdometers.add(it.odometerKm) }
        odometerEntries.forEach { allOdometers.add(it.odometerKm) }

        if (allOdometers.size < 2) return 0.0

        val totalKm = allOdometers.max() - allOdometers.min()
        if (totalKm <= 0) return 0.0

        val hypotheticalPetrolCost = totalKm * (benchmark / 100.0) * fuelPrice
        val actualTotalCost = fuelRecords.sumOf { it.totalCostIqd } + chargeRecords.sumOf { it.totalCostIqd }

        return hypotheticalPetrolCost - actualTotalCost
    }

    /**
     * Calculate monthly savings based on current month records.
     */
    suspend fun calculateMonthlySavings(): Double {
        val benchmark = preferences.benchmarkConsumption.first()
        val fuelPrice = preferences.defaultFuelPrice.first()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val monthStart = calendar.timeInMillis

        val fuelRecords = fuelDao.getAllByDateOnce().filter { it.date >= monthStart }
        val chargeRecords = chargeDao.getAllByDateOnce().filter { it.date >= monthStart }

        val allOdometers = mutableListOf<Int>()
        fuelRecords.forEach { allOdometers.add(it.odometerKm) }
        chargeRecords.forEach { allOdometers.add(it.odometerKm) }

        if (allOdometers.size < 2) return 0.0

        val totalKm = allOdometers.max() - allOdometers.min()
        if (totalKm <= 0) return 0.0

        val hypotheticalPetrolCost = totalKm * (benchmark / 100.0) * fuelPrice
        val actualTotalCost = fuelRecords.sumOf { it.totalCostIqd } + chargeRecords.sumOf { it.totalCostIqd }

        return hypotheticalPetrolCost - actualTotalCost
    }

    /**
     * Calculate the EV driving percentage.
     * Estimated from kWh charged vs total energy consumed (fuel + electric).
     */
    suspend fun calculateEvPercentage(): Double {
        val chargeRecords = chargeDao.getAllByDateOnce()
        val fuelRecords = fuelDao.getAllByDateOnce()

        val totalChargeKwh = chargeRecords.sumOf { it.kwhCharged }
        // Approximate energy from fuel: 1 liter petrol ≈ 9.7 kWh
        val totalFuelKwh = fuelRecords.sumOf { it.liters } * 9.7

        val totalEnergy = totalChargeKwh + totalFuelKwh
        if (totalEnergy <= 0.0) return 0.0

        return (totalChargeKwh / totalEnergy) * 100.0
    }

    data class MonthlyBreakdown(
        val year: Int,
        val month: Int,
        val fuelCostIqd: Double,
        val electricCostIqd: Double,
        val totalKm: Int,
        val savings: Double
    )

    suspend fun getMonthlyBreakdowns(): List<MonthlyBreakdown> {
        val benchmark = preferences.benchmarkConsumption.first()
        val fuelPrice = preferences.defaultFuelPrice.first()
        val fuelRecords = fuelDao.getAllByDateOnce()
        val chargeRecords = chargeDao.getAllByDateOnce()

        val calendar = Calendar.getInstance()
        val monthlyData = mutableMapOf<String, MonthlyBreakdown>()

        for (record in fuelRecords) {
            calendar.timeInMillis = record.date
            val key = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
            val existing = monthlyData[key]
            if (existing != null) {
                monthlyData[key] = existing.copy(
                    fuelCostIqd = existing.fuelCostIqd + record.totalCostIqd
                )
            } else {
                monthlyData[key] = MonthlyBreakdown(
                    year = calendar.get(Calendar.YEAR),
                    month = calendar.get(Calendar.MONTH),
                    fuelCostIqd = record.totalCostIqd,
                    electricCostIqd = 0.0,
                    totalKm = 0,
                    savings = 0.0
                )
            }
        }

        for (record in chargeRecords) {
            calendar.timeInMillis = record.date
            val key = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
            val existing = monthlyData[key]
            if (existing != null) {
                monthlyData[key] = existing.copy(
                    electricCostIqd = existing.electricCostIqd + record.totalCostIqd
                )
            } else {
                monthlyData[key] = MonthlyBreakdown(
                    year = calendar.get(Calendar.YEAR),
                    month = calendar.get(Calendar.MONTH),
                    fuelCostIqd = 0.0,
                    electricCostIqd = record.totalCostIqd,
                    totalKm = 0,
                    savings = 0.0
                )
            }
        }

        // Compute per-month km and savings from all odometers within each month
        val allRecordsSorted = (fuelRecords.map { it.date to it.odometerKm } +
                chargeRecords.map { it.date to it.odometerKm })
            .sortedBy { it.first }

        for ((key, data) in monthlyData) {
            val monthRecords = allRecordsSorted.filter { (date, _) ->
                calendar.timeInMillis = date
                "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}" == key
            }
            val km = if (monthRecords.size >= 2) {
                monthRecords.maxOf { it.second } - monthRecords.minOf { it.second }
            } else {
                0
            }
            val hypothetical = km * (benchmark / 100.0) * fuelPrice
            val actualCost = data.fuelCostIqd + data.electricCostIqd
            monthlyData[key] = data.copy(
                totalKm = km,
                savings = hypothetical - actualCost
            )
        }

        return monthlyData.values.sortedByDescending { it.year * 100 + it.month }
    }

    data class LifetimeTotals(
        val totalFuelLiters: Double,
        val totalFuelCostIqd: Double,
        val totalChargeKwh: Double,
        val totalChargeCostIqd: Double,
        val totalKm: Int,
        val avgLPer100Km: Double,
        val avgKwhPer100Km: Double,
        val totalSavings: Double
    )

    suspend fun getLifetimeTotals(): LifetimeTotals {
        val fuelRecords = fuelDao.getAllByDateOnce().sortedBy { it.odometerKm }
        val chargeRecords = chargeDao.getAllByDateOnce().sortedBy { it.odometerKm }

        val totalFuelLiters = fuelRecords.sumOf { it.liters }
        val totalFuelCost = fuelRecords.sumOf { it.totalCostIqd }
        val totalChargeKwh = chargeRecords.sumOf { it.kwhCharged }
        val totalChargeCost = chargeRecords.sumOf { it.totalCostIqd }

        val allOdometers = mutableListOf<Int>()
        fuelRecords.forEach { allOdometers.add(it.odometerKm) }
        chargeRecords.forEach { allOdometers.add(it.odometerKm) }

        val totalKm = if (allOdometers.size >= 2) allOdometers.max() - allOdometers.min() else 0

        val fuelKm = if (fuelRecords.size >= 2) {
            fuelRecords.last().odometerKm - fuelRecords.first().odometerKm
        } else 0

        val chargeKm = if (chargeRecords.size >= 2) {
            chargeRecords.last().odometerKm - chargeRecords.first().odometerKm
        } else 0

        val avgLPer100 = if (fuelKm > 0) (totalFuelLiters / fuelKm) * 100 else 0.0
        val avgKwhPer100 = if (chargeKm > 0) (totalChargeKwh / chargeKm) * 100 else 0.0

        val savings = calculateSavings()

        return LifetimeTotals(
            totalFuelLiters = totalFuelLiters,
            totalFuelCostIqd = totalFuelCost,
            totalChargeKwh = totalChargeKwh,
            totalChargeCostIqd = totalChargeCost,
            totalKm = totalKm,
            avgLPer100Km = avgLPer100,
            avgKwhPer100Km = avgKwhPer100,
            totalSavings = savings
        )
    }
}
