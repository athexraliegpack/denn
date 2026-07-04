package com.example.game

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private var bgPlayer: MediaPlayer? = null
    private var gameStartedState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var gameStarted by remember { mutableStateOf(false) }
            gameStartedState = gameStarted
            val context = LocalContext.current
            val sharedPref = remember { context.getSharedPreferences("MergeGameSave", Context.MODE_PRIVATE) }

            if (!gameStarted) {
                stopBackgroundMusic()
                MainMenu(
                    onStartGame = { gameStarted = true },
                    onResetGame = {
                        sharedPref.edit().clear().apply()
                        recreate()
                    }
                )
            } else {
                LaunchedEffect(Unit) { startBackgroundMusic(context) }
                MergeGameScreen(onBackToMenu = { gameStarted = false })
            }
        }
    }

    private fun startBackgroundMusic(context: Context) {
        if (bgPlayer == null) {
            try {
                bgPlayer = MediaPlayer.create(context, R.raw.bg_music)
                bgPlayer?.isLooping = true
                bgPlayer?.setVolume(0.3f, 0.3f)
                bgPlayer?.start()
            } catch (e: Exception) { e.printStackTrace() }
        } else if (bgPlayer?.isPlaying == false) {
            bgPlayer?.start()
        }
    }

    private fun stopBackgroundMusic() {
        bgPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
            bgPlayer = null
        }
    }

    override fun onPause() {
        super.onPause()
        if (bgPlayer?.isPlaying == true) bgPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (gameStartedState && bgPlayer != null && bgPlayer?.isPlaying == false) {
            bgPlayer?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBackgroundMusic()
    }
}

// --- ANA MENÜ BURADA (Class dışında tanımlıyoruz ki MainActivity görebilsin) ---
@Composable
fun MainMenu(onStartGame: () -> Unit, onResetGame: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A2A6C), Color(0xFFB21F1F)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("MERGE TOWN", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(modifier = Modifier.height(50.dp))
            Button(
                onClick = onStartGame,
                modifier = Modifier.width(250.dp).height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1C40F)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("OYUNA BAŞLA", color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
            TextButton(onClick = onResetGame) {
                Text("VERİLERİ SIFIRLA", color = Color.White.copy(0.7f), fontSize = 12.sp)
            }
        }

        // KENDİ İSMİN (Ekranın en altında)
        Text(
            text = "AGİT CAN KILIÇ",
            fontSize = 7.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer(alpha = 0.5f) // Şeffaflık hatasını bu şekilde çözdük
                .padding(bottom = 24.dp)
        )
    }
}