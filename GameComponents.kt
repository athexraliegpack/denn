package com.example.game
import androidx.compose.animation.*; import androidx.compose.animation.core.*
import androidx.compose.foundation.*; import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*; import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons; import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*; import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*; import androidx.compose.ui.draw.*; import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*; import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*; import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign; import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog; import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay; import kotlin.math.roundToInt
@Composable
fun WorldMap(
    regions: List<MapRegion>,
    diamonds: Int,
    onUnlock: (MapRegion) -> Unit,
    onRegionClick: (MapRegion) -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF1A1A1A))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(modifier = Modifier
                .fillMaxWidth()
                .height(70.dp), color = Color.Black.copy(0.9f), shadowElevation = 8.dp) {
                Row(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("DÜNYA HARİTASI", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💎 $diamonds", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(onClick = onClose) { Text("✕", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(regions) { reg ->
                    val open = reg.isUnlocked
                    val canAfford = diamonds >= reg.unlockCost

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable {
                                // DÜZELTME BURADA:
                                if (open) {
                                    onRegionClick(reg) // Bölge açıksa binaları aç
                                } else if (canAfford) {
                                    onUnlock(reg) // Bölge kapalı ve para yetiyorsa kilidi aç
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = if (open) Color.White else Color.White.copy(0.1f)),
                        shape = RoundedCornerShape(20.dp),
                        border = if (!open && canAfford) BorderStroke(2.dp, Color(0xFFFFD700)) else null
                    ) {
                        Row(modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier
                                .size(80.dp)
                                .background(
                                    if (open) Color(0xFFF0F4FF) else Color.Black.copy(0.3f),
                                    CircleShape
                                ), contentAlignment = Alignment.Center) {
                                Text(reg.emoji, fontSize = 40.sp, modifier = Modifier.alpha(if (open) 1f else 0.4f))
                                if (!open) Text("🔒", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(reg.name, fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (open) Color.Black else Color.White)
                                Text(reg.bonusDescription, fontSize = 13.sp, color = if (open) Color.Gray else Color.White.copy(0.6f))
                                if (!open) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                        Text("Açmak için: ", fontSize = 12.sp, color = Color.White.copy(0.6f))
                                        Text("💎 ${reg.unlockCost}", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                    }
                                }
                            }
                            if (open) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2ECC71))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegionUnlockAnimation(region: MapRegion, onDismiss: () -> Unit) {
    var start by remember { mutableStateOf(false) }; val scale by animateFloatAsState(if (start) 1f else 0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)); val alpha by animateFloatAsState(if (start) 1f else 0f, tween(500))
    LaunchedEffect(Unit) { start = true; delay(4500); onDismiss() }
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(0.9f))
        .zIndex(3000f)
        .clickable { onDismiss() }, contentAlignment = Alignment.Center) {
        val rot by rememberInfiniteTransition().animateFloat(0f, 360f, infiniteRepeatable(tween(4000, easing = LinearEasing)))
        Box(modifier = Modifier
            .size(500.dp)
            .rotate(rot)
            .alpha(0.4f)
            .background(
                Brush.radialGradient(listOf(Color(0xFFFFD700), Color.Transparent)),
                CircleShape
            ))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .scale(scale)
            .alpha(alpha)) { Text("YENİ BÖLGE KEŞFEDİLDİ!", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 26.sp, textAlign = TextAlign.Center); Spacer(modifier = Modifier.height(20.dp)); Text(region.emoji, fontSize = 110.sp); Text(region.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp); Spacer(modifier = Modifier.height(10.dp)); Text("KEŞİF ÖDÜLLERİ", color = Color.White.copy(0.7f), fontSize = 14.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(20.dp)); Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) { RewardBadge("⚡", "+${region.rewardEnergy}"); RewardBadge("💰", "+${region.rewardMoney}"); RewardBadge("💎", "+${region.rewardDiamonds}") }; Spacer(modifier = Modifier.height(50.dp)); Text("DEVAM ETMEK İÇİN DOKUN", color = Color.White.copy(0.4f), fontSize = 12.sp) }
    }
}

