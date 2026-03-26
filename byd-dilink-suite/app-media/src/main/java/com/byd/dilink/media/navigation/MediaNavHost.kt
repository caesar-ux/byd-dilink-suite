package com.byd.dilink.media.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.byd.dilink.media.ui.EqualizerScreen
import com.byd.dilink.media.ui.FileBrowserScreen
import com.byd.dilink.media.ui.NowPlayingScreen
import com.byd.dilink.media.ui.QueueScreen
import com.byd.dilink.media.ui.SettingsScreen
import com.byd.dilink.media.ui.VideoPlayerScreen

object MediaRoutes {
    const val NOW_PLAYING = "now_playing"
    const val FILE_BROWSER = "file_browser"
    const val QUEUE = "queue"
    const val EQUALIZER = "equalizer"
    const val SETTINGS = "settings"
    const val VIDEO_PLAYER = "video_player"
}

@Composable
fun MediaNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MediaRoutes.NOW_PLAYING
    ) {
        composable(MediaRoutes.NOW_PLAYING) {
            NowPlayingScreen(
                onNavigateToFileBrowser = {
                    navController.navigate(MediaRoutes.FILE_BROWSER)
                },
                onNavigateToQueue = {
                    navController.navigate(MediaRoutes.QUEUE)
                },
                onNavigateToEqualizer = {
                    navController.navigate(MediaRoutes.EQUALIZER)
                },
                onNavigateToSettings = {
                    navController.navigate(MediaRoutes.SETTINGS)
                },
                onNavigateToVideoPlayer = {
                    navController.navigate(MediaRoutes.VIDEO_PLAYER)
                }
            )
        }

        composable(MediaRoutes.FILE_BROWSER) {
            FileBrowserScreen(
                onBack = { navController.popBackStack() },
                onNavigateToVideoPlayer = {
                    navController.navigate(MediaRoutes.VIDEO_PLAYER)
                }
            )
        }

        composable(MediaRoutes.QUEUE) {
            QueueScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(MediaRoutes.EQUALIZER) {
            EqualizerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(MediaRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(MediaRoutes.VIDEO_PLAYER) {
            VideoPlayerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
