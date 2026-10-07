package com.example.musicplayer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    val bandLevels = remember { mutableStateListOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f) }

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

        Text("Equalizer", color = textColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Set your advanced options", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(32.dp))

        // --- Baris Toggle Oval (Bass, EQ, Guitar, dll) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OvalToggle("Bass", accentColor) { viewModel.setBassBoost(it) }
            OvalToggle("EQ", accentColor) { /* Default aktif */ }
            OvalToggle("Guitar", accentColor) { /* Belum diimplementasikan */ }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OvalToggle("Tempc", accentColor) { /* Belum diimplementasikan */ }
            OvalToggle("Lighting", accentColor) { /* Belum diimplementasikan */ }
            OvalToggle("Preset", accentColor) { /* Belum diimplementasikan */ }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Slider Vertikal ---
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
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        VerticalSlider(
                            value = bandLevels[index],
                            onValueChange = { newValue ->
                                bandLevels[index] = newValue
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

// --- Custom Toggle Switch Oval Vertikal ---
@Composable
fun OvalToggle(
    label: String,
    accentColor: Color,
    onToggle: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(false) }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(45.dp)
                .height(80.dp)
                .background(
                    color = if (isChecked) accentColor.copy(alpha = 0.2f) else Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(25.dp)
                )
                .clickable {
                    isChecked = !isChecked
                    onToggle(isChecked)
                },
            contentAlignment = if (isChecked) Alignment.TopCenter else Alignment.BottomCenter
        ) {
            // Lingkaran indikator
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .size(30.dp)
                    .background(
                        color = if (isChecked) accentColor else Color(0xFF3A3A3A),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (isChecked) Color.White else Color.Gray,
            fontSize = 10.sp,
            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// --- Custom Vertical Slider ---
@Composable
fun VerticalSlider(
    value: Float,
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
                    val delta = -dragAmount / size.height
                    val newValue = (value + delta).coerceIn(0f, 1f)
                    onValueChange(newValue)
                }
            }
    ) {
        val trackWidth = 8.dp.toPx()
        val thumbRadius = 14.dp.toPx()
        val trackHeight = size.height
        val centerX = size.width / 2

        // Track Background
        drawRoundRect(
            color = inactiveColor,
            topLeft = Offset(centerX - trackWidth / 2, 0f),
            size = Size(trackWidth, trackHeight),
            cornerRadius = CornerRadius(trackWidth / 2)
        )

        // Track Aktif (dari thumb ke bawah, berwarna oranye)
        val thumbY = trackHeight * (1 - value)
        drawRoundRect(
            color = activeColor,
            topLeft = Offset(centerX - trackWidth / 2, thumbY),
            size = Size(trackWidth, trackHeight - thumbY),
            cornerRadius = CornerRadius(trackWidth / 2)
        )

        // Thumb (Bulat)
        drawCircle(
            color = activeColor,
            radius = thumbRadius,
            center = Offset(centerX, thumbY)
        )
        drawCircle(
            color = Color.White,
            radius = thumbRadius / 3,
            center = Offset(centerX, thumbY)
        )
    }
}
