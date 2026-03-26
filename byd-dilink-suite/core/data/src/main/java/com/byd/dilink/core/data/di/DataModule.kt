package com.byd.dilink.core.data.di

import android.content.Context
import androidx.room.Room
import com.byd.dilink.core.data.dao.MaintenanceCategoryDao
import com.byd.dilink.core.data.dao.PlaylistEntryDao
import com.byd.dilink.core.data.dao.ServiceRecordDao
import com.byd.dilink.core.data.dao.VehicleProfileDao
import com.byd.dilink.core.data.db.DiLinkDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): DiLinkDatabase {
        return Room.databaseBuilder(
            context,
            DiLinkDatabase::class.java,
            "byd_dilink_db"
        ).build()
    }

    @Provides
    fun provideMaintenanceCategoryDao(db: DiLinkDatabase): MaintenanceCategoryDao =
        db.maintenanceCategoryDao()

    @Provides
    fun provideServiceRecordDao(db: DiLinkDatabase): ServiceRecordDao =
        db.serviceRecordDao()

    @Provides
    fun provideVehicleProfileDao(db: DiLinkDatabase): VehicleProfileDao =
        db.vehicleProfileDao()

    @Provides
    fun providePlaylistEntryDao(db: DiLinkDatabase): PlaylistEntryDao =
        db.playlistEntryDao()
}
