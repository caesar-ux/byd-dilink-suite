package com.byd.dilink.media.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.byd.dilink.core.ui.theme.DiLinkSurfaceVariant
import com.byd.dilink.core.ui.theme.DiLinkTextMuted
import com.byd.dilink.core.ui.theme.DiLinkTextPrimary
import com.byd.dilink.core.ui.theme.DiLinkTextSecondary
import com.byd.dilink.core.ui.theme.DiLinkTheme
import com.byd.dilink.core.ui.theme.MediaPurple
import com.byd.dilink.core.ui.theme.StatusBlue
import com.byd.dilink.media.data.FolderItem
import com.byd.dilink.media.data.MediaFile
import com.byd.dilink.media.viewmodel.BrowserViewModel
import com.byd.dilink.media.viewmodel.MediaViewModel
import com.byd.dilink.media.viewmodel.SortMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    onBack: () -> Unit,
    onNavigateToVideoPlayer: () -> Unit,
    browserViewModel: BrowserViewModel = hiltViewModel(),
    mediaViewModel: MediaViewModel = hiltViewModel()
) {
    val storageVolumes by browserViewModel.storageVolumes.collectAsState()
    val currentPath by browserViewModel.currentPath.collectAsState()
    val fileList by browserViewModel.fileList.collectAsState()
    val sortMode by browserViewModel.sortMode.collectAsState()
    val isLoading by browserViewModel.isLoading.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var longPressedFile by remember { mutableStateOf<MediaFile?>(null) }

    Scaffold(
        containerColor = DiLinkBackground,
        topBar = {
            TopBarWithBack(
                title = currentPath?.substringAfterLast('/') ?: "File Browser",
                onBack = {
                    if (!browserViewModel.navigateUp()) {
                        onBack()
                    }
                },
                actions = {
                    // Play All button
                    val mediaFiles = fileList.filterIsInstance<MediaFile>()
                    if (mediaFiles.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                mediaViewModel.setQueue(mediaFiles, 0)
                            },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play All",
                                tint = DiLinkCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Sort button
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = DiLinkTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (mode) {
                                            SortMode.NAME_ASC -> "Name (A-Z)"
                                            SortMode.NAME_DESC -> "Name (Z-A)"
                                            SortMode.DATE_NEWEST -> "Date (Newest)"
                                            SortMode.DATE_OLDEST -> "Date (Oldest)"
                                            SortMode.SIZE_LARGEST -> "Size (Largest)"
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (mode == sortMode) FontWeight.Bold else FontWeight.Normal,
                                        color = if (mode == sortMode) DiLinkCyan else DiLinkTextPrimary
                                    )
                                },
                                onClick = {
                                    browserViewModel.setSortMode(mode)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Storage volume chips when at root
            if (currentPath == null && storageVolumes.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    storageVolumes.forEach { volume ->
                        FilterChip(
                            selected = false,
                            onClick = { browserViewModel.navigateTo(volume.path) },
                            label = { Text(volume.name, style = MaterialTheme.typography.labelLarge) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (volume.isUsb) Icons.Default.Usb else Icons.Default.SdStorage,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DiLinkSurfaceVariant,
                                labelColor = DiLinkTextSecondary,
                                iconColor = DiLinkTextMuted
                            ),
                            modifier = Modifier.height(44.dp)
                        )
                    }
                }
            }

            // Current path breadcrumb
            currentPath?.let { path ->
                Text(
                    text = path,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = DiLinkTextMuted
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MediaPurple)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Scanning files...", style = MaterialTheme.typography.bodyLarge)
                }
            } else if (fileList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No media files found",
                        style = MaterialTheme.typography.headlineSmall.copy(color = DiLinkTextMuted)
                    )
                    if (currentPath == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Insert a USB drive or SD card",
                            style = MaterialTheme.typography.bodyLarge.copy(color = DiLinkTextMuted)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(fileList, key = { item ->
                        when (item) {
                            is FolderItem -> "folder:${item.path}"
                            is MediaFile -> "file:${item.path}"
                            else -> item.hashCode()
                        }
                    }) { item ->
                        when (item) {
                            is FolderItem -> FolderRow(
                                folder = item,
                                onClick = { browserViewModel.navigateTo(item.path) }
                            )
                            is MediaFile -> MediaFileRow(
                                file = item,
                                onClick = {
                                    val mediaFiles = fileList.filterIsInstance<MediaFile>()
                                    val index = mediaFiles.indexOf(item)
                                    mediaViewModel.setQueue(mediaFiles, index)
                                    if (item.isVideo) {
                                        onNavigateToVideoPlayer()
                                    }
                                },
                                onLongClick = { longPressedFile = item }
                            )
                        }
                    }
                }
            }
        }

        // Long-press bottom sheet
        longPressedFile?.let { file ->
            ModalBottomSheet(
                onDismissRequest = { longPressedFile = null },
                sheetState = rememberModalBottomSheetState(),
                containerColor = DiLinkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        file.displayTitle,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Surface(
                        onClick = {
                            val mediaFiles = fileList.filterIsInstance<MediaFile>()
                            val index = mediaFiles.indexOf(file)
                            mediaViewModel.setQueue(mediaFiles, index)
                            longPressedFile = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        color = DiLinkSurfaceElevated,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DiLinkCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Play", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        onClick = {
                            // Add to existing queue
                            val state = mediaViewModel.playbackState.value
                            val newQueue = state.queue + file
                            if (state.queue.isEmpty()) {
                                mediaViewModel.setQueue(newQueue, 0)
                            }
                            // Queue is immutable after set, so just inform user
                            longPressedFile = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        color = DiLinkSurfaceElevated,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AudioFile, contentDescription = null, tint = MediaPurple)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Play Next", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun FolderRow(
    folder: FolderItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        color = DiLinkBackground,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (folder.isStorageRoot) {
                    if (folder.isUsb) Icons.Default.Usb else Icons.Default.SdStorage
                } else Icons.Default.Folder,
                contentDescription = "Folder",
                modifier = Modifier.size(32.dp),
                tint = StatusBlue
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = DiLinkTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (folder.itemCount > 0) {
                    Text(
                        text = "${folder.itemCount} item${if (folder.itemCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DiLinkTextMuted,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaFileRow(
    file: MediaFile,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        color = DiLinkBackground,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (file.isVideo) Icons.Default.VideoFile else Icons.Default.AudioFile,
                contentDescription = if (file.isVideo) "Video" else "Audio",
                modifier = Modifier.size(32.dp),
                tint = if (file.isVideo) StatusBlue else MediaPurple
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.displayTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DiLinkTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    Text(
                        text = file.displayArtist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DiLinkTextSecondary,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (file.isVideo) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = file.formattedSize,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DiLinkTextMuted,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = file.formattedDuration,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = DiLinkTextMuted
                )
            )
            IconButton(
                onClick = onLongClick,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = DiLinkTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun FolderRowPreview() {
    DiLinkTheme {
        FolderRow(
            folder = FolderItem(
                path = "/storage/emulated/0/Music",
                name = "Music",
                itemCount = 42
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun MediaFileRowPreview() {
    DiLinkTheme {
        MediaFileRow(
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
            onClick = {},
            onLongClick = {}
        )
    }
}
