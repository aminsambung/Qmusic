package com.example.musicplayer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
    val inactiveColor = Color(0xFF2A2A2A)

    val bandLabels = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")
    // Nilai level 0.0 sampai 1.0 (0.5 = tengah)
    val bandLevels = remember { mutableStateListOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
            }
        }

        Text("Equalizer", color = textColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Set your advanced options", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(40.dp))

        // --- Toggle Switches (Bass, Tempo, dll) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EqualizerToggle("Bass")
            EqualizerToggle("Tempc")
            EqualizerToggle("Lighting")
            EqualizerToggle("Preset")
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- Slider Vertikal (Audio Mixer) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bandLabels.forEachIndexed { index, label ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Custom Vertical Slider
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        VerticalSlider(
                            value = bandLevels[index],
                            onValueChange = { newValue ->
                                bandLevels[index] = newValue
                                // Kirim ke ViewModel (konversi 0.0-1.0 menjadi -15 sampai +15 dB)
                                val dBValue = ((newValue - 0.5f) * 30).toInt().toShort()
                                viewModel.setEqualizerBandLevel(index.toShort(), dBValue)
                            },
                            activeColor = accentColor,
                            inactiveColor = inactiveColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(label, color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.weight(0.2f))
                }
            }
        }
    }
}

// --- Komponen Custom: Vertical Slider ---
@Composable
fun VerticalSlider(
    value: Float, // 0.0f - 1.0f
    onValueChange: (Float) -> Unit,
    activeColor: Color,
    inactiveColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    // Konversi drag ke nilai 0.0-1.0 (nilai dibalik karena Y ke bawah)
                    val delta = -dragAmount / size.height
                    val newValue = (value + delta).coerceIn(0f, 1f)
                    onValueChange(newValue)
                }
            }
    ) {
        val trackWidth = 8.dp.toPx()
        val thumbRadius = 16.dp.toPx()
        val trackHeight = size.height
        val centerX = size.width / 2

        // 1. Gambar Track (Latar Belakang)
        drawRoundRect(
            color = inactiveColor,
            topLeft = Offset(centerX - trackWidth / 2, 0f),
            size = androidx.compose.ui.geometry.Size(trackWidth, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackWidth / 2)
        )

        // 2. Gambar Track Aktif (dari thumb ke bawah)
        val thumbY = trackHeight * (1 - value)
        drawRoundRect(
            color = activeColor,
            topLeft = Offset(centerX - trackWidth / 2, thumbY),
            size = androidx.compose.ui.geometry.Size(trackWidth, trackHeight - thumbY),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackWidth / 2)
        )

        // 3. Gambar Thumb (Tombol Bulat)
        drawCircle(
            color = activeColor,
            radius = thumbRadius,
            center = Offset(centerX, thumbY)
        )
        // Lingkaran putih kecil di tengah thumb
        drawCircle(
            color = Color.White,
            radius = thumbRadius / 3,
            center = Offset(centerX, thumbY)
        )
    }
}

// --- Komponen Custom: Toggle Switch ---
@Composable
fun EqualizerToggle(label: String) {
    var isChecked by remember { mutableStateOf(false) }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Switch(
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFF5722),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF2A2A2A),
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier.size(40.dp, 24.dp) // Ukuran switch lebih kecil
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.Gray, fontSize = 9.sp)
    }
}
