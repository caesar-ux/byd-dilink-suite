package com.byd.dilink.dashboard.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byd.dilink.dashboard.data.DashboardPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

data class TripData(
    val distanceKm: Double = 0.0,
    val elapsedMs: Long = 0L,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val isRunning: Boolean = false
)

data class DashboardState(
    val speedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val heading: Float = 0f,
    val altitude: Double = 0.0,
    val satellites: Int = 0,
    val accuracy: Float = 0f,
    val gForceX: Float = 0f,
    val gForceY: Float = 0f,
    val peakG: Float = 0f,
    val tripA: TripData = TripData(),
    val tripB: TripData = TripData(),
    val speedUnit: String = "km/h",
    val distanceUnit: String = "km",
    val altitudeUnit: String = "m",
    val displaySpeed: Double = 0.0,
    val displayMaxSpeed: Double = 0.0,
    val displayAltitude: Double = 0.0,
    val cardinalDirection: String = "N"
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    private val preferences: DashboardPreferences
) : AndroidViewModel(application), SensorEventListener, LocationListener {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    val speedUnit = preferences.speedUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "km/h")
    val distanceUnit = preferences.distanceUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "km")
    val altitudeUnit = preferences.altitudeUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "m")

    private val context: Context get() = getApplication<Application>().applicationContext
    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private var lastLocation: Location? = null
    private var tripAStartTime: Long = 0L
    private var tripBStartTime: Long = 0L
    private var tripAAccumulatedMs: Long = 0L
    private var tripBAccumulatedMs: Long = 0L
    private var tripALastResumeTime: Long = 0L
    private var tripBLastResumeTime: Long = 0L
    private var tripTimerJob: Job? = null

    // Low-pass filter for g-force
    private var filteredGX: Float = 0f
    private var filteredGY: Float = 0f

    // Sensor heading (used when stationary)
    private var sensorHeading: Float = 0f
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var gnssStatusCallback: GnssStatus.Callback? = null

    init {
        viewModelScope.launch {
            speedUnit.collect { unit ->
                updateDisplayValues(unit, distanceUnit.value, altitudeUnit.value)
            }
        }
        viewModelScope.launch {
            distanceUnit.collect { unit ->
                updateDisplayValues(speedUnit.value, unit, altitudeUnit.value)
            }
        }
        viewModelScope.launch {
            altitudeUnit.collect { unit ->
                updateDisplayValues(speedUnit.value, distanceUnit.value, unit)
            }
        }
        startTripTimer()
    }

    private fun updateDisplayValues(speedUnitStr: String, distUnitStr: String, altUnitStr: String) {
        val s = _state.value
        val isMph = speedUnitStr == "mph"
        val isMi = distUnitStr == "mi"
        val isFt = altUnitStr == "ft"

        _state.value = s.copy(
            speedUnit = speedUnitStr,
            distanceUnit = distUnitStr,
            altitudeUnit = altUnitStr,
            displaySpeed = if (isMph) s.speedKmh * 0.621371 else s.speedKmh,
            displayMaxSpeed = if (isMph) s.maxSpeedKmh * 0.621371 else s.maxSpeedKmh,
            displayAltitude = if (isFt) s.altitude * 3.28084 else s.altitude
        )
    }

    @SuppressLint("MissingPermission")
    fun startSensors() {
        try {
            // GPS
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000L, 0f, this
            )

            // GNSS status for satellite count
            gnssStatusCallback = object : GnssStatus.Callback() {
                override fun onSatelliteStatusChanged(status: GnssStatus) {
                    var usedCount = 0
                    for (i in 0 until status.satelliteCount) {
                        if (status.usedInFix(i)) usedCount++
                    }
                    _state.value = _state.value.copy(satellites = usedCount)
                }
            }
            locationManager.registerGnssStatusCallback(gnssStatusCallback!!)
        } catch (e: SecurityException) {
            // Permission not granted
        }

        // Rotation vector for compass heading
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }

        // Accelerometer for g-force
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopSensors() {
        try {
            locationManager.removeUpdates(this)
            gnssStatusCallback?.let { locationManager.unregisterGnssStatusCallback(it) }
        } catch (_: Exception) {}
        sensorManager.unregisterListener(this)
    }

    private fun startTripTimer() {
        tripTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val now = System.currentTimeMillis()
                val s = _state.value

                var updatedTripA = s.tripA
                var updatedTripB = s.tripB

                if (updatedTripA.isRunning && tripALastResumeTime > 0) {
                    val totalMs = tripAAccumulatedMs + (now - tripALastResumeTime)
                    val avgSpeed = if (totalMs > 0) updatedTripA.distanceKm / (totalMs / 3_600_000.0) else 0.0
                    updatedTripA = updatedTripA.copy(elapsedMs = totalMs, avgSpeedKmh = avgSpeed)
                }
                if (updatedTripB.isRunning && tripBLastResumeTime > 0) {
                    val totalMs = tripBAccumulatedMs + (now - tripBLastResumeTime)
                    val avgSpeed = if (totalMs > 0) updatedTripB.distanceKm / (totalMs / 3_600_000.0) else 0.0
                    updatedTripB = updatedTripB.copy(elapsedMs = totalMs, avgSpeedKmh = avgSpeed)
                }

                _state.value = s.copy(tripA = updatedTripA, tripB = updatedTripB)
            }
        }
    }

    fun resetTripA() {
        tripAAccumulatedMs = 0L
        tripAStartTime = System.currentTimeMillis()
        tripALastResumeTime = 0L
        _state.value = _state.value.copy(tripA = TripData())
    }

    fun resetTripB() {
        tripBAccumulatedMs = 0L
        tripBStartTime = System.currentTimeMillis()
        tripBLastResumeTime = 0L
        _state.value = _state.value.copy(tripB = TripData())
    }

    fun resetMaxSpeed() {
        _state.value = _state.value.copy(maxSpeedKmh = 0.0, displayMaxSpeed = 0.0)
    }

    fun toggleSpeedUnit() {
        viewModelScope.launch {
            val current = speedUnit.value
            val newUnit = if (current == "km/h") "mph" else "km/h"
            preferences.setSpeedUnit(newUnit)
        }
    }

    fun toggleDistanceUnit() {
        viewModelScope.launch {
            val current = distanceUnit.value
            preferences.setDistanceUnit(if (current == "km") "mi" else "km")
        }
    }

    fun toggleAltitudeUnit() {
        viewModelScope.launch {
            val current = altitudeUnit.value
            preferences.setAltitudeUnit(if (current == "m") "ft" else "m")
        }
    }

    // LocationListener
    override fun onLocationChanged(location: Location) {
        val speedMs = if (location.hasSpeed()) location.speed.toDouble() else 0.0
        val speedKmh = speedMs * 3.6

        val s = _state.value
        val newMaxSpeed = maxOf(s.maxSpeedKmh, speedKmh)
        val altitude = if (location.hasAltitude()) location.altitude else s.altitude
        val accuracy = if (location.hasAccuracy()) location.accuracy else s.accuracy

        // Use GPS bearing when moving > 5 km/h, otherwise use sensor heading
        val heading = if (speedKmh > 5.0 && location.hasBearing()) {
            location.bearing
        } else {
            sensorHeading
        }

        // Trip distance accumulation
        val prev = lastLocation
        var tripA = s.tripA
        var tripB = s.tripB

        if (prev != null) {
            val distMeters = prev.distanceTo(location)
            // Only accumulate if reasonable (filter GPS jumps > 100m in 1 second)
            if (distMeters < 100f) {
                val distKm = distMeters / 1000.0

                // Auto-start/stop trips based on speed > 1 km/h
                val isMoving = speedKmh > 1.0
                val now = System.currentTimeMillis()

                if (isMoving && !tripA.isRunning) {
                    tripALastResumeTime = now
                    tripA = tripA.copy(isRunning = true)
                } else if (!isMoving && tripA.isRunning) {
                    tripAAccumulatedMs += now - tripALastResumeTime
                    tripALastResumeTime = 0L
                    tripA = tripA.copy(isRunning = false)
                }

                if (isMoving && !tripB.isRunning) {
                    tripBLastResumeTime = now
                    tripB = tripB.copy(isRunning = true)
                } else if (!isMoving && tripB.isRunning) {
                    tripBAccumulatedMs += now - tripBLastResumeTime
                    tripBLastResumeTime = 0L
                    tripB = tripB.copy(isRunning = false)
                }

                if (isMoving) {
                    tripA = tripA.copy(
                        distanceKm = tripA.distanceKm + distKm,
                        maxSpeedKmh = maxOf(tripA.maxSpeedKmh, speedKmh)
                    )
                    tripB = tripB.copy(
                        distanceKm = tripB.distanceKm + distKm,
                        maxSpeedKmh = maxOf(tripB.maxSpeedKmh, speedKmh)
                    )
                }
            }
        }

        lastLocation = location

        val isMph = s.speedUnit == "mph"
        _state.value = s.copy(
            speedKmh = speedKmh,
            maxSpeedKmh = newMaxSpeed,
            heading = heading,
            altitude = altitude,
            accuracy = accuracy,
            tripA = tripA,
            tripB = tripB,
            displaySpeed = if (isMph) speedKmh * 0.621371 else speedKmh,
            displayMaxSpeed = if (isMph) newMaxSpeed * 0.621371 else newMaxSpeed,
            displayAltitude = if (s.altitudeUnit == "ft") altitude * 3.28084 else altitude,
            cardinalDirection = getCardinalDirection(heading)
        )
    }

    @Deprecated("Deprecated in API")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    // SensorEventListener
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                sensorHeading = (azimuthDeg + 360f) % 360f

                // Update heading if stationary
                if (_state.value.speedKmh <= 5.0) {
                    _state.value = _state.value.copy(
                        heading = sensorHeading,
                        cardinalDirection = getCardinalDirection(sensorHeading)
                    )
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // G-force calculation with low-pass filter
                val rawGX = event.values[0] / 9.81f  // lateral
                val rawGY = event.values[1] / 9.81f  // longitudinal

                // Low-pass filter: filtered = prev * 0.8 + current * 0.2
                filteredGX = filteredGX * 0.8f + rawGX * 0.2f
                filteredGY = filteredGY * 0.8f + rawGY * 0.2f

                val totalG = sqrt(filteredGX * filteredGX + filteredGY * filteredGY)
                val s = _state.value

                _state.value = s.copy(
                    gForceX = filteredGX,
                    gForceY = filteredGY,
                    peakG = maxOf(s.peakG, totalG)
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getCardinalDirection(degrees: Float): String {
        val normalized = ((degrees % 360) + 360) % 360
        return when {
            normalized < 22.5 -> "N"
            normalized < 67.5 -> "NE"
            normalized < 112.5 -> "E"
            normalized < 157.5 -> "SE"
            normalized < 202.5 -> "S"
            normalized < 247.5 -> "SW"
            normalized < 292.5 -> "W"
            normalized < 337.5 -> "NW"
            else -> "N"
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
        tripTimerJob?.cancel()
    }
}