@Composable
fun ChestUnlockAnimation(reward: ChestReward, onDismiss: () -> Unit) {
    var start by remember { mutableStateOf(false) }; val scale by animateFloatAsState(if (start) 1f else 0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    LaunchedEffect(Unit) { start = true; delay(4000); onDismiss() }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.85f))
            .clickable { onDismiss() }, contentAlignment = Alignment.Center) {
            val rot by rememberInfiniteTransition().animateFloat(0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing)))
            Box(modifier = Modifier
                .size(450.dp)
                .rotate(rot)
                .alpha(0.3f)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF00E5FF), Color.Transparent)),
                    CircleShape
                ))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale)) { Text("${reward.milestone}. SANDIK AÇILDI!", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 24.sp); Spacer(modifier = Modifier.height(20.dp)); Text(if(reward.milestone == 5) "🏆" else "🎁", fontSize = 100.sp); Spacer(modifier = Modifier.height(20.dp)); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { RewardBadge("💰", "${reward.money}"); RewardBadge("⚡", "${reward.energy}"); RewardBadge("💎", "${reward.diamonds}"); reward.specialItemEmoji?.let { RewardBadge(it, "Özel") } }; Spacer(modifier = Modifier.height(40.dp)); Text("TEBRİKLER!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        }
    }
}

@Composable
fun DailySpinDialog(rewards: List<String>, isSpinning: Boolean, onSpinStart: () -> Unit, onRewardClaimed: (Int) -> Unit, onDismiss: () -> Unit) {
    var rot by remember { mutableStateOf(0f) }; val animRot by animateFloatAsState(rot, tween(3000, easing = FastOutSlowInEasing), finishedListener = { val final = it % 360; onRewardClaimed(((360 - final) / (360f / rewards.size)).toInt() % rewards.size) })
    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50))) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("GÜNLÜK ŞANS ÇARKI", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp); Spacer(modifier = Modifier.height(20.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(260.dp)
                    .rotate(animRot)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val seg = 360f / rewards.size
                        rewards.forEachIndexed { i, _ -> drawArc(if (i % 2 == 0) Color(0xFFE74C3C) else Color(0xFFF1C40F), i * seg, seg, true) }
                    }
                    rewards.forEachIndexed { i, reward ->
                        val seg = 360f / rewards.size
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .rotate(i * seg + seg / 2), contentAlignment = Alignment.TopCenter) {
                            Text(reward, modifier = Modifier.padding(top = 25.dp), color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
                Box(modifier = Modifier.offset(y = (-275).dp), contentAlignment = Alignment.Center) { Text("👇", fontSize = 35.sp) }
                Spacer(modifier = Modifier.height(10.dp)); Button(onClick = { if (!isSpinning) { rot += 1440f + (0..360).random().toFloat(); onSpinStart() } }, enabled = !isSpinning, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))) { Text(if (isSpinning) "DÖNÜYOR..." else "ÇEVİR!", fontWeight = FontWeight.Bold) }; TextButton(onClick = onDismiss, enabled = !isSpinning) { Text("KAPAT", color = Color.White.copy(0.6f)) }
            }
        }
    }
}

@Composable
fun RewardBadge(emoji: String, text: String) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
    .background(Color.White.copy(0.1f), RoundedCornerShape(16.dp))
    .padding(12.dp)
    .width(70.dp)) { Text(emoji, fontSize = 28.sp); Text(text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp) } }

