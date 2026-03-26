package com.byd.dilink.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dilink_preferences")

@Singleton
class DiLinkPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        val SPEED_UNIT = stringPreferencesKey("speed_unit")
        val DISTANCE_UNIT = stringPreferencesKey("distance_unit")
        val ALTITUDE_UNIT = stringPreferencesKey("altitude_unit")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val LAST_BROWSE_PATH = stringPreferencesKey("last_browse_path")
        val RESUME_PLAYBACK = booleanPreferencesKey("resume_playback")
        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
    }

    val speedUnit: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SPEED_UNIT] ?: "km/h"
    }

    val distanceUnit: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DISTANCE_UNIT] ?: "km"
    }

    val altitudeUnit: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ALTITUDE_UNIT] ?: "m"
    }

    val temperatureUnit: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[TEMPERATURE_UNIT] ?: "°C"
    }

    val lastBrowsePath: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LAST_BROWSE_PATH] ?: ""
    }

    val resumePlayback: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[RESUME_PLAYBACK] ?: false
    }

    val eqEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[EQ_ENABLED] ?: false
    }

    suspend fun setSpeedUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[SPEED_UNIT] = unit
        }
    }

    suspend fun setDistanceUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[DISTANCE_UNIT] = unit
        }
    }

    suspend fun setAltitudeUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[ALTITUDE_UNIT] = unit
        }
    }

    suspend fun setTemperatureUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[TEMPERATURE_UNIT] = unit
        }
    }

    suspend fun setLastBrowsePath(path: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_BROWSE_PATH] = path
        }
    }

    suspend fun setResumePlayback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[RESUME_PLAYBACK] = enabled
        }
    }

    suspend fun setEqEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[EQ_ENABLED] = enabled
        }
    }
}
