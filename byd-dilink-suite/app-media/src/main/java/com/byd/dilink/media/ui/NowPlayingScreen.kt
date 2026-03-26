package com.byd.dilink.media.ui

import android.content.res.Configuration
import android.media.AudioManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.MediaPurple
import com.byd.dilink.media.service.RepeatMode
import com.byd.dilink.media.viewmodel.MediaViewModel

@Composable
fun NowPlayingScreen(
    onNavigateToBrowser: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToVideoPlayer: () -> Unit,
    mediaViewModel: MediaViewModel = hiltViewModel()
) {
    val playbackState by mediaViewModel.playbackState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        NowPlayingLandscape(
            playbackState = playbackState,
            onPlayPause = { mediaViewModel.togglePlayPause() },
            onNext = { mediaViewModel.next() },
            onPrevious = { mediaViewModel.previous() },
            onSeek = { mediaViewModel.seekTo(it) },
            onToggleShuffle = { mediaViewModel.toggleShuffle() },
            onCycleRepeat = { mediaViewModel.cycleRepeat() },
            onNavigateToBrowser = onNavigateToBrowser,
            onNavigateToQueue = onNavigateToQueue,
            onNavigateToEqualizer = onNavigateToEqualizer,
            onNavigateToSettings = onNavigateToSettings
        )
    } else {
        NowPlayingPortrait(
            playbackState = playbackState,
            onPlayPause = { mediaViewModel.togglePlayPause() },
            onNext = { mediaViewModel.next() },
            onPrevious = { mediaViewModel.previous() },
            onSeek = { mediaViewModel.seekTo(it) },
            onToggleShuffle = { mediaViewModel.toggleShuffle() },
            onCycleRepeat = { mediaViewModel.cycleRepeat() },
            onNavigateToBrowser = onNavigateToBrowser,
            onNavigateToQueue = onNavigateToQueue,
            onNavigateToEqualizer = onNavigateToEqualizer,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}

@Composable
private fun NowPlayingPortrait(
    playbackState: com.byd.dilink.media.service.PlaybackState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onNavigateToBrowser: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiLinkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Album Art
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            AlbumArtDisplay(
                bitmap = playbackState.currentTrack?.albumArt,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Track info
        TrackInfo(playbackState = playbackState)

        Spacer(modifier = Modifier.height(16.dp))

        // Seek bar
        SeekBar(
            positionMs = playbackState.positionMs,
            durationMs = playbackState.durationMs,
            onSeek = onSeek
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main controls
        PlaybackControls(
            isPlaying = playbackState.isPlaying,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary controls
        SecondaryControls(
            shuffleEnabled = playbackState.shuffleEnabled,
            repeatMode = playbackState.repeatMode,
            onToggleShuffle = onToggleShuffle,
            onCycleRepeat = onCycleRepeat,
            onNavigateToQueue = onNavigateToQueue
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Volume bar
        VolumeBar()

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom navigation
        BottomNavButtons(
            onNavigateToBrowser = onNavigateToBrowser,
            onNavigateToEqualizer = onNavigateToEqualizer,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}

@Composable
private fun NowPlayingLandscape(
    playbackState: com.byd.dilink.media.service.PlaybackState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onNavigateToBrowser: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(DiLinkBackground)
            .padding(24.dp)
    ) {
        // Left: Album art
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.4f)
                .padding(end = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AlbumArtDisplay(
                bitmap = playbackState.currentTrack?.albumArt,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }

        // Right: Controls
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.6f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrackInfo(playbackState = playbackState)
            Spacer(modifier = Modifier.height(12.dp))
            SeekBar(
                positionMs = playbackState.positionMs,
                durationMs = playbackState.durationMs,
                onSeek = onSeek
            )
            Spacer(modifier = Modifier.height(12.dp))
            PlaybackControls(
                isPlaying = playbackState.isPlaying,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious
            )
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryControls(
                shuffleEnabled = playbackState.shuffleEnabled,
                repeatMode = playbackState.repeatMode,
                onToggleShuffle = onToggleShuffle,
                onCycleRepeat = onCycleRepeat,
                onNavigateToQueue = onNavigateToQueue
            )
            Spacer(modifier = Modifier.height(8.dp))
            VolumeBar()
            Spacer(modifier = Modifier.height(8.dp))
            BottomNavButtons(
                onNavigateToBrowser = onNavigateToBrowser,
                onNavigateToEqualizer = onNavigateToEqualizer,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun AlbumArtDisplay(
    bitmap: android.graphics.Bitmap?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = DiLinkSurfaceVariant
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Album Art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "No Album Art",
                    modifier = Modifier.size(80.dp),
                    tint = MediaPurple.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun TrackInfo(playbackState: com.byd.dilink.media.service.PlaybackState) {
    val track = playbackState.currentTrack
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = track?.displayTitle ?: "No Track",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = DiLinkTextPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = track?.displayArtist ?: "",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp,
                color = DiLinkTextSecondary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = track?.displayAlbum ?: "",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                color = DiLinkTextMuted
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit
) {
    var seekPosition by remember(positionMs) {
        mutableFloatStateOf(if (durationMs > 0) positionMs.toFloat() / durationMs else 0f)
    }
    var isSeeking by remember { mutableFloatStateOf(0f) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = seekPosition,
            onValueChange = {
                seekPosition = it
            },
            onValueChangeFinished = {
                onSeek((seekPosition * durationMs).toLong())
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = DiLinkCyan,
                activeTrackColor = DiLinkCyan,
                inactiveTrackColor = DiLinkSurfaceElevated
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(positionMs),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = DiLinkTextMuted
                )
            )
            Text(
                text = formatTime(durationMs),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = DiLinkTextMuted
                )
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(40.dp),
                tint = DiLinkTextPrimary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Surface(
            onClick = onPlayPause,
            shape = CircleShape,
            color = DiLinkCyan,
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(48.dp),
                    tint = DiLinkBackground
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = onNext,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(40.dp),
                tint = DiLinkTextPrimary
            )
        }
    }
}

@Composable
private fun SecondaryControls(
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onNavigateToQueue: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onToggleShuffle,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                modifier = Modifier.size(28.dp),
                tint = if (shuffleEnabled) DiLinkCyan else DiLinkTextMuted
            )
        }

        IconButton(
            onClick = onCycleRepeat,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = when (repeatMode) {
                    RepeatMode.ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                },
                contentDescription = "Repeat",
                modifier = Modifier.size(28.dp),
                tint = when (repeatMode) {
                    RepeatMode.OFF -> DiLinkTextMuted
                    else -> DiLinkCyan
                }
            )
        }

        IconButton(
            onClick = onNavigateToQueue,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "Queue",
                modifier = Modifier.size(28.dp),
                tint = DiLinkTextPrimary
            )
        }
    }
}

@Composable
private fun VolumeBar() {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    var currentVolume by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat())
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Volume",
            modifier = Modifier.size(24.dp),
            tint = DiLinkTextMuted
        )
        Spacer(modifier = Modifier.width(8.dp))
        Slider(
            value = currentVolume / maxVolume,
            onValueChange = { fraction ->
                val newVol = (fraction * maxVolume).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                currentVolume = newVol.toFloat()
            },
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = MediaPurple,
                activeTrackColor = MediaPurple,
                inactiveTrackColor = DiLinkSurfaceElevated
            )
        )
    }
}

