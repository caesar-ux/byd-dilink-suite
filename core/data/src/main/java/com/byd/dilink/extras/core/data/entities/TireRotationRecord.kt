package com.byd.dilink.extras.core.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tire_rotation_records")
data class TireRotationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val odometerKm: Int,
    val pattern: String,
    val notes: String?
)
