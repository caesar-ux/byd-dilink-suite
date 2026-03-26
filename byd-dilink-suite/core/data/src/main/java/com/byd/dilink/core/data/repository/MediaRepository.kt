package com.byd.dilink.core.data.repository

import com.byd.dilink.core.data.dao.PlaylistDao
import com.byd.dilink.core.data.entities.PlaylistEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {
    val playlistNames: Flow<List<String>> = playlistDao.getPlaylistNames()

    fun getEntriesForPlaylist(name: String): Flow<List<PlaylistEntry>> {
        return playlistDao.getEntriesForPlaylist(name)
    }

    suspend fun getEntriesForPlaylistOnce(name: String): List<PlaylistEntry> {
        return playlistDao.getEntriesForPlaylistOnce(name)
    }

    suspend fun addToPlaylist(entry: PlaylistEntry): Long {
        return playlistDao.insert(entry)
    }

    suspend fun addAllToPlaylist(entries: List<PlaylistEntry>) {
        playlistDao.insertAll(entries)
    }

    suspend fun updateEntry(entry: PlaylistEntry) {
        playlistDao.update(entry)
    }

    suspend fun removeFromPlaylist(entry: PlaylistEntry) {
        playlistDao.delete(entry)
    }

    suspend fun deletePlaylist(name: String) {
        playlistDao.deletePlaylist(name)
    }

    suspend fun saveQueueAsPlaylist(
        playlistName: String,
        entries: List<PlaylistEntry>
    ) {
        playlistDao.deletePlaylist(playlistName)
        val reordered = entries.mapIndexed { index, entry ->
            entry.copy(playlistName = playlistName, sortOrder = index)
        }
        playlistDao.insertAll(reordered)
    }

    suspend fun getPlaylistSize(name: String): Int {
        return playlistDao.getPlaylistSize(name)
    }
}