@Composable
private fun BottomNavButtons(
    onNavigateToBrowser: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Surface(
            onClick = onNavigateToBrowser,
            shape = RoundedCornerShape(12.dp),
            color = DiLinkSurfaceElevated,
            modifier = Modifier.height(56.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = MediaPurple, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browser", style = MaterialTheme.typography.labelLarge)
            }
        }

        Surface(
            onClick = onNavigateToEqualizer,
            shape = RoundedCornerShape(12.dp),
            color = DiLinkSurfaceElevated,
            modifier = Modifier.height(56.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Equalizer, contentDescription = null, tint = MediaPurple, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Equalizer", style = MaterialTheme.typography.labelLarge)
            }
        }

        IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = DiLinkTextMuted,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 400, heightDp = 800)
@Composable
private fun NowPlayingPortraitPreview() {
    DiLinkTheme {
        NowPlayingPortrait(
            playbackState = com.byd.dilink.media.service.PlaybackState(
                positionMs = 120000,
                durationMs = 300000,
                shuffleEnabled = true,
                repeatMode = RepeatMode.ALL
            ),
            onPlayPause = {},
            onNext = {},
            onPrevious = {},
            onSeek = {},
            onToggleShuffle = {},
            onCycleRepeat = {},
            onNavigateToBrowser = {},
            onNavigateToQueue = {},
            onNavigateToEqualizer = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 800, heightDp = 400)
@Composable
private fun NowPlayingLandscapePreview() {
    DiLinkTheme {
        NowPlayingLandscape(
            playbackState = com.byd.dilink.media.service.PlaybackState(
                positionMs = 60000,
                durationMs = 240000
            ),
            onPlayPause = {},
            onNext = {},
            onPrevious = {},
            onSeek = {},
            onToggleShuffle = {},
            onCycleRepeat = {},
            onNavigateToBrowser = {},
            onNavigateToQueue = {},
            onNavigateToEqualizer = {},
            onNavigateToSettings = {}
        )
    }
}
