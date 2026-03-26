package com.byd.dilink.extras.core.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "odometer_entries")
data class OdometerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val odometerKm: Int
)
