package com.byd.dilink.media.ui

import android.content.res.Configuration
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
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOneOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurface
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
    onNavigateToFileBrowser: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToVideoPlayer: () -> Unit,
    mediaViewModel: MediaViewModel = hiltViewModel()
) {
    val playbackState by mediaViewModel.playbackState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = DiLinkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DiLinkSurface,
                contentColor = DiLinkTextPrimary
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = "Now Playing",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("Playing", style = MaterialTheme.typography.labelMedium) },
                    colors = navBarItemColors()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToFileBrowser,
                    icon = {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = "Browse",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("Browse", style = MaterialTheme.typography.labelMedium) },
                    colors = navBarItemColors()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToQueue,
                    icon = {
                        Icon(
                            Icons.Default.QueueMusic,
                            contentDescription = "Queue",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("Queue", style = MaterialTheme.typography.labelMedium) },
                    colors = navBarItemColors()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToEqualizer,
                    icon = {
                        Icon(
                            Icons.Default.Equalizer,
                            contentDescription = "EQ",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("EQ", style = MaterialTheme.typography.labelMedium) },
                    colors = navBarItemColors()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSettings,
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("Settings", style = MaterialTheme.typography.labelMedium) },
                    colors = navBarItemColors()
                )
            }
        }
    ) { paddingValues ->
        if (isLandscape) {
            // Landscape: art on left, controls on right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Album art
                AlbumArtPlaceholder(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )

                Spacer(modifier = Modifier.width(24.dp))

                // Controls column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    TrackInfo(
                        title = playbackState.currentTrack?.displayTitle ?: "No Track Playing",
                        artist = playbackState.currentTrack?.displayArtist ?: "Browse files to add music",
                        album = playbackState.currentTrack?.displayAlbum ?: ""
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SeekBar(
                        positionMs = playbackState.positionMs,
                        durationMs = playbackState.durationMs,
                        onSeek = { mediaViewModel.seekTo(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PlaybackControls(
                        isPlaying = playbackState.isPlaying,
                        repeatMode = playbackState.repeatMode,
                        shuffleEnabled = playbackState.shuffleEnabled,
                        isVideo = playbackState.currentTrack?.isVideo == true,
                        onPlayPause = { mediaViewModel.togglePlayPause() },
                        onSkipNext = { mediaViewModel.skipNext() },
                        onSkipPrevious = { mediaViewModel.skipPrevious() },
                        onToggleRepeat = { mediaViewModel.toggleRepeat() },
                        onToggleShuffle = { mediaViewModel.toggleShuffle() },
                        onVideoFullscreen = onNavigateToVideoPlayer
                    )
                }
            }
        } else {
            // Portrait layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(0.5f))

                // Album art
                AlbumArtPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                TrackInfo(
                    title = playbackState.currentTrack?.displayTitle ?: "No Track Playing",
                    artist = playbackState.currentTrack?.displayArtist ?: "Browse files to add music",
                    album = playbackState.currentTrack?.displayAlbum ?: ""
                )

                Spacer(modifier = Modifier.height(24.dp))

                SeekBar(
                    positionMs = playbackState.positionMs,
                    durationMs = playbackState.durationMs,
                    onSeek = { mediaViewModel.seekTo(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PlaybackControls(
                    isPlaying = playbackState.isPlaying,
                    repeatMode = playbackState.repeatMode,
                    shuffleEnabled = playbackState.shuffleEnabled,
                    isVideo = playbackState.currentTrack?.isVideo == true,
                    onPlayPause = { mediaViewModel.togglePlayPause() },
                    onSkipNext = { mediaViewModel.skipNext() },
                    onSkipPrevious = { mediaViewModel.skipPrevious() },
                    onToggleRepeat = { mediaViewModel.toggleRepeat() },
                    onToggleShuffle = { mediaViewModel.toggleShuffle() },
                    onVideoFullscreen = onNavigateToVideoPlayer
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun navBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = DiLinkCyan,
    selectedTextColor = DiLinkCyan,
    unselectedIconColor = DiLinkTextMuted,
    unselectedTextColor = DiLinkTextMuted,
    indicatorColor = DiLinkSurfaceElevated
)

@Composable
private fun AlbumArtPlaceholder(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = DiLinkSurfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = DiLinkTextMuted
            )
        }
    }
}

@Composable
private fun TrackInfo(
    title: String,
    artist: String,
    album: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = artist,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DiLinkTextSecondary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        if (album.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = album,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DiLinkTextMuted,
                    fontSize = 14.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit
) {
    var seekPosition by remember(positionMs, durationMs) {
        mutableFloatStateOf(
            if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = seekPosition,
            onValueChange = { seekPosition = it },
            onValueChangeFinished = {
                onSeek((seekPosition * durationMs).toLong())
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MediaPurple,
                activeTrackColor = MediaPurple,
                inactiveTrackColor = DiLinkSurfaceVariant
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(positionMs),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = DiLinkTextMuted
                )
            )
            Text(
                text = formatTime(durationMs),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = DiLinkTextMuted
                )
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    repeatMode: RepeatMode,
    shuffleEnabled: Boolean,
    isVideo: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onVideoFullscreen: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle
        IconButton(
            onClick = onToggleShuffle,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = if (shuffleEnabled) Icons.Default.ShuffleOn else Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffleEnabled) MediaPurple else DiLinkTextMuted,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Previous
        IconButton(
            onClick = onSkipPrevious,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = DiLinkTextPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Play/Pause
        Surface(
            onClick = onPlayPause,
            shape = CircleShape,
            color = MediaPurple,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = DiLinkBackground,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Next
        IconButton(
            onClick = onSkipNext,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = DiLinkTextPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Repeat / Video fullscreen
        if (isVideo) {
            IconButton(
                onClick = onVideoFullscreen,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = "Video Fullscreen",
                    tint = DiLinkCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            IconButton(
                onClick = onToggleRepeat,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = when (repeatMode) {
                        RepeatMode.OFF -> Icons.Default.Repeat
                        RepeatMode.ALL -> Icons.Default.RepeatOn
                        RepeatMode.ONE -> Icons.Default.RepeatOneOn
                    },
                    contentDescription = "Repeat: ${repeatMode.name}",
                    tint = if (repeatMode != RepeatMode.OFF) MediaPurple else DiLinkTextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun NowPlayingPreviewPortrait() {
    DiLinkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AlbumArtPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            TrackInfo(
                title = "Bohemian Rhapsody",
                artist = "Queen",
                album = "A Night at the Opera"
            )
            Spacer(modifier = Modifier.height(16.dp))
            SeekBar(positionMs = 120000, durationMs = 354000, onSeek = {})
            Spacer(modifier = Modifier.height(8.dp))
            PlaybackControls(
                isPlaying = true,
                repeatMode = RepeatMode.ALL,
                shuffleEnabled = false,
                isVideo = false,
                onPlayPause = {},
                onSkipNext = {},
                onSkipPrevious = {},
                onToggleRepeat = {},
                onToggleShuffle = {},
                onVideoFullscreen = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 800, heightDp = 400)
@Composable
private fun NowPlayingPreviewLandscape() {
    DiLinkTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            AlbumArtPlaceholder(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                TrackInfo(
                    title = "Bohemian Rhapsody",
                    artist = "Queen",
                    album = "A Night at the Opera"
                )
                Spacer(modifier = Modifier.height(16.dp))
                SeekBar(positionMs = 120000, durationMs = 354000, onSeek = {})
                Spacer(modifier = Modifier.height(8.dp))
                PlaybackControls(
                    isPlaying = false,
                    repeatMode = RepeatMode.OFF,
                    shuffleEnabled = true,
                    isVideo = false,
                    onPlayPause = {},
                    onSkipNext = {},
                    onSkipPrevious = {},
                    onToggleRepeat = {},
                    onToggleShuffle = {},
                    onVideoFullscreen = {}
                )
            }
        }
    }
}
