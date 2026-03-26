package com.byd.dilink.parking.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ParkingModule {
    // ParkingRepository is already provided via @Inject constructor
    // Additional parking-specific dependencies can be added here
}