@Composable
fun MergeGrid(gridItems: Array<MergeItem?>, isHammerActive: Boolean, draggingIndex: Int?, dragPositionRoot: Offset, cellBoundsRoot: MutableMap<Int, Rect>, energy: Int, bubbles: List<BubbleItem>, generatorCooldowns: Map<Int, Long>, isGeneratorReady: (Int) -> Boolean, onGeneratorUse: (Int) -> Unit, onBubbleBuy: (BubbleItem) -> Unit, onDragStart: (Int, Offset) -> Unit, onDrag: (Offset) -> Unit, onDragEnd: () -> Unit, onGridUpdate: (Array<MergeItem?>) -> Unit, onEnergyUpdate: (Int) -> Unit, onInfoClick: (List<MergeItem>) -> Unit, onDiamondCollect: (Int) -> Unit) {
    var gridOffset by remember { mutableStateOf(Offset.Zero) }
    Box(modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned { gridOffset = it.positionInRoot() }
        .pointerInput(gridItems, energy, isHammerActive) {
            detectTapGestures(onTap = {
                val hit =
                    cellBoundsRoot.entries.find { e -> e.value.contains(it + gridOffset) }?.key; if (hit != null) {
                val item =
                    gridItems[hit]; if (isHammerActive && item != null) onGeneratorUse(hit) else if (item?.emoji?.contains(
                        "💎"
                    ) == true
                ) onDiamondCollect(hit)
            }
            }, onDoubleTap = {
                if (!isHammerActive) {
                    val hit =
                        cellBoundsRoot.entries.find { e -> e.value.contains(it + gridOffset) }?.key;
                    val item =
                        hit?.let { gridItems[it] }; if (hit != null && item?.isGenerator == true && !item.isLocked && isGeneratorReady(
                            hit
                        ) && energy > 0
                    ) {
                        gridItems.indices
                            .filter { gridItems[it] == null }
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                onGeneratorUse(hit);
                                val newGrid = gridItems.copyOf();
                                val lib = GameData.allLibraries[(item.genType - 1).coerceIn(
                                    0,
                                    GameData.allLibraries.size - 1
                                )]; newGrid[it.random()] =
                                lib[0]; onGridUpdate(newGrid); onEnergyUpdate(energy - 1)
                            }
                    }
                }
            })
        }
        .pointerInput(
            gridItems,
            isHammerActive
        ) {
            detectDragGestures(onDragStart = {
                if (!isHammerActive) cellBoundsRoot.entries.find { e ->
                    e.value.contains(
                        it + gridOffset
                    )
                }?.key?.let { i ->
                    if (gridItems[i]?.isLocked == false) onDragStart(
                        i,
                        it + gridOffset
                    )
                }
            }, onDrag = { change, drag ->
                if (!isHammerActive) {
                    change.consume(); onDrag(drag)
                }
            }, onDragEnd = { if (!isHammerActive) onDragEnd() })
        }) {
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize(), userScrollEnabled = false) { items(56) { idx ->
            val item = gridItems[idx]; val bub = bubbles.find { it.index == idx }; val ready = isGeneratorReady(idx); val target = draggingIndex != null && cellBoundsRoot[idx]?.contains(dragPositionRoot) == true; val ham = isHammerActive && item != null
            Box(modifier = Modifier
                .aspectRatio(1f)
                .padding(1.dp)
                .onGloballyPositioned { cellBoundsRoot[idx] = it.boundsInRoot() }
                .background(
                    if (item?.isLocked == true) Color(0xFFD5DBDB) else if (item?.isGenerator == true) Color(
                        0xFFF0F4FF
                    ) else Color.White, RoundedCornerShape(4.dp)
                )
                .border(
                    if (target || ham) 2.dp else 1.dp,
                    if (ham) Color.Red else if (target) Color(0xFF3498DB) else Color(0xFFE5E8E8),
                    RoundedCornerShape(4.dp)
                ), contentAlignment = Alignment.Center) {
                if (item != null && draggingIndex != idx) Box(modifier = Modifier.fillMaxSize()) {

                    if (item.emoji.contains("💎")) Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { when(item.level) { 1->Text("💎", fontSize = 24.sp); 2->Box { Text("💎", fontSize = 22.sp, modifier = Modifier.offset((-4).dp, 4.dp)); Text("💎", fontSize = 22.sp, modifier = Modifier.offset(4.dp, (-4).dp)) }; 3->Box { Text("💎", fontSize = 18.sp, modifier = Modifier.offset((-6).dp, 6.dp)); Text("💎", fontSize = 18.sp, modifier = Modifier.offset(6.dp, 6.dp)); Text("💎", fontSize = 18.sp, modifier = Modifier.offset(y = (-6).dp)) }; else->Text("💎", fontSize = 30.sp, fontWeight = FontWeight.Bold) } }
                    else Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer(alpha = if (item.isLocked) 0.4f else 1f)) { Text(item.emoji, fontSize = if (item.isGenerator) 18.sp else 24.sp); if (item.isLocked) Text("🔒", fontSize = 8.sp) }
                    if (item.isGenerator && !ready) Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.4f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) { Text("⏳", fontSize = 16.sp) }
                }
                if (bub != null) Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .background(Color(0x88AED6F1), CircleShape)
                    .clickable { onBubbleBuy(bub) }, contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(bub.item.emoji, fontSize = 18.sp); Text("${bub.timeLeft}s", fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold) } }
            }
        } }
    }
}

