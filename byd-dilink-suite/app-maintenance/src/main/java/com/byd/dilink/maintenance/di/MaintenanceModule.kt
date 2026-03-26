package com.byd.dilink.maintenance.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Maintenance module for Hilt DI.
 * 
 * All DAOs (MaintenanceDao, VehicleProfileDao) are already provided by
 * the shared DataModule in core:data. This module exists for any
 * maintenance-specific bindings that may be needed in the future.
 */
@Module
@InstallIn(SingletonComponent::class)
object MaintenanceModule
