package com.example.musicplayer

import android.app.Application
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.media.audiofx.Visualizer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
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

    fun loadLocalAudio(uri: Uri, fileName: String) {
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        _currentSongTitle.value = fileName
        setupAudioEffects()
    }

    private fun setupAudioEffects() {
        try {
            val sessionId = exoPlayer.audioSessionId
            if (sessionId == 0 || sessionId == android.media.audiofx.AudioEffect.ERROR) return

            // Release yang lama
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
            visualizer?.release()

            // 1. Equalizer
            equalizer = Equalizer(0, sessionId).apply { enabled = true }

            // 2. Bass Boost (Default: Mati)
            bassBoost = BassBoost(0, sessionId).apply { enabled = false }

            // 3. Virtualizer (Efek Suara Luas, Default: Mati)
            virtualizer = Virtualizer(0, sessionId).apply { enabled = false }

            // 4. Loudness Enhancer (Default: Mati)
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply { enabled = false }

            // 5. Visualizer
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEqualizerBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
    }

    // Fungsi kontrol efek suara
    fun setBassBoost(enabled: Boolean) {
        bassBoost?.let {
            it.enabled = enabled
            if (enabled) {
                // Set ke kekuatan maksimum yang didukung hardware
                it.setStrength(it.strengthSupported.max().toShort())
            }
        }
    }

    fun setVirtualizer(enabled: Boolean) {
        virtualizer?.let {
            it.enabled = enabled
            if (enabled) {
                it.setStrength(it.strengthSupported.max().toShort())
            }
        }
    }

    fun setLoudness(enabled: Boolean) {
        loudnessEnhancer?.let {
            it.enabled = enabled
            if (enabled) {
                it.setTargetGain(500) // 500 mB = +5 dB
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
