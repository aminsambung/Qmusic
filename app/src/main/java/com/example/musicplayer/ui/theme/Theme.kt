package com.example.musicplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Skema Warna Gelap sesuai desain (True Black + Oranye)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF5722),      // Oranye sebagai aksen
    onPrimary = Color.White,
    secondary = Color(0xFFFF5722),
    onSecondary = Color.White,
    background = Color(0xFF0F0F0F),   // Latar belakang hitam
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),      // Kartu abu gelap
    onSurface = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun MusicPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Kita selalu pakai dark theme karena desainnya dark
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
