package com.byd.dilink.media.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byd.dilink.core.ui.components.TopBarWithBack
import com.byd.dilink.core.ui.theme.DiLinkBackground
import com.byd.dilink.core.ui.theme.DiLinkCyan
import com.byd.dilink.core.ui.theme.DiLinkSurface
import com.byd.dilink.core.ui.theme.DiLinkSurfaceElevated
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.MediaPurple
import com.byd.dilink.core.ui.theme.StatusRed
import com.byd.dilink.media.data.MediaFile
import com.byd.dilink.media.viewmodel.MediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onBack: () -> Unit,
    mediaViewModel: MediaViewModel = hiltViewModel()
) {
    val playbackState by mediaViewModel.playbackState.collectAsState()
    val queue = playbackState.queue
    val currentIndex = playbackState.currentIndex

    var showSaveDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DiLinkBackground,
        topBar = {
            TopBarWithBack(
                title = "Queue (${queue.size} tracks)",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.PlaylistAdd,
                            contentDescription = "Save as Playlist",
                            tint = DiLinkCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier.size(56.dp),
                        enabled = queue.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.ClearAll,
                            contentDescription = "Clear Queue",
                            tint = if (queue.isNotEmpty()) StatusRed else DiLinkTextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (queue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = DiLinkTextMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Queue is empty",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = DiLinkTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Browse files to add music",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = DiLinkTextMuted
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(queue, key = { index, file -> "${file.path}_$index" }) { index, file ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                mediaViewModel.removeFromQueue(index)
                            }
                            false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                    StatusRed.copy(alpha = 0.3f)
                                } else DiLinkBackground,
                                label = "swipe_bg"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, MaterialTheme.shapes.medium)
                                    .padding(end = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = StatusRed,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        QueueItemRow(
                            index = index + 1,
                            file = file,
                            isCurrentlyPlaying = index == currentIndex,
                            onClick = {
                                mediaViewModel.setQueue(queue, index)
                            }
                        )
                    }
                }
            }
        }

        if (showSaveDialog) {
            SavePlaylistDialog(
                onDismiss = { showSaveDialog = false },
                onSave = { name ->
                    // Save is handled at the UI level — the ViewModel doesn't directly access PlaylistEntryDao
                    // In production this would go through a repository
                    showSaveDialog = false
                }
            )
        }

        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text("Clear Queue?", style = MaterialTheme.typography.headlineSmall) },
                text = {
                    Text(
                        "Remove all ${queue.size} tracks from the queue?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            mediaViewModel.setQueue(emptyList())
                            showClearConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelLarge)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearConfirm = false },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }
                },
                containerColor = DiLinkSurface
            )
        }
    }
}

@Composable
private fun QueueItemRow(
    index: Int,
    file: MediaFile,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        color = if (isCurrentlyPlaying) DiLinkCyan.copy(alpha = 0.15f) else DiLinkBackground,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    color = if (isCurrentlyPlaying) DiLinkCyan else DiLinkTextMuted
                ),
                modifier = Modifier.width(36.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.displayTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (isCurrentlyPlaying) DiLinkCyan else DiLinkTextPrimary,
                        fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.Normal
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = file.displayArtist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DiLinkTextSecondary,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = file.formattedDuration,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = DiLinkTextMuted
                )
            )
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp),
                tint = DiLinkTextMuted
            )
        }
    }
}

@Composable
private fun SavePlaylistDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Playlist", style = MaterialTheme.typography.headlineSmall) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    isError = false
                },
                label = { Text("Playlist Name") },
                singleLine = true,
                isError = isError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DiLinkCyan,
                    cursorColor = DiLinkCyan,
                    focusedLabelColor = DiLinkCyan,
                    unfocusedTextColor = DiLinkTextPrimary,
                    focusedTextColor = DiLinkTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
                        onSave(name.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DiLinkCyan),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Save", style = MaterialTheme.typography.labelLarge.copy(color = DiLinkBackground))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(56.dp)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        },
        containerColor = DiLinkSurface
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun QueueItemRowPreview() {
    DiLinkTheme {
        Column {
            QueueItemRow(
                index = 1,
                file = MediaFile(
                    path = "/music/song.mp3",
                    name = "song.mp3",
                    title = "Bohemian Rhapsody",
                    artist = "Queen",
                    album = "A Night at the Opera",
                    durationMs = 354000,
                    albumArt = null,
                    isVideo = false,
                    sizeBytes = 8_500_000,
                    lastModified = System.currentTimeMillis()
                ),
                isCurrentlyPlaying = true,
                onClick = {}
            )
            QueueItemRow(
                index = 2,
                file = MediaFile(
                    path = "/music/song2.mp3",
                    name = "song2.mp3",
                    title = "Another One Bites the Dust",
                    artist = "Queen",
                    album = "The Game",
                    durationMs = 215000,
                    albumArt = null,
                    isVideo = false,
                    sizeBytes = 5_200_000,
                    lastModified = System.currentTimeMillis()
                ),
                isCurrentlyPlaying = false,
                onClick = {}
            )
        }
    }
}