@Composable
fun GameBottomMenu(isStorageOpen: Boolean, hammerCount: Int, magnetCount: Int, clockCount: Int, onHammerClick: () -> Unit, onMagnetClick: () -> Unit, onClockClick: () -> Unit, onMenuClick: () -> Unit, onStorageClick: () -> Unit, onTasksClick: () -> Unit, onMapClick: () -> Unit, onShopClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) { BoosterItem("🔨", hammerCount, onHammerClick); BoosterItem("🧲", magnetCount, onMagnetClick); BoosterItem("⏰", clockCount, onClockClick) }
        Surface(modifier = Modifier
            .fillMaxWidth()
            .height(75.dp), shadowElevation = 12.dp, color = Color.White) { Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onMenuClick) { Text("🏠", fontSize = 26.sp) }; IconButton(onClick = onStorageClick) { Text(if (isStorageOpen) "📂" else "📦", fontSize = 26.sp) }; IconButton(onClick = onTasksClick) { Text("🎯", fontSize = 26.sp) }; IconButton(onClick = onMapClick) { Text("🗺️", fontSize = 26.sp) }; IconButton(onClick = onShopClick) { Text("🛒", fontSize = 26.sp) } } }
    }
}

@Composable
fun BoosterItem(emoji: String, count: Int, onClick: () -> Unit) { Box(modifier = Modifier
    .size(40.dp)
    .background(Color.White, CircleShape)
    .border(1.dp, Color(0xFFEEEEEE), CircleShape)
    .clickable { onClick() }, contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp); Box(modifier = Modifier
    .align(Alignment.BottomEnd)
    .size(20.dp)
    .background(if (count > 0) Color(0xFFE74C3C) else Color.Gray, CircleShape)
    .border(1.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) { Text(count.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) } } }

// 1. ÖNCE BU YENİ BİLEŞENİ EKLEYİN (DailyTasksPanel'in üstüne veya altına koyabilirsiniz)
@Composable
fun TaskProgressBar(
    currentProgress: Int,
    claimedChests: List<Int>,
    onChestClick: (Int) -> Unit
) {
    val milestones = listOf(20, 40, 60, 80, 100)
    val maxProgress = 100f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFFF0F4F8), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFDDE4ED), RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 12.dp) // Dikey padding'i biraz azalttık
    ) {
        Text(
            text = "İlerleme: $currentProgress / 100",
            color = Color(0xFF2D3436),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 20.dp) // Yazı ile bar arasına daha fazla boşluk (12'den 20'ye)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp), // Yüksekliği biraz daralttık
            contentAlignment = Alignment.CenterStart
        ) {
            // Arka Plan Çubuğu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDDE4ED))
            )

            // İlerleme Çubuğu
            val animatedProgress by animateFloatAsState(
                targetValue = (currentProgress / maxProgress).coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 1000)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))))
            )

            // Milestones
            milestones.forEach { milestone ->
                val isReached = currentProgress >= milestone
                val isClaimed = claimedChests.contains(milestone)
                val bias = (milestone / maxProgress) * 2 - 1

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = BiasAlignment(bias.toFloat(), 0f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // İSTEDİĞİN EMOJİ SETİ
                        val chestEmoji = when {
                            isClaimed -> "\uD83D\uDD13" // Alındı: Hediye Paketi
                            isReached -> "\uD83C\uDF81" // Hazır: Açık Kilit
                            else -> "🔒"      // Kilitli: Kapalı Kilit
                        }

                        // Scale değerini 1.4'ten 1.2'ye düşürdük (Çok büyümesin)
                        val scale by animateFloatAsState(if (isReached && !isClaimed) 1.2f else 1.0f)

                        Text(
                            text = chestEmoji,
                            fontSize = 18.sp, // Fontu 20'den 18'e düşürdük
                            modifier = Modifier
                                .scale(scale)
                                .clickable(enabled = isReached && !isClaimed) { onChestClick(milestone) }
                                .offset(y = (-12).dp) // Yukarı kaymayı -16'dan -12'ye çektik
                        )
                        Text(
                            text = milestone.toString(),
                            color = if (isReached) Color(0xFF2D3436) else Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.offset(y = 14.dp)
                        )
                    }
                }
            }
        }
    }
}

