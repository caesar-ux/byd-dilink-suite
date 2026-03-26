package com.byd.dilink.dashboard.di

import android.content.Context
import com.byd.dilink.dashboard.data.DashboardPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DashboardModule {

    @Provides
    @Singleton
    fun provideDashboardPreferences(
        @ApplicationContext context: Context
    ): DashboardPreferences {
        return DashboardPreferences(context)
    }
}
