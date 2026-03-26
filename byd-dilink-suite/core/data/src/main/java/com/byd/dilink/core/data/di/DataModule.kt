package com.byd.dilink.core.data.di

import android.content.Context
import androidx.room.Room
import com.byd.dilink.core.data.dao.FavoriteLocationDao
import com.byd.dilink.core.data.dao.MaintenanceDao
import com.byd.dilink.core.data.dao.ParkingDao
import com.byd.dilink.core.data.dao.PlaylistDao
import com.byd.dilink.core.data.dao.VehicleProfileDao
import com.byd.dilink.core.data.db.DiLinkDatabase
import com.byd.dilink.core.data.preferences.DiLinkPreferences
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
    fun provideParkingDao(db: DiLinkDatabase): ParkingDao =
        db.parkingDao()

    @Provides
    fun provideFavoriteLocationDao(db: DiLinkDatabase): FavoriteLocationDao =
        db.favoriteLocationDao()

    @Provides
    fun provideMaintenanceDao(db: DiLinkDatabase): MaintenanceDao =
        db.maintenanceDao()

    @Provides
    fun provideVehicleProfileDao(db: DiLinkDatabase): VehicleProfileDao =
        db.vehicleProfileDao()

    @Provides
    fun providePlaylistDao(db: DiLinkDatabase): PlaylistDao =
        db.playlistDao()

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): DiLinkPreferences =
        DiLinkPreferences(context)
}
