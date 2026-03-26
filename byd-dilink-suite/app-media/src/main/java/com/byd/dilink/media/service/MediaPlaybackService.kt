package com.byd.dilink.media.service

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.Surface
import com.byd.dilink.media.data.MediaFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Playback state for the media player. Named MediaPlaybackState to avoid
 * conflict with android.media.session.PlaybackState.
 */
data class MediaPlaybackState(
    val isPlaying: Boolean = false,
    val currentTrack: MediaFile? = null,
    val currentIndex: Int = -1,
    val queue: List<MediaFile> = emptyList(),
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false
)

enum class RepeatMode {
    OFF, ONE, ALL
}

class MediaPlaybackService {

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private val handler = Handler(Looper.getMainLooper())

    private val _playbackState = MutableStateFlow(MediaPlaybackState())
    val playbackState: StateFlow<MediaPlaybackState> = _playbackState.asStateFlow()

    private val _audioSessionId = MutableStateFlow(0)
    val audioSessionId: StateFlow<Int> = _audioSessionId.asStateFlow()

    private var surface: Surface? = null
    private var shuffledIndices: List<Int> = emptyList()

    private val positionUpdater = object : Runnable {
        override fun run() {
            try {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _playbackState.value = _playbackState.value.copy(
                            positionMs = player.currentPosition.toLong()
                        )
                    }
                }
            } catch (_: Exception) {}
            handler.postDelayed(this, 500)
        }
    }

    fun init(audioManager: AudioManager) {
        this.audioManager = audioManager
    }

    fun setQueue(files: List<MediaFile>, startIndex: Int = 0) {
        val state = _playbackState.value
        _playbackState.value = state.copy(
            queue = files,
            currentIndex = if (files.isNotEmpty()) startIndex else -1
        )
        if (state.shuffleEnabled) {
            generateShuffledIndices(files.size, startIndex)
        }
        if (files.isNotEmpty()) {
            playTrackAt(startIndex)
        } else {
            stop()
        }
    }

    fun playTrackAt(index: Int) {
        val state = _playbackState.value
        if (index < 0 || index >= state.queue.size) return

        val track = state.queue[index]
        releasePlayer()

        try {
            mediaPlayer = MediaPlayer().apply {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(
                        if (track.isVideo) AudioAttributes.CONTENT_TYPE_MOVIE
                        else AudioAttributes.CONTENT_TYPE_MUSIC
                    )
                    .build()
                setAudioAttributes(attrs)
                setDataSource(track.path)

                surface?.let { setSurface(it) }

                setOnPreparedListener { player ->
                    _audioSessionId.value = player.audioSessionId
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = true,
                        currentTrack = track,
                        currentIndex = index,
                        positionMs = 0,
                        durationMs = player.duration.toLong()
                    )
                    requestAudioFocus()
                    player.start()
                    handler.post(positionUpdater)
                }

                setOnCompletionListener {
                    onTrackCompleted()
                }

                setOnErrorListener { _, what, extra ->
                    android.util.Log.e("MediaPlayback", "Error: what=$what extra=$extra")
                    // Try next track on error
                    onTrackCompleted()
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaPlayback", "Failed to play: ${track.path}", e)
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
            } else {
                player.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
            }
        }
    }

    fun pause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
            }
        }
    }

    fun resume() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs.toInt())
            _playbackState.value = _playbackState.value.copy(positionMs = positionMs)
        }
    }

    fun skipNext() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        val nextIndex = if (state.shuffleEnabled && shuffledIndices.isNotEmpty()) {
            val currentShufflePos = shuffledIndices.indexOf(state.currentIndex)
            if (currentShufflePos >= 0 && currentShufflePos < shuffledIndices.size - 1) {
                shuffledIndices[currentShufflePos + 1]
            } else if (state.repeatMode == RepeatMode.ALL) {
                generateShuffledIndices(state.queue.size, -1)
                shuffledIndices.firstOrNull() ?: 0
            } else return
        } else {
            val next = state.currentIndex + 1
            if (next >= state.queue.size) {
                if (state.repeatMode == RepeatMode.ALL) 0 else return
            } else next
        }
        playTrackAt(nextIndex)
    }

    fun skipPrevious() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        // If more than 3 seconds in, restart current track
        if (state.positionMs > 3000) {
            seekTo(0)
            return
        }

        val prevIndex = if (state.shuffleEnabled && shuffledIndices.isNotEmpty()) {
            val currentShufflePos = shuffledIndices.indexOf(state.currentIndex)
            if (currentShufflePos > 0) {
                shuffledIndices[currentShufflePos - 1]
            } else state.currentIndex
        } else {
            val prev = state.currentIndex - 1
            if (prev < 0) {
                if (state.repeatMode == RepeatMode.ALL) state.queue.size - 1 else 0
            } else prev
        }
        playTrackAt(prevIndex)
    }

    fun toggleRepeat() {
        val state = _playbackState.value
        val nextMode = when (state.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.value = state.copy(repeatMode = nextMode)
    }

    fun toggleShuffle() {
        val state = _playbackState.value
        val newShuffle = !state.shuffleEnabled
        _playbackState.value = state.copy(shuffleEnabled = newShuffle)
        if (newShuffle) {
            generateShuffledIndices(state.queue.size, state.currentIndex)
        }
    }

    fun removeFromQueue(index: Int) {
        val state = _playbackState.value
        if (index < 0 || index >= state.queue.size) return

        val newQueue = state.queue.toMutableList().apply { removeAt(index) }
        val newIndex = when {
            newQueue.isEmpty() -> -1
            index == state.currentIndex -> {
                // Currently playing track was removed
                if (index < newQueue.size) index else 0
            }
            index < state.currentIndex -> state.currentIndex - 1
            else -> state.currentIndex
        }

        _playbackState.value = state.copy(queue = newQueue, currentIndex = newIndex)

        if (index == state.currentIndex && newQueue.isNotEmpty()) {
            playTrackAt(newIndex)
        } else if (newQueue.isEmpty()) {
            stop()
        }
    }

    fun setSurface(surface: Surface?) {
        this.surface = surface
        mediaPlayer?.setSurface(surface)
    }

    fun getAudioSessionId(): Int {
        return mediaPlayer?.audioSessionId ?: 0
    }

    fun stop() {
        handler.removeCallbacks(positionUpdater)
        releasePlayer()
        abandonAudioFocus()
        _playbackState.value = MediaPlaybackState()
    }

    fun release() {
        stop()
    }

    private fun onTrackCompleted() {
        val state = _playbackState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0)
                resume()
            }
            RepeatMode.ALL -> skipNext()
            RepeatMode.OFF -> {
                if (state.currentIndex < state.queue.size - 1) {
                    skipNext()
                } else {
                    _playbackState.value = state.copy(isPlaying = false, positionMs = 0)
                }
            }
        }
    }

    private fun releasePlayer() {
        handler.removeCallbacks(positionUpdater)
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    private fun requestAudioFocus() {
        val am = audioManager ?: return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> pause()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        mediaPlayer?.setVolume(0.3f, 0.3f)
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        mediaPlayer?.setVolume(1.0f, 1.0f)
                        resume()
                    }
                }
            }
            .build()

        am.requestAudioFocus(focusRequest!!)
    }

    private fun abandonAudioFocus() {
        focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
    }

    private fun generateShuffledIndices(size: Int, currentIndex: Int) {
        val indices = (0 until size).toMutableList()
        indices.shuffle()
        // Move current to front if valid
        if (currentIndex in indices) {
            indices.remove(currentIndex)
            indices.add(0, currentIndex)
        }
        shuffledIndices = indices
    }
}
