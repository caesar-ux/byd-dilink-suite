package com.byd.dilink.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.byd.dilink.core.data.entities.PlaylistEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PlaylistEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PlaylistEntry>)

    @Update
    suspend fun update(entry: PlaylistEntry)

    @Delete
    suspend fun delete(entry: PlaylistEntry)

    @Query("SELECT DISTINCT playlist_name FROM playlist_entries ORDER BY playlist_name ASC")
    fun getPlaylistNames(): Flow<List<String>>

    @Query("SELECT * FROM playlist_entries WHERE playlist_name = :playlistName ORDER BY sort_order ASC")
    fun getEntriesForPlaylist(playlistName: String): Flow<List<PlaylistEntry>>

    @Query("SELECT * FROM playlist_entries WHERE playlist_name = :playlistName ORDER BY sort_order ASC")
    suspend fun getEntriesForPlaylistOnce(playlistName: String): List<PlaylistEntry>

    @Query("DELETE FROM playlist_entries WHERE playlist_name = :playlistName")
    suspend fun deletePlaylist(playlistName: String)

    @Query("DELETE FROM playlist_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM playlist_entries WHERE playlist_name = :playlistName")
    suspend fun getPlaylistSize(playlistName: String): Int
}
