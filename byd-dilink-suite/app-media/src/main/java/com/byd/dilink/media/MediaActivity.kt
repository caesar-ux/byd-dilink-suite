package com.byd.dilink.media

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.media.navigation.MediaNavHost
import com.byd.dilink.media.service.MediaPlaybackService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaActivity : ComponentActivity() {

    @Inject
    lateinit var playbackService: MediaPlaybackService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize playback service with audio manager
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        playbackService.init(audioManager)

        setContent {
            DiLinkTheme {
                MediaNavHost()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            playbackService.release()
        }
    }
}
