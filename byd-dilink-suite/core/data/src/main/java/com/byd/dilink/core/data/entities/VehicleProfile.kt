package com.byd.dilink.core.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_profile")
data class VehicleProfile(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "name")
    val name: String = "BYD Destroyer 05",

    @ColumnInfo(name = "year")
    val year: Int = 2025,

    @ColumnInfo(name = "current_odometer_km")
    val currentOdometerKm: Int = 0,

    @ColumnInfo(name = "last_odometer_update")
    val lastOdometerUpdate: Long = 0,

    @ColumnInfo(name = "vin")
    val vin: String? = null,

    @ColumnInfo(name = "license_plate")
    val licensePlate: String? = null,

    @ColumnInfo(name = "purchase_date")
    val purchaseDate: Long? = null
)
