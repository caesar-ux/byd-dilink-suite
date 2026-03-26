package com.byd.dilink.extras.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.byd.dilink.extras.core.data.dao.BatteryRecordDao
import com.byd.dilink.extras.core.data.dao.ChargeDao
import com.byd.dilink.extras.core.data.dao.FuelDao
import com.byd.dilink.extras.core.data.dao.HazardDao
import com.byd.dilink.extras.core.data.dao.OdometerDao
import com.byd.dilink.extras.core.data.dao.TasbeehDao
import com.byd.dilink.extras.core.data.dao.TirePressureDao
import com.byd.dilink.extras.core.data.dao.TireRotationDao
import com.byd.dilink.extras.core.data.entities.BatteryRecord
import com.byd.dilink.extras.core.data.entities.ChargeRecord
import com.byd.dilink.extras.core.data.entities.FuelRecord
import com.byd.dilink.extras.core.data.entities.HazardRecord
import com.byd.dilink.extras.core.data.entities.OdometerEntry
import com.byd.dilink.extras.core.data.entities.TasbeehSession
import com.byd.dilink.extras.core.data.entities.TirePressureRecord
import com.byd.dilink.extras.core.data.entities.TireRotationRecord

@Database(
    entities = [
        HazardRecord::class,
        FuelRecord::class,
        ChargeRecord::class,
        OdometerEntry::class,
        TirePressureRecord::class,
        BatteryRecord::class,
        TireRotationRecord::class,
        TasbeehSession::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ExtrasDatabase : RoomDatabase() {
    abstract fun hazardDao(): HazardDao
    abstract fun fuelDao(): FuelDao
    abstract fun chargeDao(): ChargeDao
    abstract fun odometerDao(): OdometerDao
    abstract fun tirePressureDao(): TirePressureDao
    abstract fun batteryRecordDao(): BatteryRecordDao
    abstract fun tireRotationDao(): TireRotationDao
    abstract fun tasbeehDao(): TasbeehDao
}
