package com.example.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopPanel(
    isOpen: Boolean,
    money: Int,
    energy: Int,
    purchaseCounts: Map<Int, Int>,
    onClose: () -> Unit,
    onBuyEnergy: (Int, Int, Int) -> Unit,
    onBuyBooster: (String, Int) -> Unit // YENİ: Booster satın alma callback'i
) {
    if (isOpen) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            color = Color.White,
            shadowElevation = 12.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()) // İçerik uzarsa kaydırılabilir
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MARKET", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    IconButton(onClick = onClose) { Text("✕") }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- ENERJİ BÖLÜMÜ ---
                Text("⚡ ENERJİ PAKETLERİ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                val energyPackages = listOf(Triple(1, 50, 100), Triple(2, 100, 1000), Triple(3, 150, 2000), Triple(4, 200, 2500))

                energyPackages.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { (id, amount, price) ->
                            val count = purchaseCounts[id] ?: 0
                            val enabled = count < 3 && money >= price && energy < 200

                            OutlinedCard(
                                onClick = { if(enabled) onBuyEnergy(price, amount, id) },
                                modifier = Modifier.weight(1f).aspectRatio(1.2f),
                                enabled = enabled
                            ) {
                                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(if(count < 3 && energy < 200) "⚡" else "🚫", fontSize = 24.sp)
                                    Text("$amount Enerji", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Kalan: ${3-count}", fontSize = 9.sp, color = if(count >= 3) Color.Red else Color.Gray)
                                    Surface(modifier = Modifier.padding(top = 4.dp), color = if(enabled) Color(0xFF27AE60) else Color.Gray, shape = RoundedCornerShape(4.dp)) {
                                        Text("$price 💰", color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // --- BOOSTER BÖLÜMÜ ---
                Spacer(modifier = Modifier.height(8.dp))
                Text("✨ BOOSTER MARKETİ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                val boosterPackages = listOf(
                    Triple("hammer", "🔨 Balyoz", 500),
                    Triple("magnet", "🧲 Mıknatıs", 750),
                    Triple("clock", "⏰ Saat", 500)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    boosterPackages.forEach { (type, label, price) ->
                        val canAfford = money >= price
                        OutlinedCard(
                            onClick = { if(canAfford) onBuyBooster(type, price) },
                            modifier = Modifier.weight(1f).height(100.dp),
                            enabled = canAfford
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(label.split(" ")[0], fontSize = 22.sp)
                                Text(label.split(" ")[1], fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Surface(modifier = Modifier.padding(top = 4.dp), color = if(canAfford) Color(0xFF2E86C1) else Color.Gray, shape = RoundedCornerShape(4.dp)) {
                                    Text("$price 💰", color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}