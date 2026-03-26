package com.byd.dilink.extras.hazard.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byd.dilink.extras.core.data.entities.HazardRecord
import com.byd.dilink.extras.core.data.preferences.ExtrasPreferences
import com.byd.dilink.extras.core.data.repository.HazardRepository
import com.byd.dilink.extras.hazard.model.HazardType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class HazardWithDistance(
    val record: HazardRecord,
    val distanceMeters: Double,
    val bearing: Double,
    val direction: String
)

data class LiveDriveUiState(
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val currentSpeed: Float = 0f,
    val currentHeading: Float = 0f,
    val isRecording: Boolean = false,
    val nearbyHazards: List<HazardWithDistance> = emptyList(),
    val nearbyCount: Int = 0,
    val closestHazard: HazardWithDistance? = null,
    val warningActive: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val savedMessage: String? = null,
    val warningDistanceMeters: Int = 500,
    val warningSoundEnabled: Boolean = true,
    val warningVolume: Float = 0.7f,
    val autoRecord: Boolean = false
)

@HiltViewModel
class HazardViewModel @Inject constructor(
    application: Application,
    private val hazardRepository: HazardRepository,
    private val preferences: ExtrasPreferences
) : AndroidViewModel(application), LocationListener, SensorEventListener {

    private val _uiState = MutableStateFlow(LiveDriveUiState())
    val uiState: StateFlow<LiveDriveUiState> = _uiState.asStateFlow()

    private val _allHazards = MutableStateFlow<List<HazardRecord>>(emptyList())
    val allHazards: StateFlow<List<HazardRecord>> = _allHazards.asStateFlow()

    private val _allHazardsWithDistance = MutableStateFlow<List<HazardWithDistance>>(emptyList())
    val allHazardsWithDistance: StateFlow<List<HazardWithDistance>> = _allHazardsWithDistance.asStateFlow()

    private val _hazardCount = MutableStateFlow(0)
    val hazardCount: StateFlow<Int> = _hazardCount.asStateFlow()

    private var locationManager: LocationManager? = null
    private var sensorManager: SensorManager? = null
    private var toneGenerator: ToneGenerator? = null

    private var lastWarningHazardId: Long = -1L
    private var lastWarningTime: Long = 0L
    private val WARNING_DEBOUNCE_MS = 60_000L

    private var proximityJob: Job? = null
    private var compassHeading: Float = 0f

    init {
        val ctx = getApplication<Application>()
        locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        sensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
        } catch (_: Exception) { }

        viewModelScope.launch {
            hazardRepository.getAllHazards().collect { records ->
                _allHazards.value = records
                _hazardCount.value = records.size
                updateNearbyHazards()
            }
        }

        viewModelScope.launch {
            preferences.warningDistanceMeters.collect { distance ->
                _uiState.value = _uiState.value.copy(warningDistanceMeters = distance)
            }
        }
        viewModelScope.launch {
            preferences.warningSoundEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(warningSoundEnabled = enabled)
            }
        }
        viewModelScope.launch {
            preferences.warningVolume.collect { volume ->
                _uiState.value = _uiState.value.copy(warningVolume = volume)
            }
        }
        viewModelScope.launch {
            preferences.autoRecord.collect { auto ->
                _uiState.value = _uiState.value.copy(autoRecord = auto)
            }
        }

        registerSensorListener()
        checkAndStartLocation()
    }

    private fun checkAndStartLocation() {
        val ctx = getApplication<Application>()
        val hasPerm = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.value = _uiState.value.copy(hasLocationPermission = hasPerm)
        if (hasPerm) {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val ctx = getApplication<Application>()
        val hasPerm = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPerm) return

        _uiState.value = _uiState.value.copy(hasLocationPermission = true)

        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000L,
                5f,
                this
            )
        } catch (_: Exception) { }
    }

    fun stopLocationUpdates() {
        try {
            locationManager?.removeUpdates(this)
        } catch (_: Exception) { }
    }

    private fun registerSensorListener() {
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor != null) {
            sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // LocationListener
    override fun onLocationChanged(location: Location) {
        val speed = location.speed * 3.6f // m/s to km/h
        val heading = if (location.hasBearing() && speed > 5f) {
            location.bearing
        } else {
            compassHeading
        }

        _uiState.value = _uiState.value.copy(
            currentLatitude = location.latitude,
            currentLongitude = location.longitude,
            currentSpeed = speed,
            currentHeading = heading
        )

        // Auto record if speed > 10 km/h
        if (_uiState.value.autoRecord && speed > 10f && !_uiState.value.isRecording) {
            _uiState.value = _uiState.value.copy(isRecording = true)
        }

        updateNearbyHazards()
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }
    override fun onProviderEnabled(provider: String) { }
    override fun onProviderDisabled(provider: String) { }

    // SensorEventListener
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            compassHeading = ((Math.toDegrees(orientation[0].toDouble()).toFloat()) + 360) % 360

            // Update heading if speed is low (GPS bearing unreliable at low speeds)
            if (_uiState.value.currentSpeed < 5f) {
                _uiState.value = _uiState.value.copy(currentHeading = compassHeading)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    private fun updateNearbyHazards() {
        val state = _uiState.value
        if (state.currentLatitude == 0.0 && state.currentLongitude == 0.0) return

        val hazards = _allHazards.value
        val withDistance = hazards.map { record ->
            val distance = haversineDistance(
                state.currentLatitude, state.currentLongitude,
                record.latitude, record.longitude
            )
            val brng = bearing(
                state.currentLatitude, state.currentLongitude,
                record.latitude, record.longitude
            )
            HazardWithDistance(
                record = record,
                distanceMeters = distance,
                bearing = brng,
                direction = bearingToDirection(brng)
            )
        }.sortedBy { it.distanceMeters }

        val nearby = withDistance.filter { it.distanceMeters <= 5000.0 }
        val closest = nearby.firstOrNull { it.distanceMeters <= state.warningDistanceMeters }

        val warningActive: Boolean
        val now = System.currentTimeMillis()

        if (closest != null) {
            val shouldWarn = closest.record.id != lastWarningHazardId ||
                    (now - lastWarningTime) > WARNING_DEBOUNCE_MS
            warningActive = true

            if (shouldWarn && state.isRecording) {
                lastWarningHazardId = closest.record.id
                lastWarningTime = now
                if (state.warningSoundEnabled) {
                    playWarningSound()
                }
            }
        } else {
            warningActive = false
        }

        _uiState.value = state.copy(
            nearbyHazards = nearby,
            nearbyCount = nearby.size,
            closestHazard = closest,
            warningActive = warningActive
        )

        _allHazardsWithDistance.value = withDistance
    }

    private fun playWarningSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300)
        } catch (_: Exception) { }
    }

    fun startRecording() {
        _uiState.value = _uiState.value.copy(isRecording = true)
    }

    fun stopRecording() {
        _uiState.value = _uiState.value.copy(isRecording = false)
    }

    fun toggleRecording() {
        if (_uiState.value.isRecording) stopRecording() else startRecording()
    }

    fun addHazard(type: HazardType, notes: String? = null) {
        val state = _uiState.value
        if (state.currentLatitude == 0.0 && state.currentLongitude == 0.0) return

        viewModelScope.launch {
            val record = HazardRecord(
                type = type.name,
                latitude = state.currentLatitude,
                longitude = state.currentLongitude,
                heading = state.currentHeading,
                speed = state.currentSpeed,
                timestamp = System.currentTimeMillis(),
                notes = notes,
                confirmed = 1
            )
            hazardRepository.insertHazard(record)
            _uiState.value = _uiState.value.copy(savedMessage = "Saved!")
            delay(1500)
            _uiState.value = _uiState.value.copy(savedMessage = null)
        }
    }

    fun deleteHazard(id: Long) {
        viewModelScope.launch {
            hazardRepository.deleteHazardById(id)
        }
    }

    fun deleteAllHazards() {
        viewModelScope.launch {
            hazardRepository.deleteAllHazards()
        }
    }

    fun cleanExpiredHazards() {
        viewModelScope.launch {
            val expiryDays = preferences.hazardExpiryDays.first()
            if (expiryDays > 0) {
                val cutoff = System.currentTimeMillis() - (expiryDays.toLong() * 24 * 60 * 60 * 1000)
                hazardRepository.deleteExpiredHazards(cutoff)
            }
        }
    }

    fun exportToJson(): File? {
        val hazards = _allHazards.value
        if (hazards.isEmpty()) return null

        val jsonArray = JSONArray()
        for (h in hazards) {
            val obj = JSONObject().apply {
                put("id", h.id)
                put("type", h.type)
                put("latitude", h.latitude)
                put("longitude", h.longitude)
                put("heading", h.heading.toDouble())
                put("speed", h.speed.toDouble())
                put("timestamp", h.timestamp)
                put("notes", h.notes ?: "")
                put("confirmed", h.confirmed)
            }
            jsonArray.put(obj)
        }

        return try {
            val dir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(dir, "hazards_export_${System.currentTimeMillis()}.json")
            file.writeText(jsonArray.toString(2))
            file
        } catch (_: Exception) {
            null
        }
    }

    fun importFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val record = HazardRecord(
                        type = obj.getString("type"),
                        latitude = obj.getDouble("latitude"),
                        longitude = obj.getDouble("longitude"),
                        heading = obj.getDouble("heading").toFloat(),
                        speed = obj.getDouble("speed").toFloat(),
                        timestamp = obj.getLong("timestamp"),
                        notes = obj.optString("notes", null),
                        confirmed = obj.optInt("confirmed", 1)
                    )
                    hazardRepository.insertHazard(record)
                }
            } catch (_: Exception) { }
        }
    }

    fun getRouteHazards(
        fromLat: Double, fromLon: Double,
        toLat: Double, toLon: Double,
        corridorWidthMeters: Double
    ): List<HazardWithDistance> {
        val state = _uiState.value
        val hazards = _allHazards.value
        val halfWidth = corridorWidthMeters / 2.0

        return hazards.mapNotNull { record ->
            val perpDist = HazardRepository.perpendicularDistance(
                record.latitude, record.longitude,
                fromLat, fromLon,
                toLat, toLon
            )

            if (perpDist <= halfWidth) {
                val distFromStart = haversineDistance(fromLat, fromLon, record.latitude, record.longitude)
                val brng = bearing(fromLat, fromLon, record.latitude, record.longitude)
                HazardWithDistance(
                    record = record,
                    distanceMeters = distFromStart,
                    bearing = brng,
                    direction = bearingToDirection(brng)
                )
            } else {
                null
            }
        }.sortedBy { it.distanceMeters }
    }

    suspend fun updateWarningDistance(meters: Int) {
        preferences.set(ExtrasPreferences.HazardKeys.WARNING_DISTANCE_METERS, meters)
    }

    suspend fun updateWarningSound(enabled: Boolean) {
        preferences.set(ExtrasPreferences.HazardKeys.WARNING_SOUND_ENABLED, enabled)
    }

    suspend fun updateWarningVolume(volume: Float) {
        preferences.set(ExtrasPreferences.HazardKeys.WARNING_VOLUME, volume)
    }

    suspend fun updateAutoRecord(enabled: Boolean) {
        preferences.set(ExtrasPreferences.HazardKeys.AUTO_RECORD, enabled)
    }

    suspend fun updateHazardExpiry(days: Int) {
        preferences.set(ExtrasPreferences.HazardKeys.HAZARD_EXPIRY_DAYS, days)
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        sensorManager?.unregisterListener(this)
        toneGenerator?.release()
    }

    companion object {
        private const val EARTH_RADIUS_METERS = 6371000.0

        fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return EARTH_RADIUS_METERS * c
        }

        fun bearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val lat1Rad = Math.toRadians(lat1)
            val lat2Rad = Math.toRadians(lat2)
            val dLon = Math.toRadians(lon2 - lon1)
            val y = sin(dLon) * cos(lat2Rad)
            val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
            val b = Math.toDegrees(atan2(y, x))
            return (b + 360) % 360
        }

        fun bearingToDirection(bearing: Double): String {
            val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
            val index = ((bearing + 22.5) / 45).toInt() % 8
            return directions[index]
        }
    }
}
