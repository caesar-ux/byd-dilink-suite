package com.byd.dilink.media.viewmodel

import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byd.dilink.media.data.MediaFile
import com.byd.dilink.media.service.MediaPlaybackService
import com.byd.dilink.media.service.MediaPlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val playbackService: MediaPlaybackService
) : ViewModel() {

    val playbackState: StateFlow<MediaPlaybackState> = playbackService.playbackState
    val audioSessionId: StateFlow<Int> = playbackService.audioSessionId

    fun setQueue(files: List<MediaFile>, startIndex: Int = 0) {
        playbackService.setQueue(files, startIndex)
    }

    fun togglePlayPause() {
        playbackService.togglePlayPause()
    }

    fun pause() {
        playbackService.pause()
    }

    fun resume() {
        playbackService.resume()
    }

    fun seekTo(positionMs: Long) {
        playbackService.seekTo(positionMs)
    }

    fun skipNext() {
        playbackService.skipNext()
    }

    fun skipPrevious() {
        playbackService.skipPrevious()
    }

    fun toggleRepeat() {
        playbackService.toggleRepeat()
    }

    fun toggleShuffle() {
        playbackService.toggleShuffle()
    }

    fun removeFromQueue(index: Int) {
        playbackService.removeFromQueue(index)
    }

    fun setSurface(surface: Surface?) {
        playbackService.setSurface(surface)
    }

    fun getAudioSessionId(): Int {
        return playbackService.getAudioSessionId()
    }

    override fun onCleared() {
        super.onCleared()
        // Don't release the service since it's a singleton;
        // other ViewModels may still reference it
    }
}
