package com.byd.dilink.core.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_records")
data class ServiceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "date_performed")
    val datePerformed: Long,

    @ColumnInfo(name = "odometer_km")
    val odometerKm: Int,

    @ColumnInfo(name = "cost")
    val cost: Double?,

    @ColumnInfo(name = "shop_name")
    val shopName: String?,

    @ColumnInfo(name = "notes")
    val notes: String?
)
