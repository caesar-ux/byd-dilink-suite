package com.byd.dilink.media.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.os.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaScanner @Inject constructor(
    private val context: Context
) {

    /**
     * Returns available storage volumes: internal, SD card, USB
     */
    fun getStorageVolumes(): List<FolderItem> {
        val volumes = mutableListOf<FolderItem>()

        // Internal storage
        val internalRoot = Environment.getExternalStorageDirectory()
        if (internalRoot.exists()) {
            volumes.add(
                FolderItem(
                    path = internalRoot.absolutePath,
                    name = "Internal Storage",
                    itemCount = internalRoot.listFiles()?.size ?: 0,
                    isStorageRoot = true,
                    isUsb = false
                )
            )
        }

        // Check for SD card and USB via StorageManager
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        try {
            val storageVolumes = storageManager.storageVolumes
            for (volume in storageVolumes) {
                val desc = volume.getDescription(context) ?: continue
                // Skip internal since we already added it
                if (volume.isPrimary) continue

                val path = try {
                    // Use reflection for path since getPath() is hidden API
                    val getPath = volume.javaClass.getMethod("getPath")
                    getPath.invoke(volume) as? String
                } catch (e: Exception) {
                    null
                }

                if (path != null) {
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        val isUsb = desc.lowercase().contains("usb") ||
                                path.contains("usb", ignoreCase = true)
                        volumes.add(
                            FolderItem(
                                path = file.absolutePath,
                                name = desc,
                                itemCount = file.listFiles()?.size ?: 0,
                                isStorageRoot = true,
                                isUsb = isUsb
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback: check common mount points
            val sdCard = File("/storage/sdcard1")
            if (sdCard.exists() && sdCard.canRead()) {
                volumes.add(
                    FolderItem(
                        path = sdCard.absolutePath,
                        name = "SD Card",
                        itemCount = sdCard.listFiles()?.size ?: 0,
                        isStorageRoot = true,
                        isUsb = false
                    )
                )
            }
            val usb = File("/storage/usbdisk")
            if (usb.exists() && usb.canRead()) {
                volumes.add(
                    FolderItem(
                        path = usb.absolutePath,
                        name = "USB Drive",
                        itemCount = usb.listFiles()?.size ?: 0,
                        isStorageRoot = true,
                        isUsb = true
                    )
                )
            }
        }

        return volumes
    }

    /**
     * List folders and media files in a directory
     */
    suspend fun listDirectory(path: String): List<Any> = withContext(Dispatchers.IO) {
        val dir = File(path)
        if (!dir.exists() || !dir.canRead()) return@withContext emptyList()

        val children = dir.listFiles() ?: return@withContext emptyList()
        val result = mutableListOf<Any>()

        // Folders first
        children.filter { it.isDirectory && !it.isHidden }
            .sortedBy { it.name.lowercase() }
            .forEach { folder ->
                val mediaCount = countMediaFiles(folder)
                if (mediaCount > 0 || folder.listFiles()?.any { it.isDirectory } == true) {
                    result.add(
                        FolderItem(
                            path = folder.absolutePath,
                            name = folder.name,
                            itemCount = mediaCount
                        )
                    )
                }
            }

        // Media files
        children.filter { it.isFile && !it.isHidden && MediaFile.isMediaFile(it.name) }
            .sortedBy { it.name.lowercase() }
            .forEach { file ->
                result.add(scanFile(file))
            }

        result
    }

    /**
     * Scan a single file for metadata
     */
    suspend fun scanFile(file: File): MediaFile = withContext(Dispatchers.IO) {
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var durationMs: Long = 0
        var albumArt: Uri? = null

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)

            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0

            val artBytes = retriever.embeddedPicture
            if (artBytes != null) {
                // We use the file URI as a reference; actual bitmap would need a ContentProvider
                albumArt = Uri.fromFile(file)
            }

            retriever.release()
        } catch (_: Exception) {
            // Fallback: no metadata available
        }

        MediaFile(
            path = file.absolutePath,
            name = file.name,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            albumArt = albumArt,
            isVideo = MediaFile.isVideoFile(file.name),
            sizeBytes = file.length(),
            lastModified = file.lastModified()
        )
    }

    /**
     * Recursively scan a directory for all media files
     */
    suspend fun scanRecursive(path: String): List<MediaFile> = withContext(Dispatchers.IO) {
        val dir = File(path)
        if (!dir.exists() || !dir.canRead()) return@withContext emptyList()

        val results = mutableListOf<MediaFile>()
        scanRecursiveInternal(dir, results)
        results
    }

    private fun scanRecursiveInternal(dir: File, results: MutableList<MediaFile>) {
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (child.isHidden) continue
            if (child.isDirectory) {
                scanRecursiveInternal(child, results)
            } else if (MediaFile.isMediaFile(child.name)) {
                // Use basic metadata for recursive scan (faster)
                results.add(
                    MediaFile(
                        path = child.absolutePath,
                        name = child.name,
                        title = null,
                        artist = null,
                        album = null,
                        durationMs = 0,
                        albumArt = null,
                        isVideo = MediaFile.isVideoFile(child.name),
                        sizeBytes = child.length(),
                        lastModified = child.lastModified()
                    )
                )
            }
        }
    }

    private fun countMediaFiles(dir: File): Int {
        val children = dir.listFiles() ?: return 0
        return children.count { it.isFile && !it.isHidden && MediaFile.isMediaFile(it.name) }
    }
}
