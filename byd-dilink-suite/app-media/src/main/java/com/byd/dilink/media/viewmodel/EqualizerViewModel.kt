package com.byd.dilink.media.viewmodel

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import androidx.lifecycle.ViewModel
import com.byd.dilink.core.data.preferences.DiLinkPreferences
import com.byd.dilink.media.data.EqPreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val preferences: DiLinkPreferences
) : ViewModel() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val _bandLevels = MutableStateFlow(listOf(0, 0, 0, 0, 0))
    val bandLevels: StateFlow<List<Int>> = _bandLevels.asStateFlow()

    private val _currentPreset = MutableStateFlow(EqPreset.FLAT)
    val currentPreset: StateFlow<EqPreset> = _currentPreset.asStateFlow()

    private val _eqEnabled = MutableStateFlow(true)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()

    private val _bassBoostEnabled = MutableStateFlow(false)
    val bassBoostEnabled: StateFlow<Boolean> = _bassBoostEnabled.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow(500)
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength.asStateFlow()

    private val _virtualizerEnabled = MutableStateFlow(false)
    val virtualizerEnabled: StateFlow<Boolean> = _virtualizerEnabled.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(500)
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()

    fun attachToAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        releaseEffects()

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _eqEnabled.value
                // Apply current band levels
                val bands = numberOfBands.toInt()
                val levels = _bandLevels.value
                for (i in 0 until minOf(bands, levels.size)) {
                    setBandLevel(i.toShort(), levels[i].toShort())
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("EqualizerVM", "Failed to create equalizer", e)
        }

        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _bassBoostEnabled.value
                if (_bassBoostEnabled.value) {
                    setStrength(_bassBoostStrength.value.toShort())
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("EqualizerVM", "Failed to create bass boost", e)
        }

        try {
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _virtualizerEnabled.value
                if (_virtualizerEnabled.value) {
                    setStrength(_virtualizerStrength.value.toShort())
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("EqualizerVM", "Failed to create virtualizer", e)
        }
    }

    fun toggleEq() {
        val newEnabled = !_eqEnabled.value
        _eqEnabled.value = newEnabled
        equalizer?.enabled = newEnabled
    }

    fun setBandLevel(bandIndex: Int, level: Int) {
        val levels = _bandLevels.value.toMutableList()
        if (bandIndex in levels.indices) {
            val clampedLevel = level.coerceIn(-1500, 1500)
            levels[bandIndex] = clampedLevel
            _bandLevels.value = levels
            _currentPreset.value = EqPreset.CUSTOM.copy(bands = levels)

            try {
                equalizer?.setBandLevel(bandIndex.toShort(), clampedLevel.toShort())
            } catch (_: Exception) {}
        }
    }

    fun applyPreset(preset: EqPreset) {
        _currentPreset.value = preset
        _bandLevels.value = preset.bands.toList()

        equalizer?.let { eq ->
            val bands = eq.numberOfBands.toInt()
            for (i in 0 until minOf(bands, preset.bands.size)) {
                try {
                    eq.setBandLevel(i.toShort(), preset.bands[i].toShort())
                } catch (_: Exception) {}
            }
        }
    }

    fun toggleBassBoost() {
        val newEnabled = !_bassBoostEnabled.value
        _bassBoostEnabled.value = newEnabled
        bassBoost?.enabled = newEnabled
    }

    fun setBassBoostStrength(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _bassBoostStrength.value = clamped
        try {
            bassBoost?.setStrength(clamped.toShort())
        } catch (_: Exception) {}
    }

    fun toggleVirtualizer() {
        val newEnabled = !_virtualizerEnabled.value
        _virtualizerEnabled.value = newEnabled
        virtualizer?.enabled = newEnabled
    }

    fun setVirtualizerStrength(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _virtualizerStrength.value = clamped
        try {
            virtualizer?.setStrength(clamped.toShort())
        } catch (_: Exception) {}
    }

    fun saveCustomPreset() {
        // Save current band levels as the "Custom" preset
        _currentPreset.value = EqPreset.CUSTOM.copy(bands = _bandLevels.value)
    }

    private fun releaseEffects() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
    }

    override fun onCleared() {
        super.onCleared()
        releaseEffects()
    }
}