// 2. MEVCUT DailyTasksPanel FONKSİYONUNUZU BU ŞEKİLDE GÜNCELLEYİN
@Composable // Sadece bir tane @Composable olmalı!
fun DailyTasksPanel(
    tasks: List<DailyTask>,
    progress: Int,
    claimed: List<Int>,
    onClaimChest: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Günlük Görevler", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Yeni ProgressBar burada çağrılıyor
        TaskProgressBar(
            currentProgress = progress,
            claimedChests = claimed,
            onChestClick = onClaimChest
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9F9), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.description, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        LinearProgressIndicator(
                            progress = (task.currentCount.toFloat() / task.targetCount.toFloat()).coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(4.dp)
                                .clip(CircleShape),
                            color = if(task.currentCount >= task.targetCount) Color(0xFF2ECC71) else Color(0xFF3498DB)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                        Text("${task.pointValue} Puan", fontSize = 11.sp, color = Color(0xFFE67E22), fontWeight = FontWeight.Bold)
                        Text("${task.currentCount}/${task.targetCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DailyBonusDialog(onClaim: () -> Unit) = AlertDialog(onDismissRequest = {}, confirmButton = { Button(onClick = onClaim) { Text("AL") } }, title = { Text("Günlük Hediye!") }, text = { Text("Bugünkü hediyen: 200💰, 5💎 ve 50⚡") })

@Composable
fun GhostItem(emoji: String, position: Offset) { val animPos by animateOffsetAsState(position, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessHigh)); Box(modifier = Modifier
    .offset { IntOffset(animPos.x.roundToInt() - 70, animPos.y.roundToInt() - 160) }
    .size(90.dp)
    .zIndex(1000f), contentAlignment = Alignment.Center) { Text(emoji, fontSize = if (emoji.contains("⚡")) 30.sp else 46.sp) } }

@Composable
fun OrdersRow(orders: List<Order>, gridItems: Array<MergeItem?>, onOrderComplete: (Order) -> Unit, onOrderCancel: (Order) -> Unit) {
    LazyRow(modifier = Modifier
        .fillMaxWidth()
        .height(115.dp), contentPadding = PaddingValues(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(orders) { o ->
        val can = GameLogic.canFulfillOrder(o, gridItems); Card(modifier = Modifier.size(105.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) { Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(2.dp)
            .size(18.dp)
            .background(Color(0xFFF2F3F4), CircleShape)
            .clickable { onOrderCancel(o) }, contentAlignment = Alignment.Center) { Text("✕", fontSize = 10.sp, color = Color.Gray) }
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) { Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) { o.requiredItems.forEach { Text(it.emoji, fontSize = 22.sp) } }; Text("${o.reward} 💰", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if(can) Color(0xFF27AE60) else Color.Gray); Button(onClick = { onOrderComplete(o) }, enabled = can, modifier = Modifier
            .fillMaxWidth()
            .height(22.dp), contentPadding = PaddingValues(0.dp), shape = RoundedCornerShape(4.dp)) { Text("SAT", fontSize = 9.sp) } }
    } }
    } }
}

@Composable
fun StoragePanel(isStorageOpen: Boolean, storageItems: List<MergeItem>, storageCapacity: Int, money: Int, onUpgrade: () -> Unit, onItemClick: (Int, MergeItem) -> Unit, onPositioned: (Rect) -> Unit) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .onGloballyPositioned { onPositioned(it.boundsInRoot()) }, color = Color.White, shadowElevation = 8.dp) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("DEPO (${storageItems.size}/$storageCapacity)", fontWeight = FontWeight.Bold); if (storageCapacity < 10) Button(onClick = onUpgrade, enabled = money >= 500, modifier = Modifier.height(30.dp), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Bölme Aç (500💰)", fontSize = 10.sp) } }
            Spacer(modifier = Modifier.height(8.dp)); LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(10) { i -> val open = i < storageCapacity; val item = if (i < storageItems.size) storageItems[i] else null; Box(modifier = Modifier
            .size(65.dp)
            .background(
                if (open) Color(0xFFF8F9F9) else Color(0xFFD5DBDB),
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, if (open) Color(0xFFE5E8E8) else Color.Gray, RoundedCornerShape(8.dp))
            .clickable(enabled = open && item != null) { item?.let { onItemClick(i, it) } }, contentAlignment = Alignment.Center) { if (!open) Text("🔒", fontSize = 20.sp) else if (item != null) Text(item.emoji, fontSize = 38.sp) } } }
        }
    }
}

