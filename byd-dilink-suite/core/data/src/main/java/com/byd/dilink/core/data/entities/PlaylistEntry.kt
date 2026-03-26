package com.byd.dilink.core.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_entries")
data class PlaylistEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "playlist_name")
    val playlistName: String,

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "artist")
    val artist: String?,

    @ColumnInfo(name = "album")
    val album: String?,

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int
)
