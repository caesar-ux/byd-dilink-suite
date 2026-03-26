package com.byd.dilink.parking.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byd.dilink.core.data.entities.FavoriteLocation
import com.byd.dilink.core.data.entities.ParkingRecord
import com.byd.dilink.core.data.repository.ParkingRepository
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

data class ParkingUiState(
    val activeParking: ParkingRecord? = null,
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val hasLocation: Boolean = false,
    val compassHeading: Float = 0f,
    val distanceToCar: Float = 0f,
    val bearingToCar: Float = 0f,
    val elapsedTimeMs: Long = 0L,
    val timerRemainingMs: Long = -1L,
    val timerActive: Boolean = false,
    val isSaving: Boolean = false,
    val alarmPlaying: Boolean = false
)

@HiltViewModel
class ParkingViewModel @Inject constructor(
    application: Application,
    private val parkingRepository: ParkingRepository
) : AndroidViewModel(application), SensorEventListener, LocationListener {

    private val _uiState = MutableStateFlow(ParkingUiState())
    val uiState: StateFlow<ParkingUiState> = _uiState.asStateFlow()

    val activeParking = parkingRepository.activeParking
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val favorites = parkingRepository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history = parkingRepository.parkingHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val context: Context get() = getApplication<Application>().applicationContext
    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private var elapsedTimerJob: Job? = null
    private var countdownJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    init {
        viewModelScope.launch {
            activeParking.collect { parking ->
                _uiState.value = _uiState.value.copy(activeParking = parking)
                if (parking != null) {
                    startElapsedTimer(parking.parkedAt)
                    updateDistanceAndBearing()
                } else {
                    stopElapsedTimer()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        try {
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                locationManager.requestLocationUpdates(provider, 2000L, 1f, this)
            }
            // Also try to get last known location
            for (provider in providers) {
                val lastKnown = locationManager.getLastKnownLocation(provider)
                if (lastKnown != null) {
                    _uiState.value = _uiState.value.copy(
                        currentLatitude = lastKnown.latitude,
                        currentLongitude = lastKnown.longitude,
                        hasLocation = true
                    )
                    updateDistanceAndBearing()
                    break
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun stopLocationUpdates() {
        try {
            locationManager.removeUpdates(this)
        } catch (_: Exception) {}
    }

    fun startCompass() {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopCompass() {
        sensorManager.unregisterListener(this)
    }

    fun saveParking() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.hasLocation) {
                _uiState.value = state.copy(isSaving = true)
                parkingRepository.saveParking(
                    latitude = state.currentLatitude,
                    longitude = state.currentLongitude
                )
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun clearParking() {
        viewModelScope.launch {
            val parking = _uiState.value.activeParking
            if (parking != null) {
                parkingRepository.clearParking(parking.id)
                stopCountdownTimer()
                stopAlarm()
            }
        }
    }

    fun startParkingTimer(durationMinutes: Int) {
        val parking = _uiState.value.activeParking ?: return
        viewModelScope.launch {
            parkingRepository.setTimer(parking.id, durationMinutes)
        }
        stopCountdownTimer()
        val totalMs = durationMinutes * 60 * 1000L
        _uiState.value = _uiState.value.copy(
            timerActive = true,
            timerRemainingMs = totalMs
        )
        countdownJob = viewModelScope.launch {
            var remaining = totalMs
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining -= 1000
                _uiState.value = _uiState.value.copy(timerRemainingMs = remaining)
                // Play alarm when timer expires
                if (remaining <= 0) {
                    playAlarm()
                }
            }
        }
    }

    fun stopCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = _uiState.value.copy(timerActive = false, timerRemainingMs = -1L)
    }

    private fun playAlarm() {
        try {
            val alarmUri = Settings.System.DEFAULT_ALARM_ALERT_URI
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmUri)
                isLooping = true
                prepare()
                start()
            }
            _uiState.value = _uiState.value.copy(alarmPlaying = true)
        } catch (_: Exception) {
            // Alarm sound not available
        }
    }

    fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _uiState.value = _uiState.value.copy(alarmPlaying = false)
    }

    private fun startElapsedTimer(parkedAtMs: Long) {
        stopElapsedTimer()
        elapsedTimerJob = viewModelScope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - parkedAtMs
                _uiState.value = _uiState.value.copy(elapsedTimeMs = elapsed)
                delay(1000)
            }
        }
    }

    private fun stopElapsedTimer() {
        elapsedTimerJob?.cancel()
        elapsedTimerJob = null
    }

    private fun updateDistanceAndBearing() {
        val state = _uiState.value
        val parking = state.activeParking ?: return
        if (!state.hasLocation) return

        val results = FloatArray(3)
        Location.distanceBetween(
            state.currentLatitude, state.currentLongitude,
            parking.latitude, parking.longitude,
            results
        )
        val distance = results[0]
        val bearing = results[1]

        _uiState.value = state.copy(
            distanceToCar = distance,
            bearingToCar = bearing
        )
    }

    fun addFavorite(name: String, latitude: Double, longitude: Double, notes: String?) {
        viewModelScope.launch {
            parkingRepository.addFavorite(name, latitude, longitude, notes)
        }
    }

    fun deleteFavorite(location: FavoriteLocation) {
        viewModelScope.launch {
            parkingRepository.deleteFavorite(location)
        }
    }

    fun deleteHistoryRecord(record: ParkingRecord) {
        viewModelScope.launch {
            parkingRepository.deleteHistoryRecord(record)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            parkingRepository.clearAllHistory()
        }
    }

    fun navigateTo(latitude: Double, longitude: Double) {
        // This will be handled via intent in the UI layer
    }

    // LocationListener
    override fun onLocationChanged(location: Location) {
        _uiState.value = _uiState.value.copy(
            currentLatitude = location.latitude,
            currentLongitude = location.longitude,
            hasLocation = true
        )
        updateDistanceAndBearing()
    }

    @Deprecated("Deprecated in API")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    // SensorEventListener
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRadians = orientationAngles[0]
            val azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
            val heading = (azimuthDegrees + 360f) % 360f
            _uiState.value = _uiState.value.copy(compassHeading = heading)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        stopCompass()
        stopElapsedTimer()
        stopCountdownTimer()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
