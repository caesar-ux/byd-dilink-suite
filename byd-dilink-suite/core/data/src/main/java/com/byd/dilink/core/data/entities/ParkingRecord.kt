package com.byd.dilink.core.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_records")
data class ParkingRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "address")
    val address: String?,

    @ColumnInfo(name = "parked_at")
    val parkedAt: Long,

    @ColumnInfo(name = "cleared_at")
    val clearedAt: Long?,

    @ColumnInfo(name = "timer_duration_min")
    val timerDurationMin: Int?,

    @ColumnInfo(name = "notes")
    val notes: String?
)