@Composable
fun GeneratorInfoDialog(library: List<MergeItem>, onDismiss: () -> Unit) = AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = onDismiss) { Text("KAPAT") } }, title = { Text("Üretim Zinciri", fontWeight = FontWeight.Black) }, text = { Column { library.chunked(4).forEach { r -> Row(modifier = Modifier
    .fillMaxWidth()
    .padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) { r.forEach { i -> Column(horizontalAlignment = Alignment.CenterHorizontally) { Surface(modifier = Modifier.size(50.dp), color = i.color.copy(0.1f), shape = RoundedCornerShape(8.dp)) { Box(contentAlignment = Alignment.Center) { Text(i.emoji, fontSize = 28.sp) } }; Text("Lvl ${i.level}", fontSize = 9.sp) } } } } } } )

@Composable
fun LevelUpAnimation(level: Int, onDismiss: () -> Unit) { LaunchedEffect(Unit) { delay(3000); onDismiss() }; Box(modifier = Modifier
    .fillMaxSize()
    .background(Color.Black.copy(0.6f))
    .zIndex(2000f)
    .clickable { onDismiss() }, contentAlignment = Alignment.Center) { Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.size(250.dp)) { Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text("🌟", fontSize = 50.sp); Text("TEBRİKLER!", fontWeight = FontWeight.Black, fontSize = 24.sp); Text("SEVİYE ATLADIN", color = Color.Gray); Text(level.toString(), fontSize = 60.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700)) } } } }

@Composable
fun CollectionBookDialog(discovered: Set<String>, claimedRewards: Set<Int>, onClaimReward: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = onDismiss) { Text("KAPAT") } }, title = { Text("Koleksiyon Albümü", fontWeight = FontWeight.Black) }, text = { Box(modifier = Modifier.size(400.dp)) { LazyColumn { itemsIndexed(GameData.allLibraries) { index, library -> val complete = library.all { discovered.contains(it.emoji) }; val claimed = claimedRewards.contains(index); Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFEFE)), border = BorderStroke(1.dp, Color(0xFFE5E8E8))) { Column(modifier = Modifier.padding(8.dp)) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Kategori ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp); if (complete && !claimed) Button(onClick = { onClaimReward(index) }, modifier = Modifier.height(24.dp), contentPadding = PaddingValues(0.dp)) { Text("ÖDÜL AL", fontSize = 9.sp) } else if (claimed) Text("ALINDI ✅", color = Color.Gray, fontSize = 10.sp) }; Spacer(modifier = Modifier.height(8.dp)); library.chunked(5).forEach { row -> Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) { for (item in row) { val found = discovered.contains(item.emoji); Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(modifier = Modifier
        .size(45.dp)
        .background(
            if (found) item.color.copy(0.1f) else Color.LightGray.copy(0.2f),
            RoundedCornerShape(8.dp)
        ), contentAlignment = Alignment.Center) { Text(if (found) item.emoji else "?", fontSize = 24.sp) }; Text("Lvl ${item.level}", fontSize = 8.sp, color = Color.Gray) } }; repeat(5 - row.size) { Spacer(modifier = Modifier.size(45.dp)) } } } } } } } } } )
}

