package com.example.musicplayer.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.musicplayer.MusicViewModel
import kotlin.math.abs

@Composable
fun PlayerScreen(navController: NavController, viewModel: MusicViewModel) {
    val backgroundColor = Color(0xFF0F0F0F)
    val accentColor = Color(0xFFFF5722)
    val textColor = Color.White
    
    // Mengambil state dari ViewModel
    val waveformData by viewModel.waveformData.collectAsState()
    val songTitle by viewModel.currentSongTitle.collectAsState()
    
    // State untuk melacak apakah musik sedang diputar
    var isPlaying by remember { mutableStateOf(viewModel.exoPlayer.isPlaying) }

    // Listener untuk memperbarui state isPlaying saat lagu berubah
    DisposableEffect(viewModel.exoPlayer) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }
        }
        viewModel.exoPlayer.addListener(listener)
        onDispose {
            viewModel.exoPlayer.removeListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Cover Art (Placeholder) ---
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(Color(0xFF1E1E1E), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Cover",
                tint = accentColor,
                modifier = Modifier.size(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Informasi Lagu ---
        Text(
            text = songTitle,
            color = textColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "File Lokal (Offline)",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Visualizer Gelombang Suara ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val data = waveformData
                if (data != null && data.isNotEmpty()) {
                    val barWidth = size.width / data.size
                    for (i in data.indices) {
                        // Konversi byte ke tinggi bar (0-255)
                        val amplitude = abs(data[i].toInt()).coerceIn(0, 255)
                        val height = (amplitude / 255f) * size.height
                        val x = i * barWidth
                        val y = (size.height - height) / 2
                        
                        drawLine(
                            color = if (i < data.size / 2) accentColor else Color.Gray,
                            start = Offset(x, y),
                            end = Offset(x, y + height),
                            strokeWidth = barWidth - 2f
                        )
                    }
                } else {
                    // Garis datar jika tidak ada audio
                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 4f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Progress Bar (Placeholder Durasi) ---
        Slider(
            value = 0.45f,
            onValueChange = { /* Logika seek */ },
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0xFF2A2A2A)
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0:00", color = Color.Gray, fontSize = 12.sp)
            Text("--:--", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Tombol Kontrol Pemutar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol Shuffle
            IconButton(onClick = { /* Logika Shuffle */ }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = textColor
                )
            }
            
            // Tombol Previous
            IconButton(onClick = { viewModel.exoPlayer.seekToPrevious() }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = textColor,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Tombol Play/Pause Utama
            IconButton(
                onClick = {
                    if (isPlaying) {
                        viewModel.exoPlayer.pause()
                    } else {
                        viewModel.exoPlayer.play()
                    }
                },
                modifier = Modifier
                    .size(80.dp)
                    .background(accentColor, CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            // Tombol Next
            IconButton(onClick = { viewModel.exoPlayer.seekToNext() }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = textColor,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Tombol Repeat
            IconButton(onClick = { /* Logika Repeat */ }) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = textColor
                )
            }
        }
    }
}
