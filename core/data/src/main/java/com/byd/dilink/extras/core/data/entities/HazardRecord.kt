package com.byd.dilink.extras.core.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hazards")
data class HazardRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val heading: Float,
    val speed: Float,
    val timestamp: Long,
    val notes: String?,
    val confirmed: Int = 1
)
