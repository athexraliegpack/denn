import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenu(onStartGame: () -> Unit, onResetGame: () -> Unit) {
    // Ana kapsayıcı Box (Tüm ekranı kaplar)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A2A6C),
                        Color(0xFFB21F1F)
                    )
                )
            ),
        contentAlignment = Alignment.Center // İçindeki Column'u merkeze alır
    ) {
        // Orta kısımdaki butonlar ve başlık
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("MERGE TOWN", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(modifier = Modifier.height(50.dp))
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .width(250.dp)
                    .height(70.dp),
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

        // KENDİ İSMİN (Column dışına, Box içine aldık)
        Text(
            text = "AGİT CAN KILIÇ",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.5f), // Beyazın şeffaf hali daha şık durur
            modifier = Modifier
                .align(Alignment.BottomCenter) // Şimdi Box'a göre en alta hizalanır
                .padding(bottom = 24.dp)       // En alttan biraz boşluk
        )
    }
}