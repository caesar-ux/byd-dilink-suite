package com.byd.dilink.media.service

import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AspectRatioMode(val displayName: String) {
    FIT("Fit"),
    FILL("Fill"),
    STRETCH("16:9"),
    FOUR_THREE("4:3")
}

class VideoPlayerHelper {

    var aspectRatioMode by mutableStateOf(AspectRatioMode.FIT)
        private set

    private var currentBrightness: Float = -1f

    fun cycleAspectRatio() {
        val values = AspectRatioMode.entries
        val currentIndex = values.indexOf(aspectRatioMode)
        aspectRatioMode = values[(currentIndex + 1) % values.size]
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        aspectRatioMode = mode
    }

    /**
     * Adjust screen brightness by a delta value (-1.0 to 1.0).
     * Left side vertical swipe gesture handler.
     */
    fun adjustBrightness(window: Window, delta: Float) {
        val layoutParams = window.attributes
        if (currentBrightness < 0) {
            currentBrightness = if (layoutParams.screenBrightness < 0) {
                0.5f
            } else {
                layoutParams.screenBrightness
            }
        }
        currentBrightness = (currentBrightness + delta * 0.5f).coerceIn(0.01f, 1.0f)
        layoutParams.screenBrightness = currentBrightness
        window.attributes = layoutParams
    }

    /**
     * Get current brightness level as a percentage (0-100)
     */
    fun getBrightnessPercent(): Int {
        return if (currentBrightness < 0) 50
        else (currentBrightness * 100).toInt()
    }

    /**
     * Reset brightness to system default
     */
    fun resetBrightness(window: Window) {
        currentBrightness = -1f
        val layoutParams = window.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams
    }
}
