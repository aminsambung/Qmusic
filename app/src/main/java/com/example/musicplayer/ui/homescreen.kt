package com.example.musicplayer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.musicplayer.MusicViewModel
import com.example.musicplayer.Song
import com.example.musicplayer.formatDuration
import com.example.musicplayer.getAllSongs

@Composable
fun HomeScreen(navController: NavController, viewModel: MusicViewModel) {
    val backgroundColor = Color(0xFF0F0F0F)
    val textColor = Color.White
    val accentColor = Color(0xFFFF5722)
    val context = LocalContext.current

    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }

    val currentTitle by viewModel.currentSongTitle.collectAsState()
    val isPlaying = viewModel.exoPlayer.isPlaying

    // Fungsi memuat lagu
    fun loadSongs() {
        songs = getAllSongs(context)
        hasPermission = true
    }

    // Launcher izin
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) loadSongs()
    }

    // Minta izin saat pertama dibuka
    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            loadSongs()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // --- Header ---
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pustaka Musik",
                color = textColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${songs.size} lagu ditemukan",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        // --- Tombol Acak Semua ---
        if (songs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.playAllSongs(songs.shuffled(), 0)
                        navController.navigate("player")
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "ACAK SEMUA",
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Divider(color = Color(0xFF1E1E1E), thickness = 1.dp)
        }

        // --- Daftar Lagu ---
        if (!hasPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Izin akses musik diperlukan.\nSilakan izinkan di pengaturan.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else if (songs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Tidak ada lagu ditemukan.\nPastikan ada file MP3 di HP Anda.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp) // Ruang untuk mini player
            ) {
                items(songs) { song ->
                    SongRow(song) {
                        viewModel.playAllSongs(songs, songs.indexOf(song))
                        navController.navigate("player")
                    }
                }
            }
        }

        // --- Mini Player (Muncul jika ada lagu yang sedang diputar) ---
        if (currentTitle != "No Song Selected") {
            MiniPlayer(
                title = currentTitle,
                isPlaying = isPlaying,
                onPlayPauseClick = {
                    if (isPlaying) viewModel.exoPlayer.pause() else viewModel.exoPlayer.play()
                },
                onNextClick = { viewModel.exoPlayer.seekToNext() },
                onClick = { navController.navigate("player") }
            )
        }
    }
}

@Composable
fun SongRow(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Gray)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${song.artist} • ${formatDuration(song.duration)}",
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Default.MoreVert,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun MiniPlayer(
    title: String,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF1E1E1E),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF5722)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Sedang diputar...", color = Color.Gray, fontSize = 11.sp)
            }
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White
                )
            }
            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
            }
        }
    }
}
