package com.byd.dilink.media.data

import android.net.Uri

data class MediaFile(
    val path: String,
    val name: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val albumArt: Uri? = null,
    val isVideo: Boolean,
    val sizeBytes: Long,
    val lastModified: Long
) {
    val displayTitle: String
        get() = title?.takeIf { it.isNotBlank() } ?: name.substringBeforeLast('.')

    val displayArtist: String
        get() = artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"

    val displayAlbum: String
        get() = album?.takeIf { it.isNotBlank() } ?: "Unknown Album"

    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }

    val formattedSize: String
        get() {
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.1f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }

    val extension: String
        get() = name.substringAfterLast('.', "").lowercase()

    companion object {
        val AUDIO_EXTENSIONS = setOf(
            "mp3", "flac", "wav", "aac", "ogg", "m4a", "wma", "opus", "aiff"
        )
        val VIDEO_EXTENSIONS = setOf(
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "3gp", "ts"
        )
        val ALL_EXTENSIONS = AUDIO_EXTENSIONS + VIDEO_EXTENSIONS

        fun isAudioFile(fileName: String): Boolean {
            val ext = fileName.substringAfterLast('.', "").lowercase()
            return ext in AUDIO_EXTENSIONS
        }

        fun isVideoFile(fileName: String): Boolean {
            val ext = fileName.substringAfterLast('.', "").lowercase()
            return ext in VIDEO_EXTENSIONS
        }

        fun isMediaFile(fileName: String): Boolean {
            val ext = fileName.substringAfterLast('.', "").lowercase()
            return ext in ALL_EXTENSIONS
        }
    }
}

data class FolderItem(
    val path: String,
    val name: String,
    val itemCount: Int = 0,
    val isStorageRoot: Boolean = false,
    val isUsb: Boolean = false
)
