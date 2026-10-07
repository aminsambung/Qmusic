package com.example.musicplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.musicplayer.MusicViewModel

@Composable
fun EqualizerScreen(navController: NavController, viewModel: MusicViewModel) {
    val backgroundColor = Color(0xFF0F0F0F)
    val accentColor = Color(0xFFFF5722)
    val textColor = Color.White

    // State untuk 5 band frekuensi (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz)
    val bandLevels = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f) }
    val bandLabels = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
            }
        }

        Text("Equalizer", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Set your advanced options", color = Color.Gray, fontSize = 12.sp)
        
        Spacer(modifier = Modifier.height(32.dp))

        // Slider untuk setiap band
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bandLabels.forEachIndexed { index, label ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Slider Vertikal (Menggunakan Slider Horizontal yang dirotasi atau custom)
                    // Untuk simplifikasi, kita gunakan Slider horizontal standar
                    Slider(
                        value = bandLevels[index],
                        onValueChange = { newValue ->
                            bandLevels[index] = newValue
                            // Kirim ke ViewModel (konversi ke short)
                            viewModel.setEqualizerBandLevel(index.toShort(), (newValue * 100).toInt().toShort())
                        },
                        valueRange = -15f..15f, // Range typical equalizer
                        modifier = Modifier.width(40.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(label, color = Color.Gray, fontSize = 10.sp)
                }
            }
        }
    }
}
