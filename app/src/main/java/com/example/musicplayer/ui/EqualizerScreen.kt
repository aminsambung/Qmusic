package com.example.musicplayer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
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

    // State untuk toggle switch
    var bassEnabled by remember { mutableStateOf(false) }
    var virtualizerEnabled by remember { mutableStateOf(false) }
    var loudnessEnabled by remember { mutableStateOf(false) }

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

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle Switches (Bass, Virtualizer, Loudness)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EqualizerToggle(
                label = "Bass",
                isChecked = bassEnabled,
                onCheckedChange = {
                    bassEnabled = it
                    viewModel.setBassBoost(it)
                }
            )
            EqualizerToggle(
                label = "Virtual",
                isChecked = virtualizerEnabled,
                onCheckedChange = {
                    virtualizerEnabled = it
                    viewModel.setVirtualizer(it)
                }
            )
            EqualizerToggle(
                label = "Loudness",
                isChecked = loudnessEnabled,
                onCheckedChange = {
                    loudnessEnabled = it
                    viewModel.setLoudness(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Slider Vertikal
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
                            .height(250.dp),
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
        val thumbRadius = 16.dp.toPx()
        val trackHeight = size.height
        val centerX = size.width / 2

        drawRoundRect(
            color = inactiveColor,
            topLeft = Offset(centerX - trackWidth / 2, 0f),
            size = Size(trackWidth, trackHeight),
            cornerRadius = CornerRadius(trackWidth / 2)
        )

        val thumbY = trackHeight * (1 - value)
        drawRoundRect(
            color = activeColor,
            topLeft = Offset(centerX - trackWidth / 2, thumbY),
            size = Size(trackWidth, trackHeight - thumbY),
            cornerRadius = CornerRadius(trackWidth / 2)
        )

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

@Composable
fun EqualizerToggle(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFF5722),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF2A2A2A),
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier.size(40.dp, 24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.Gray, fontSize = 10.sp)
    }
}
