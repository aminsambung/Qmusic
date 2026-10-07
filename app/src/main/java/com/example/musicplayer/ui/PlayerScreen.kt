package com.example.musicplayer.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.musicplayer.MusicViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun PlayerScreen(navController: NavController, viewModel: MusicViewModel) {
    val backgroundColor = Color(0xFF0F0F0F)
    val accentColor = Color(0xFFFF5722) // Oranye untuk visualizer saja
    val textColor = Color.White
    val context = LocalContext.current

    val waveformData by viewModel.waveformData.collectAsState()
    val songTitle by viewModel.currentSongTitle.collectAsState()

    var currentPosition by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }
    var isPlaying by remember { mutableStateOf(viewModel.exoPlayer.isPlaying) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(viewModel.exoPlayer) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == androidx.media3.common.Player.STATE_READY) {
                    totalDuration = viewModel.exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        }
        viewModel.exoPlayer.addListener(listener)
        onDispose { viewModel.exoPlayer.removeListener(listener) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = viewModel.exoPlayer.currentPosition
            if (totalDuration == 0L) {
                totalDuration = viewModel.exoPlayer.duration.coerceAtLeast(0L)
            }
            delay(500)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cover Art
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(Color(0xFF1E1E1E), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Headphones, contentDescription = null, tint = textColor, modifier = Modifier.size(120.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Info Lagu
        Text(songTitle, color = textColor, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
        Text("Local File (Offline)", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Visualizer (tetap oranye)
        Box(modifier = Modifier.fillMaxWidth().height(70.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val data = waveformData
                if (data != null && data.isNotEmpty()) {
                    val displayData = data.take(60)
                    val barWidth = size.width / displayData.size
                    for (i in displayData.indices) {
                        val amplitude = abs(displayData[i].toInt()).coerceIn(0, 255)
                        val height = (amplitude / 255f) * size.height
                        val x = i * barWidth
                        val y = (size.height - height) / 2
                        drawLine(
                            color = if (i < displayData.size / 3) accentColor else Color.Gray,
                            start = Offset(x, y),
                            end = Offset(x, y + height),
                            strokeWidth = barWidth - 3f
                        )
                    }
                } else {
                    drawLine(Color.DarkGray, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 4f)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Slider Progress Bar
        Slider(
            value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat() else 0f,
            onValueChange = { newValue ->
                val newPosition = (newValue * totalDuration).toLong()
                viewModel.exoPlayer.seekTo(newPosition)
                currentPosition = newPosition
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,        // Thumb putih
                activeTrackColor = Color.White,  // Track aktif putih
                inactiveTrackColor = Color(0xFF2A2A2A)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(currentPosition), color = Color.Gray, fontSize = 12.sp)
            Text(formatTime(totalDuration), color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Kontrol Pemutar - SEMUA PUTIH
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                viewModel.exoPlayer.shuffleModeEnabled = !viewModel.exoPlayer.shuffleModeEnabled
            }) {
                Icon(Icons.Default.Shuffle, "Shuffle", tint = Color.White)
            }
            IconButton(onClick = { viewModel.exoPlayer.seekToPrevious() }) {
                Icon(Icons.Default.SkipPrevious, "Prev", tint = Color.White, modifier = Modifier.size(40.dp))
            }
            
            // Tombol Play/Pause Utama: PUTIH dengan ikon HITAM
            IconButton(
                onClick = { if (isPlaying) viewModel.exoPlayer.pause() else viewModel.exoPlayer.play() },
                modifier = Modifier.size(80.dp).background(Color.White, CircleShape)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    "Play/Pause", tint = Color.Black, modifier = Modifier.size(48.dp)
                )
            }
            
            IconButton(onClick = { viewModel.exoPlayer.seekToNext() }) {
                Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(40.dp))
            }
            IconButton(onClick = {
                viewModel.exoPlayer.repeatMode = when (viewModel.exoPlayer.repeatMode) {
                    androidx.media3.common.Player.REPEAT_MODE_OFF -> androidx.media3.common.Player.REPEAT_MODE_ALL
                    androidx.media3.common.Player.REPEAT_MODE_ALL -> androidx.media3.common.Player.REPEAT_MODE_ONE
                    else -> androidx.media3.common.Player.REPEAT_MODE_OFF
                }
            }) {
                Icon(Icons.Default.Repeat, "Repeat", tint = Color.White)
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
