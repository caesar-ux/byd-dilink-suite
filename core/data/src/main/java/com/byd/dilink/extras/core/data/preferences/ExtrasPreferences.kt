package com.byd.dilink.extras.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dilink_extras_prefs")

class ExtrasPreferences(private val context: Context) {

    // ============================================================
    // Hazard App Preferences
    // ============================================================
    object HazardKeys {
        val WARNING_DISTANCE_METERS = intPreferencesKey("hazard_warning_distance_meters")
        val WARNING_SOUND_ENABLED = booleanPreferencesKey("hazard_warning_sound_enabled")
        val WARNING_VOLUME = floatPreferencesKey("hazard_warning_volume")
        val AUTO_RECORD = booleanPreferencesKey("hazard_auto_record")
        val HAZARD_EXPIRY_DAYS = intPreferencesKey("hazard_expiry_days")
    }

    // ============================================================
    // Fuel Cost App Preferences
    // ============================================================
    object FuelKeys {
        val BATTERY_CAPACITY_KWH = doublePreferencesKey("fuel_battery_capacity_kwh")
        val DEFAULT_FUEL_PRICE = doublePreferencesKey("fuel_default_price_per_liter")
        val DEFAULT_ELECTRICITY_PRICE = doublePreferencesKey("fuel_default_electricity_price_per_kwh")
        val BENCHMARK_CONSUMPTION = doublePreferencesKey("fuel_benchmark_l_per_100km")
        val CURRENCY = stringPreferencesKey("fuel_currency")
        val DISTANCE_UNIT = stringPreferencesKey("fuel_distance_unit")
    }

    // ============================================================
    // Tire Guard App Preferences
    // ============================================================
    object TireKeys {
        val RECOMMENDED_PRESSURE_BAR = doublePreferencesKey("tire_recommended_pressure_bar")
        val LOW_PRESSURE_THRESHOLD = doublePreferencesKey("tire_low_pressure_threshold")
        val HIGH_PRESSURE_THRESHOLD = doublePreferencesKey("tire_high_pressure_threshold")
        val BATTERY_LOW_VOLTAGE = doublePreferencesKey("tire_battery_low_voltage")
        val BATTERY_HIGH_VOLTAGE = doublePreferencesKey("tire_battery_high_voltage")
        val CHECK_REMINDER_DAYS = intPreferencesKey("tire_check_reminder_days")
        val PRESSURE_UNIT = stringPreferencesKey("tire_pressure_unit")
    }

    // ============================================================
    // Prayer App Preferences
    // ============================================================
    object PrayerKeys {
        val CALCULATION_METHOD = stringPreferencesKey("prayer_calculation_method")
        val ASR_METHOD = stringPreferencesKey("prayer_asr_method")
        val FAJR_ADJUSTMENT = intPreferencesKey("prayer_fajr_adj")
        val SUNRISE_ADJUSTMENT = intPreferencesKey("prayer_sunrise_adj")
        val DHUHR_ADJUSTMENT = intPreferencesKey("prayer_dhuhr_adj")
        val ASR_ADJUSTMENT = intPreferencesKey("prayer_asr_adj")
        val MAGHRIB_ADJUSTMENT = intPreferencesKey("prayer_maghrib_adj")
        val ISHA_ADJUSTMENT = intPreferencesKey("prayer_isha_adj")
        val TIME_FORMAT_24H = booleanPreferencesKey("prayer_time_format_24h")
        val TASBEEH_VIBRATION = booleanPreferencesKey("prayer_tasbeeh_vibration")
        val TASBEEH_SOUND = booleanPreferencesKey("prayer_tasbeeh_sound")
        val DEFAULT_TASBEEH_GOAL = intPreferencesKey("prayer_default_tasbeeh_goal")
        val LOCATION_MODE = stringPreferencesKey("prayer_location_mode")
        val MANUAL_LATITUDE = doublePreferencesKey("prayer_manual_latitude")
        val MANUAL_LONGITUDE = doublePreferencesKey("prayer_manual_longitude")
        val TIMEZONE_OFFSET = doublePreferencesKey("prayer_timezone_offset")
    }

    // ============================================================
    // Generic read / write
    // ============================================================

    fun <T> get(key: Preferences.Key<T>, default: T): Flow<T> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: default
        }
    }

    suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    // ============================================================
    // Convenience getters with defaults
    // ============================================================

    // Hazard
    val warningDistanceMeters: Flow<Int> get() = get(HazardKeys.WARNING_DISTANCE_METERS, 500)
    val warningSoundEnabled: Flow<Boolean> get() = get(HazardKeys.WARNING_SOUND_ENABLED, true)
    val warningVolume: Flow<Float> get() = get(HazardKeys.WARNING_VOLUME, 0.7f)
    val autoRecord: Flow<Boolean> get() = get(HazardKeys.AUTO_RECORD, false)
    val hazardExpiryDays: Flow<Int> get() = get(HazardKeys.HAZARD_EXPIRY_DAYS, 0) // 0 = never

    // Fuel
    val batteryCapacityKwh: Flow<Double> get() = get(FuelKeys.BATTERY_CAPACITY_KWH, 8.3)
    val defaultFuelPrice: Flow<Double> get() = get(FuelKeys.DEFAULT_FUEL_PRICE, 750.0)
    val defaultElectricityPrice: Flow<Double> get() = get(FuelKeys.DEFAULT_ELECTRICITY_PRICE, 100.0)
    val benchmarkConsumption: Flow<Double> get() = get(FuelKeys.BENCHMARK_CONSUMPTION, 7.0)
    val currency: Flow<String> get() = get(FuelKeys.CURRENCY, "IQD")
    val distanceUnit: Flow<String> get() = get(FuelKeys.DISTANCE_UNIT, "km")

    // Tire
    val recommendedPressureBar: Flow<Double> get() = get(TireKeys.RECOMMENDED_PRESSURE_BAR, 2.4)
    val lowPressureThreshold: Flow<Double> get() = get(TireKeys.LOW_PRESSURE_THRESHOLD, 2.1)
    val highPressureThreshold: Flow<Double> get() = get(TireKeys.HIGH_PRESSURE_THRESHOLD, 2.7)
    val batteryLowVoltage: Flow<Double> get() = get(TireKeys.BATTERY_LOW_VOLTAGE, 12.0)
    val batteryHighVoltage: Flow<Double> get() = get(TireKeys.BATTERY_HIGH_VOLTAGE, 13.0)
    val checkReminderDays: Flow<Int> get() = get(TireKeys.CHECK_REMINDER_DAYS, 14)
    val pressureUnit: Flow<String> get() = get(TireKeys.PRESSURE_UNIT, "bar")

    // Prayer
    val calculationMethod: Flow<String> get() = get(PrayerKeys.CALCULATION_METHOD, "UMM_AL_QURA")
    val asrMethod: Flow<String> get() = get(PrayerKeys.ASR_METHOD, "SHAFI")
    val timeFormat24h: Flow<Boolean> get() = get(PrayerKeys.TIME_FORMAT_24H, true)
    val tasbeehVibration: Flow<Boolean> get() = get(PrayerKeys.TASBEEH_VIBRATION, true)
    val tasbeehSound: Flow<Boolean> get() = get(PrayerKeys.TASBEEH_SOUND, false)
    val defaultTasbeehGoal: Flow<Int> get() = get(PrayerKeys.DEFAULT_TASBEEH_GOAL, 33)
    val locationMode: Flow<String> get() = get(PrayerKeys.LOCATION_MODE, "AUTO")
    val timezoneOffset: Flow<Double> get() = get(PrayerKeys.TIMEZONE_OFFSET, 3.0)
}
