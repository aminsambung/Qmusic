package com.example.musicplayer

import android.app.Application
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.media.audiofx.Visualizer
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _currentSongTitle = MutableStateFlow("No Song Selected")
    val currentSongTitle: StateFlow<String> = _currentSongTitle

    // Audio Effects
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var visualizer: Visualizer? = null

    private val _waveformData = MutableStateFlow<ByteArray?>(null)
    val waveformData: StateFlow<ByteArray?> = _waveformData

    init {
        // Listener untuk update judul lagu saat lagu berubah
        exoPlayer.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.mediaMetadata?.title?.let {
                    _currentSongTitle.value = it.toString()
                }
            }
        })
    }

    // Memutar satu lagu
    fun playSong(song: Song) {
        val mediaItem = MediaItem.Builder()
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .build()
            )
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        _currentSongTitle.value = song.title
        setupAudioEffects()
    }

    // Memutar semua lagu dari daftar (playlist)
    fun playAllSongs(songs: List<Song>, startIndex: Int = 0) {
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .build()
                )
                .build()
        }
        exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        setupAudioEffects()
    }

    // Setup semua efek audio (Equalizer, Bass, Virtualizer, Loudness, Visualizer)
    private fun setupAudioEffects() {
        try {
            val sessionId = exoPlayer.audioSessionId
            if (sessionId == 0 || sessionId == android.media.audiofx.AudioEffect.ERROR) return

            // Release efek lama sebelum membuat yang baru
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
            visualizer?.release()

            // 1. Equalizer
            equalizer = Equalizer(0, sessionId).apply { enabled = true }

            // 2. Bass Boost (Default: Mati)
            bassBoost = BassBoost(0, sessionId).apply { enabled = false }

            // 3. Virtualizer (Default: Mati)
            virtualizer = Virtualizer(0, sessionId).apply { enabled = false }

            // 4. Loudness Enhancer (Default: Mati)
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply { enabled = false }

            // 5. Visualizer
            visualizer = Visualizer(sessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        v: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        _waveformData.value = waveform
                    }

                    override fun onFftDataCapture(
                        v: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        // Tidak digunakan
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false)
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Kontrol Equalizer
    fun setEqualizerBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
    }

    // Kontrol Bass Boost (SUDAH DIPERBAIKI)
    fun setBassBoost(enabled: Boolean) {
        bassBoost?.let {
            it.enabled = enabled
            if (enabled) {
                try {
                    // Coba set ke kekuatan maksimum (1000)
                    it.setStrength(1000.toShort())
                } catch (e: Exception) {
                    try {
                        // Jika gagal, coba nilai tengah (500)
                        it.setStrength(500.toShort())
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
            }
        }
    }

    // Kontrol Virtualizer (SUDAH DIPERBAIKI)
    fun setVirtualizer(enabled: Boolean) {
        virtualizer?.let {
            it.enabled = enabled
            if (enabled) {
                try {
                    it.setStrength(1000.toShort())
                } catch (e: Exception) {
                    try {
                        it.setStrength(500.toShort())
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
            }
        }
    }

    // Kontrol Loudness Enhancer
    fun setLoudness(enabled: Boolean) {
        loudnessEnhancer?.let {
            it.enabled = enabled
            if (enabled) {
                it.setTargetGain(500) // +5 dB
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        visualizer?.release()
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        loudnessEnhancer?.release()
        exoPlayer.release()
    }
}
