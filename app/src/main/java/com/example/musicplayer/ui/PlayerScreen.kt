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
import kotlin.math.abs

@Composable
fun PlayerScreen(navController: NavController, viewModel: MusicViewModel) {
    val backgroundColor = Color(0xFF0F0F0F)
    val accentColor = Color(0xFFFF5722)
    val textColor = Color.White
    val context = LocalContext.current

    val waveformData by viewModel.waveformData.collectAsState()
    val songTitle by viewModel.currentSongTitle.collectAsState()
    var isPlaying by remember { mutableStateOf(viewModel.exoPlayer.isPlaying) }

    // Minta izin RECORD_AUDIO (wajib untuk Visualizer)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Jika diizinkan, panggil ulang setup visualizer
            // (Di aplikasi nyata, sebaiknya panggil fungsi setup di ViewModel)
        }
    }

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
        }
        viewModel.exoPlayer.addListener(listener)
        onDispose { viewModel.exoPlayer.removeListener(listener) }
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
            Icon(Icons.Default.Headphones, contentDescription = null, tint = accentColor, modifier = Modifier.size(120.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Info Lagu
        Text(songTitle, color = textColor, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
        Text("Local File (Offline)", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Visualizer
        Box(modifier = Modifier.fillMaxWidth().height(70.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val data = waveformData
                if (data != null && data.isNotEmpty()) {
                    // Ambil hanya 60 data pertama agar tidak terlalu padat
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
                    // Garis statis jika tidak ada data
                    drawLine(Color.DarkGray, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 4f)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Progress Bar (Placeholder)
        Slider(
            value = 0.45f, onValueChange = {},
            colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor, inactiveTrackColor = Color(0xFF2A2A2A))
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0:00", color = Color.Gray, fontSize = 12.sp)
            Text("--:--", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Kontrol
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { }) { Icon(Icons.Default.Shuffle, "Shuffle", tint = textColor) }
            IconButton(onClick = { viewModel.exoPlayer.seekToPrevious() }) { Icon(Icons.Default.SkipPrevious, "Prev", tint = textColor, modifier = Modifier.size(40.dp)) }
            IconButton(
                onClick = { if (isPlaying) viewModel.exoPlayer.pause() else viewModel.exoPlayer.play() },
                modifier = Modifier.size(80.dp).background(accentColor, CircleShape)
            ) {
                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", tint = Color.White, modifier = Modifier.size(48.dp))
            }
            IconButton(onClick = { viewModel.exoPlayer.seekToNext() }) { Icon(Icons.Default.SkipNext, "Next", tint = textColor, modifier = Modifier.size(40.dp)) }
            IconButton(onClick = { }) { Icon(Icons.Default.Repeat, "Repeat", tint = textColor) }
        }
    }
}
