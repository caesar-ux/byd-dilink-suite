package com.byd.dilink.dashboard.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dashboardDataStore: DataStore<Preferences> by preferencesDataStore(name = "dashboard_preferences")

@Singleton
class DashboardPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        val SPEED_UNIT = stringPreferencesKey("dashboard_speed_unit")
        val DISTANCE_UNIT = stringPreferencesKey("dashboard_distance_unit")
        val ALTITUDE_UNIT = stringPreferencesKey("dashboard_altitude_unit")
        val SPEED_WARNING_THRESHOLD = floatPreferencesKey("speed_warning_threshold")
        val GPS_INTERVAL = intPreferencesKey("gps_interval_ms")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    val speedUnit: Flow<String> = context.dashboardDataStore.data.map { prefs ->
        prefs[SPEED_UNIT] ?: "km/h"
    }

    val distanceUnit: Flow<String> = context.dashboardDataStore.data.map { prefs ->
        prefs[DISTANCE_UNIT] ?: "km"
    }

    val altitudeUnit: Flow<String> = context.dashboardDataStore.data.map { prefs ->
        prefs[ALTITUDE_UNIT] ?: "m"
    }

    val speedWarningThreshold: Flow<Float> = context.dashboardDataStore.data.map { prefs ->
        prefs[SPEED_WARNING_THRESHOLD] ?: 140f
    }

    val gpsInterval: Flow<Int> = context.dashboardDataStore.data.map { prefs ->
        prefs[GPS_INTERVAL] ?: 1000
    }

    val keepScreenOn: Flow<Boolean> = context.dashboardDataStore.data.map { prefs ->
        prefs[KEEP_SCREEN_ON] ?: true
    }

    suspend fun setSpeedUnit(unit: String) {
        context.dashboardDataStore.edit { prefs -> prefs[SPEED_UNIT] = unit }
    }

    suspend fun setDistanceUnit(unit: String) {
        context.dashboardDataStore.edit { prefs -> prefs[DISTANCE_UNIT] = unit }
    }

    suspend fun setAltitudeUnit(unit: String) {
        context.dashboardDataStore.edit { prefs -> prefs[ALTITUDE_UNIT] = unit }
    }

    suspend fun setSpeedWarningThreshold(threshold: Float) {
        context.dashboardDataStore.edit { prefs -> prefs[SPEED_WARNING_THRESHOLD] = threshold }
    }

    suspend fun setGpsInterval(intervalMs: Int) {
        context.dashboardDataStore.edit { prefs -> prefs[GPS_INTERVAL] = intervalMs }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dashboardDataStore.edit { prefs -> prefs[KEEP_SCREEN_ON] = enabled }
    }
}
