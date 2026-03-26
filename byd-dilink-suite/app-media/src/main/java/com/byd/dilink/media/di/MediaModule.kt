package com.byd.dilink.media.di

import android.content.Context
import com.byd.dilink.media.data.MediaScanner
import com.byd.dilink.media.service.MediaPlaybackService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideMediaScanner(@ApplicationContext context: Context): MediaScanner {
        return MediaScanner(context)
    }

    @Provides
    @Singleton
    fun provideMediaPlaybackService(): MediaPlaybackService {
        return MediaPlaybackService()
    }
}
