package com.byd.dilink.core.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_categories")
data class MaintenanceCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon_name")
    val iconName: String,

    @ColumnInfo(name = "interval_km")
    val intervalKm: Int?,

    @ColumnInfo(name = "interval_months")
    val intervalMonths: Int?,

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
)
