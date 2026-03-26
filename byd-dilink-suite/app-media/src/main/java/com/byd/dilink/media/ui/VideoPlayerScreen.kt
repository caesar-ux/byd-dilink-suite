package com.byd.dilink.media.ui

import android.media.AudioManager
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.media.service.VideoPlayerHelper
import com.byd.dilink.media.viewmodel.MediaViewModel
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerScreen(
    onBack: () -> Unit,
    mediaViewModel: MediaViewModel = hiltViewModel()
) {
    val playbackState by mediaViewModel.playbackState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val videoHelper = remember { VideoPlayerHelper() }

    var controlsVisible by remember { mutableStateOf(true) }
    var controlsLocked by remember { mutableStateOf(false) }

    // Auto-hide controls
    LaunchedEffect(controlsVisible, playbackState.isPlaying) {
        if (controlsVisible && playbackState.isPlaying && !controlsLocked) {
            delay(3000)
            controlsVisible = false
        }
    }

    // Full screen
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        if (window != null) {
            val controller = WindowInsetsControllerCompat(window, view)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            if (window != null) {
                val controller = WindowInsetsControllerCompat(window, view)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(controlsLocked) {
                detectTapGestures(
                    onTap = {
                        if (!controlsLocked) {
                            controlsVisible = !controlsVisible
                        }
                    }
                )
            }
            .pointerInput(controlsLocked) {
                if (controlsLocked) return@pointerInput
                val screenWidth = size.width.toFloat()
                val screenHeight = size.height.toFloat()

                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val x = change.position.x
                    val isLeftSide = x < screenWidth / 2

                    if (kotlin.math.abs(dragAmount.y) > kotlin.math.abs(dragAmount.x)) {
                        // Vertical swipe
                        val delta = -dragAmount.y / screenHeight
                        if (isLeftSide) {
                            // Brightness control
                            val window = (context as? android.app.Activity)?.window
                            if (window != null) {
                                videoHelper.adjustBrightness(window, delta)
                            }
                        } else {
                            // Volume control
                            val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            val newVol = (currentVol + delta * maxVol * 0.5f).toInt().coerceIn(0, maxVol)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                        }
                    } else if (kotlin.math.abs(dragAmount.x) > 30) {
                        // Horizontal swipe: seek
                        val seekDelta = (dragAmount.x / screenWidth * 60000).toLong() // max 60s
                        val currentPos = playbackState.positionMs
                        val newPos = (currentPos + seekDelta).coerceIn(0, playbackState.durationMs)
                        mediaViewModel.seekTo(newPos)
                    }
                }
            }
    ) {
        // Video surface
        AndroidView(
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            mediaViewModel.setSurface(holder.surface)
                        }
                        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            mediaViewModel.setSurface(null)
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay controls
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = playbackState.currentTrack?.displayTitle ?: "Video",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White
                        ),
                        maxLines = 1
                    )
                }

                // Center controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newPos = (playbackState.positionMs - 10000).coerceAtLeast(0)
                            mediaViewModel.seekTo(newPos)
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Surface(
                        onClick = { mediaViewModel.togglePlayPause() },
                        shape = CircleShape,
                        color = DiLinkCyan,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                tint = DiLinkBackground,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val newPos = (playbackState.positionMs + 10000).coerceAtMost(playbackState.durationMs)
                            mediaViewModel.seekTo(newPos)
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Bottom bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    // Seek bar
                    var seekPosition by remember(playbackState.positionMs) {
                        mutableFloatStateOf(
                            if (playbackState.durationMs > 0) {
                                playbackState.positionMs.toFloat() / playbackState.durationMs
                            } else 0f
                        )
                    }

                    Slider(
                        value = seekPosition,
                        onValueChange = { seekPosition = it },
                        onValueChangeFinished = {
                            mediaViewModel.seekTo((seekPosition * playbackState.durationMs).toLong())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = DiLinkCyan,
                            activeTrackColor = DiLinkCyan,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatTime(playbackState.positionMs)} / ${formatTime(playbackState.durationMs)}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Aspect ratio
                            IconButton(
                                onClick = { videoHelper.cycleAspectRatio() },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.AspectRatio,
                                        contentDescription = "Aspect Ratio",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        videoHelper.aspectRatioMode.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            // Lock button
                            IconButton(
                                onClick = {
                                    controlsLocked = !controlsLocked
                                },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = if (controlsLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = if (controlsLocked) "Unlock" else "Lock",
                                    tint = if (controlsLocked) DiLinkCyan else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 800, heightDp = 400)
@Composable
private fun VideoPlayerOverlayPreview() {
    DiLinkTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Text(
                "Video Player Preview",
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
