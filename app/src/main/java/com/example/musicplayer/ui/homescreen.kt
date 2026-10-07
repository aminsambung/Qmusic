package com.example.musicplayer.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.musicplayer.MusicViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: MusicViewModel) {
    val backgroundColor = Color(0xFF0F0F0F)
    val textColor = Color.White
    val context = LocalContext.current

    // Launcher untuk memilih file audio dari penyimpanan HP (Offline)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Ambil nama file dari URI
            val fileName = getFileNameFromUri(context, it)
            // Load ke ExoPlayer melalui ViewModel
            viewModel.loadLocalAudio(it, fileName)
            // Pindah ke layar Player
            navController.navigate("player")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "Offline Music Player",
            color = textColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Putar musik dari penyimpanan HP Anda",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Tombol Utama: Pilih Lagu
        Button(
            onClick = {
                // Membuka file picker khusus audio (MP3, WAV, dll)
                filePickerLauncher.launch(arrayOf("audio/*"))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = "Pilih Lagu",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Pilih Lagu dari HP",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Menu Cepat
        Text(
            text = "Menu Cepat",
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Item History (Dummy)
            CategoryItem(
                icon = Icons.Default.History,
                label = "History"
            ) { /* Aksi untuk history */ }

            // Item Favorites (Dummy)
            CategoryItem(
                icon = Icons.Default.Favorite,
                label = "Favorites"
            ) { /* Aksi untuk favorites */ }

            // Item Shuffle (Dummy)
            CategoryItem(
                icon = Icons.Default.Shuffle,
                label = "Shuffle"
            ) { /* Aksi untuk shuffle */ }

            // Item Equalizer (Navigasi ke layar Equalizer)
            CategoryItem(
                icon = Icons.Default.Settings,
                label = "Equalizer"
            ) {
                navController.navigate("equalizer")
            }
        }
    }
}

@Composable
fun CategoryItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Fungsi Helper untuk mendapatkan nama file dari URI penyimpanan
fun getFileNameFromUri(context: Context, uri: Uri): String {
    var fileName = "Lagu Tidak Dikenal"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = it.getString(nameIndex)
            }
        }
    }
    return fileName
}
