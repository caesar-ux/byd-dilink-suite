package com.byd.dilink.extras.core.data.di

import android.content.Context
import androidx.room.Room
import com.byd.dilink.extras.core.data.dao.BatteryRecordDao
import com.byd.dilink.extras.core.data.dao.ChargeDao
import com.byd.dilink.extras.core.data.dao.FuelDao
import com.byd.dilink.extras.core.data.dao.HazardDao
import com.byd.dilink.extras.core.data.dao.OdometerDao
import com.byd.dilink.extras.core.data.dao.TasbeehDao
import com.byd.dilink.extras.core.data.dao.TirePressureDao
import com.byd.dilink.extras.core.data.dao.TireRotationDao
import com.byd.dilink.extras.core.data.db.ExtrasDatabase
import com.byd.dilink.extras.core.data.preferences.ExtrasPreferences
import com.byd.dilink.extras.core.data.repository.FuelCostRepository
import com.byd.dilink.extras.core.data.repository.HazardRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ExtrasDatabase {
        return Room.databaseBuilder(
            context,
            ExtrasDatabase::class.java,
            "dilink_extras.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideHazardDao(db: ExtrasDatabase): HazardDao = db.hazardDao()

    @Provides
    @Singleton
    fun provideFuelDao(db: ExtrasDatabase): FuelDao = db.fuelDao()

    @Provides
    @Singleton
    fun provideChargeDao(db: ExtrasDatabase): ChargeDao = db.chargeDao()

    @Provides
    @Singleton
    fun provideOdometerDao(db: ExtrasDatabase): OdometerDao = db.odometerDao()

    @Provides
    @Singleton
    fun provideTirePressureDao(db: ExtrasDatabase): TirePressureDao = db.tirePressureDao()

    @Provides
    @Singleton
    fun provideBatteryRecordDao(db: ExtrasDatabase): BatteryRecordDao = db.batteryRecordDao()

    @Provides
    @Singleton
    fun provideTireRotationDao(db: ExtrasDatabase): TireRotationDao = db.tireRotationDao()

    @Provides
    @Singleton
    fun provideTasbeehDao(db: ExtrasDatabase): TasbeehDao = db.tasbeehDao()

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): ExtrasPreferences {
        return ExtrasPreferences(context)
    }

    @Provides
    @Singleton
    fun provideHazardRepository(hazardDao: HazardDao): HazardRepository {
        return HazardRepository(hazardDao)
    }

    @Provides
    @Singleton
    fun provideFuelCostRepository(
        fuelDao: FuelDao,
        chargeDao: ChargeDao,
        odometerDao: OdometerDao,
        preferences: ExtrasPreferences
    ): FuelCostRepository {
        return FuelCostRepository(fuelDao, chargeDao, odometerDao, preferences)
    }
}