@Composable
fun PetPanel(
    level: Int,
    hunger: Int,
    xp: Int,
    hungerTimer: Int,
    gridItems: Array<MergeItem?>,
    petLevelRewards: List<PetLevelReward>, // Ödül listesi eklendi
    onFeed: (MergeItem, Int) -> Unit,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ejderha Yuvası 🐲", fontSize = 22.sp, fontWeight = FontWeight.Black)

                // --- SEVİYE AVANTAJLARI LİSTESİ (YENİ) ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFFF4F7F6), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Text("🏆 Seviye Avantajları", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE67E22))
                    Spacer(modifier = Modifier.height(4.dp))

                    petLevelRewards.forEach { reward ->
                        val isReached = level >= reward.level
                        val isCurrent = level == reward.level

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                                .background(
                                    if (isCurrent) Color.White else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isReached) "✅ L${reward.level}" else "🔒 L${reward.level}",
                                fontSize = 10.sp,
                                modifier = Modifier.width(45.dp),
                                color = if (isReached) Color(0xFF2E7D32) else Color.Gray
                            )
                            Text(
                                text = "⚡${reward.energyTimer}sn | 💎%${reward.diamondChance}",
                                fontSize = 10.sp,
                                modifier = Modifier.weight(1f),
                                color = if (isReached) Color.Black else Color.Gray
                            )
                            if (reward.instantGift != "Başlangıç") {
                                Text(reward.instantGift, fontSize = 9.sp, color = if(isReached) Color.Magenta else Color.LightGray)
                            }
                        }
                    }
                }

                // SEVİYE VE XP BÖLÜMÜ
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mevcut Seviye: $level", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("XP: %$xp", color = Color.Magenta, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                LinearProgressIndicator(
                    progress = xp / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color.Magenta,
                    trackColor = Color.LightGray.copy(0.3f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // TOKLUK BÖLÜMÜ
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9F9), RoundedCornerShape(12.dp))
                    .padding(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ejderha Tokluğu: %$hunger", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${hungerTimer}s", fontSize = 11.sp, color = Color.Gray)
                    }
                    LinearProgressIndicator(
                        progress = hunger / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .padding(top = 4.dp)
                            .clip(CircleShape),
                        color = if(hunger < 20) Color.Red else Color(0xFF2ECC71),
                        trackColor = Color.LightGray.copy(0.5f)
                    )
                }

                Text("Beslemek İçin Eşya Seç:", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

                // IZGARA
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    gridItems.forEachIndexed { index, item ->
                        if (item != null && !item.isGenerator && !item.isLocked) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(55.dp)
                                        .background(
                                            item.color.copy(alpha = 0.15f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .border(
                                            1.dp,
                                            item.color.copy(0.3f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onFeed(item, index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(item.emoji, fontSize = 22.sp)
                                        Text("Lvl ${item.level}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(onClick = onClose, modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)) {
                    Text("KAPAT")
                }
            }
        }
    }
}
@Composable
fun TownBuildingScreen(
    region: MapRegion,
    playerMoney: Int,
    onUpgrade: (Building) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = false) {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Üst Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(" ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                IconButton(onClick = onClose) {
                    Text("❌", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ANA GÖRSEL (Bölge İkonu)
            // Burası senin hayal ettiğin: Binalar geliştikçe parlayacak olan ana görsel
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(region.emoji, fontSize = 70.sp)
            }

            Text(region.name, color = Color.Yellow, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("Gelişmişlik: %${(region.buildings.sumOf { it.level } * 100 / (region.buildings.size * 5))}", color = Color.LightGray)

            Spacer(modifier = Modifier.height(20.dp))

            // BİNALAR LİSTESİ
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(region.buildings) { building ->
                    BuildingItemCard(building, playerMoney, onUpgrade)
                }
            }
        }
    }
}

@Composable
fun BuildingItemCard(building: Building, playerMoney: Int, onUpgrade: (Building) -> Unit) {
    val cost = building.baseCost * (building.level + 1)
    val isMax = building.level >= building.maxLevel
    val currentEmoji = if (building.level == 0) "🏗️" else building.emojis.getOrNull(building.level) ?: building.emojis.last()

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(currentEmoji, fontSize = 40.sp)
        Text(building.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)

        // Yıldızlar
        Row {
            repeat(5) { i ->
                Text(
                    text = "⭐",
                    fontSize = 10.sp,
                    modifier = Modifier.alpha(if (i < building.level) 1f else 0.2f) // Alpha buraya taşındı
                )
            }
        }

        Text(text = building.bonusDescription, color = Color.Cyan, fontSize = 10.sp, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(8.dp))

        if (!isMax) {
            Button(
                onClick = { onUpgrade(building) },
                enabled = playerMoney >= cost,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text("💰 $cost", fontSize = 12.sp)
            }
        } else {
            // Color.Gold yerine Color(0xFFFFD700) (Altın Rengi) kullandık
            Text(text = "MAX", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PetLevelUpAnimation(reward: PetLevelReward, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("EJDERHA GELİŞTİ! 🐲", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFE67E22))
                Text("SEVİYE ${reward.level}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2ECC71))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Kalıcı Bonuslar:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("⚡ Enerji Hızı: ${reward.energyTimer}sn", fontSize = 16.sp)
                Text("💎 Elmas Şansı: %${reward.diamondChance}", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Hediye: ${reward.instantGift}", fontWeight = FontWeight.Bold, color = Color.Magenta)
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("HARİKA!") }
            }
        }
    }
}
