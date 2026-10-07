package com.example.musicplayer

import android.app.Application
import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()
    
    // State untuk judul lagu
    private val _currentSongTitle = MutableStateFlow("No Song Selected")
    val currentSongTitle: StateFlow<String> = _currentSongTitle

    private var equalizer: Equalizer? = null
    private var visualizer: Visualizer? = null

    private val _waveformData = MutableStateFlow<ByteArray?>(null)
    val waveformData: StateFlow<ByteArray?> = _waveformData

    // Fungsi untuk memuat lagu dari penyimpanan lokal
    fun loadLocalAudio(uri: Uri, fileName: String) {
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        _currentSongTitle.value = fileName

        // Setup Equalizer & Visualizer setelah audio session ID tersedia
        setupEqualizerAndVisualizer()
    }

    private fun setupEqualizerAndVisualizer() {
        try {
            val sessionId = exoPlayer.audioSessionId
            if (sessionId != 0 && sessionId != android.media.audiofx.AudioEffect.ERROR) {
                // Release yang lama jika ada
                equalizer?.release()
                visualizer?.release()

                // Setup Equalizer
                equalizer = Equalizer(0, sessionId).apply { enabled = true }
                
                // Setup Visualizer
                visualizer = Visualizer(sessionId).apply {
                    captureSize = Visualizer.getCaptureSizeRange()[1]
                    setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                            _waveformData.value = waveform
                        }
                        override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {}
                    }, Visualizer.getMaxCaptureRate() / 2, true, false)
                    enabled = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEqualizerBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
    }

    override fun onCleared() {
        super.onCleared()
        visualizer?.release()
        equalizer?.release()
        exoPlayer.release()
    }
}
