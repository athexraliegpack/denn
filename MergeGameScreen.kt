package com.example.game

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MergeGameScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("MergeGameSave", Context.MODE_PRIVATE) }
    val vm = remember { MergeGameViewModel(context, sharedPref) }
    var isHammerActive by remember { mutableStateOf(false) }



    // ANA KAPSAYICI BOX
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF4F7F6))) {

        Column(modifier = Modifier.fillMaxSize()) {
            // Üst Panel (Seviye, Enerji, Para, Elmas)
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(45.dp)
                    .background(Color(0xFF3498DB), CircleShape), contentAlignment = Alignment.Center) {
                    Text(vm.playerLevel.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 18.sp)
                        Text("${vm.energy}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (vm.energy < vm.maxEnergy) {
                            Text(" (${vm.timerSeconds}s)", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(start = 2.dp))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💰", fontSize = 18.sp)
                        Text("${vm.money}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💎", fontSize = 18.sp)
                        Text("${vm.diamonds}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(color = Color(0xFF4FC3F7).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                            Text(text = "%${vm.getTotalDiamondChanceDisplay()}", color = Color(0xFF2980B9), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                        }
                        IconButton(onClick = { vm.isLibraryMenuOpen = true }) {
                            Text("ℹ️", fontSize = 20.sp)
                        }
                    }
                }
            }

            // Jeneratör Bilgi Mesajı
            AnimatedVisibility(visible = vm.selectedGeneratorInfo != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(Color(0xFF34495E).copy(alpha = 0.9f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                    Text(text = vm.selectedGeneratorInfo ?: "", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 2.dp))
                }
            }

            LinearProgressIndicator(progress = (vm.playerXp.toFloat() / vm.xpToNextLevel.toFloat()).coerceIn(0f, 1f), modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .padding(horizontal = 16.dp), color = Color(0xFF3498DB), trackColor = Color.LightGray.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))

            // Orta Menü Butonları
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .clickable { vm.showCollection = true }, contentAlignment = Alignment.Center) { Text("📖", fontSize = 22.sp) }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .clickable {
                        if (vm.checkSpinReady()) vm.isSpinOpen = true else Toast.makeText(context, "Yarın tekrar gel!", Toast.LENGTH_SHORT).show()
                    }, contentAlignment = Alignment.Center) {
                    Text("🎡", fontSize = 22.sp)
                    if (vm.checkSpinReady()) Box(modifier = Modifier.size(10.dp).background(Color.Red, CircleShape).align(Alignment.TopEnd))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .clickable { vm.isPetOpen = true }, contentAlignment = Alignment.Center) { Text("🐲", fontSize = 22.sp) }
            }

            // Siparişler
            Box(modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 110.dp)
                .padding(horizontal = 4.dp, vertical = 2.dp)) {
                OrdersRow(vm.orders, vm.gridItems, { vm.onOrderComplete(it) }, { vm.onOrderCancel(it) })
            }

            // Oyun Izgarası
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp)) {
                MergeGrid(vm.gridItems, isHammerActive, vm.draggingIndex, vm.dragPositionRoot, vm.cellBoundsRoot, vm.energy, vm.bubbles, vm.generatorCooldowns, { vm.isGeneratorReady(it) }, { if (isHammerActive) { vm.useHammer(it); isHammerActive = false } else vm.useGenerator(it) }, { if (vm.money >= 750) { vm.money -= 750; vm.gridItems[it.index] = it.item; vm.bubbles.remove(it); vm.save() } }, { i, p -> if (!isHammerActive) { vm.draggingIndex = i; vm.dragPositionRoot = p } }, { vm.dragPositionRoot += it }, { vm.onDragEnd() }, { vm.gridItems = it }, { vm.energy = it }, { }, { vm.collectDiamond(it) })
                if (isHammerActive) Surface(modifier = Modifier.fillMaxWidth().padding(16.dp).zIndex(10f), color = Color.Red.copy(0.9f), shape = RoundedCornerShape(8.dp)) {
                    Text("BALYOZ AKTİF: Yok etmek istediğin eşyaya tıkla!", color = Color.White, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center, fontSize = 12.sp)
                }
            }

            // Alt Menü
            Column(modifier = Modifier.fillMaxWidth()) {
                if (vm.isStorageOpen) StoragePanel(vm.isStorageOpen, vm.storageItems, vm.storageCapacity, vm.money, { if (vm.money >= 500 && vm.storageCapacity < 10) { vm.money -= 500; vm.storageCapacity++; vm.save() } }, { idx, item -> vm.gridItems.indexOfFirst { it == null }.takeIf { it != -1 }?.let { vm.gridItems[it] = item; vm.storageItems.removeAt(idx); vm.save() } }, { vm.storageBoundsRoot = it })
                GameBottomMenu(
                    vm.isStorageOpen,
                    vm.hammerCount,
                    vm.magnetCount,
                    vm.clockCount,
                    { if (vm.hammerCount > 0) isHammerActive = !isHammerActive else Toast.makeText(context, "Balyozun yok!", Toast.LENGTH_SHORT).show() },
                    { if (vm.magnetCount > 0) { vm.useMagnet(); Toast.makeText(context, "Mıknatıs Kullanıldı!", Toast.LENGTH_SHORT).show() } },
                    { if (vm.clockCount > 0) { vm.useClock(); Toast.makeText(context, "Tüm süreler sıfırlandı!", Toast.LENGTH_SHORT).show() } },
                    onBackToMenu,
                    { vm.isStorageOpen = !vm.isStorageOpen; vm.isShopOpen = false; vm.isTasksOpen = false; vm.isMapOpen = false; vm.isSideQuestsOpen = false },
                    { vm.isTasksOpen = !vm.isTasksOpen; vm.isStorageOpen = false; vm.isShopOpen = false; vm.isMapOpen = false; vm.isSideQuestsOpen = false },
                    { vm.isSideQuestsOpen = !vm.isSideQuestsOpen; vm.isTasksOpen = false; vm.isStorageOpen = false; vm.isShopOpen = false; vm.isMapOpen = false }, // YENİ EKLENEN YAN GÖREV PARAMETRESİ
                    { vm.isMapOpen = !vm.isMapOpen; vm.isTasksOpen = false; vm.isStorageOpen = false; vm.isShopOpen = false; vm.isSideQuestsOpen = false },
                    { vm.isShopOpen = !vm.isShopOpen; vm.isStorageOpen = false; vm.isTasksOpen = false; vm.isMapOpen = false; vm.isSideQuestsOpen = false }
                )
            }
        }

        // --- Dialoglar ve Paneller (Üst Katmanlar) ---

        if (vm.isMapOpen) {
            WorldMap(regions = vm.regions, diamonds = vm.diamonds, onUnlock = { vm.unlockRegion(it) }, onRegionClick = { region -> if (region.isUnlocked) { vm.selectedRegionForBuilding = region; vm.isMapOpen = false } else { vm.unlockedRegionReward = region } }, onClose = { vm.isMapOpen = false })
        }

        if (vm.isSideQuestsOpen) {
            Dialog(onDismissRequest = { vm.isSideQuestsOpen = false }) {
                SideQuestsPanel(
                    quests = vm.activeSideQuests,
                    onClaim = { vm.claimSideQuest(it) },
                    onClose = { vm.isSideQuestsOpen = false }
                )
            }
        }

        vm.unlockedRegionReward?.let { RegionUnlockAnimation(it) { vm.unlockedRegionReward = null } }
        vm.unlockedChestReward?.let { ChestUnlockAnimation(it) { vm.unlockedChestReward = null } }

        if (vm.isPetOpen) {
            PetPanel(level = vm.petLevel, hunger = vm.petHunger, xp = vm.petXp, hungerTimer = vm.hungerTimer, gridItems = vm.gridItems, petLevelRewards = vm.petLevelRewards, onFeed = { item, index -> vm.feedPet(item, index) }, onClose = { vm.isPetOpen = false })
        }

        if (vm.isLibraryMenuOpen) {
            Dialog(onDismissRequest = { vm.isLibraryMenuOpen = false }) {
                Surface(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.8f), shape = RoundedCornerShape(16.dp), color = Color.White) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Üretim Rehberi", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        LazyRow(modifier = Modifier.padding(vertical = 12.dp)) {
                            itemsIndexed(GameData.libraryNames) { index, name ->
                                val isSelected = vm.selectedLibraryIndex == index
                                Surface(modifier = Modifier.padding(end = 8.dp).clickable { vm.selectedLibraryIndex = index }, color = if (isSelected) Color(0xFF3498DB) else Color(0xFFECF0F1), shape = RoundedCornerShape(12.dp)) {
                                    Text(text = name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = if (isSelected) Color.White else Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Divider()
                        LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                            val currentLib = GameData.allLibraries[vm.selectedLibraryIndex]
                            items(currentLib) { item ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                    Box(modifier = Modifier.size(50.dp).background(item.color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        Text(item.emoji, fontSize = 26.sp)
                                    }
                                    Text("Lv.${item.level}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                        Button(onClick = { vm.isLibraryMenuOpen = false }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Kapat") }
                    }
                }
            }
        }

        ShopPanel(vm.isShopOpen, vm.money, vm.energy, vm.purchaseCounts, { vm.isShopOpen = false }, { p, a, id -> vm.onBuyEnergy(p, a, id) }, { t, c -> vm.buyBooster(t, c) })
        if (vm.isTasksOpen) Dialog({ vm.isTasksOpen = false }) { DailyTasksPanel(vm.dailyTasks, vm.taskProgress, vm.claimedChests, { vm.openChest(it) }) }

        // GÜNLÜK BONUS DİALOGU
        //if (vm.showDailyBonus) DailyBonusDialog { vm.claimDailyBonus() }

        if (vm.showCollection) CollectionBookDialog(vm.discoveredItems.toSet(), vm.claimedRewards, { vm.claimCategoryReward(it); Toast.makeText(context, "Ödül Alındı!", Toast.LENGTH_SHORT).show() }, { vm.showCollection = false })
        if (vm.draggingIndex != null) GhostItem(vm.gridItems[vm.draggingIndex!!]?.emoji ?: "", vm.dragPositionRoot)
        if (vm.showLevelAnim) LevelUpAnimation(vm.playerLevel) { vm.showLevelAnim = false }

        if (vm.isSpinOpen) {
            DailySpinDialog(rewards = vm.spinRewards, isSpinning = vm.isSpinning, onSpinStart = { vm.isSpinning = true }, onRewardClaimed = { index -> vm.claimSpinReward(index); vm.isSpinning = false; vm.isSpinOpen = false; Toast.makeText(context, "Tebrikler: ${vm.spinRewards[index]}", Toast.LENGTH_LONG).show() }, onDismiss = { vm.isSpinOpen = false })
        }

        if (vm.showPetLevelUpAnimation) {
            val currentReward = vm.petLevelRewards.find { it.level == vm.petLevel } ?: vm.petLevelRewards.last()
            PetLevelUpAnimation(reward = currentReward, onDismiss = { vm.showPetLevelUpAnimation = false })
        }

        vm.selectedRegionForBuilding?.let { region ->
            TownBuildingScreen(region = region, playerMoney = vm.money, onUpgrade = { vm.upgradeBuilding(it) }, onClose = { vm.selectedRegionForBuilding = null })
        }

        // YAN GÖREV ÖDÜL ANİMASYONU
        if (vm.showSideQuestReward) {
            SideQuestRewardAnimation(
                reward = vm.currentSideQuestReward,
                onDismiss = {
                    vm.showSideQuestReward = false
                    vm.currentSideQuestReward = null
                }
            )
        }

        // EN ÜST KATMAN: SANDIK AÇILMA ANİMASYONU
        DailyChestAnimation(
            showDailyBonus = vm.showDailyBonus,      // Ödül hazır mı?
            showChestAnimation = vm.showChestAnimation, // Patlama animasyonu sürüyor mu?
            onClaim = { vm.claimDailyBonus() }       // Tıklayınca ödülü al
        )
    }
}

@Composable
fun DailyChestAnimation(
    showDailyBonus: Boolean,
    showChestAnimation: Boolean,
    onClaim: () -> Unit
) {
    // Eğer ikisi de false ise hiçbir şey gösterme
    if (!showDailyBonus && !showChestAnimation) return

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .zIndex(200f)
            // Eğer ödül hazırsa (henüz tıklanmadıysa) tıklamayı aktif et
            .clickable(enabled = showDailyBonus) { onClaim() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showDailyBonus) {
                // --- DURUM 1: OYUN AÇILDI, ÖDÜL BEKLİYOR (TIKLA VE AÇ) ---
                Text(
                    text = "GÜNLÜK HEDİYEN!",
                    color = Color.Yellow,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "✨", fontSize = 150.sp, modifier = Modifier.graphicsLayer(alpha = 0.5f))
                    Text(text = "🎁", fontSize = 130.sp, modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "AÇMAK İÇİN DOKUN",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (showChestAnimation) {
                // --- DURUM 2: TIKLANDI, ÖDÜLLER FIRLIYOR ---
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "✨",
                        fontSize = 150.sp,
                        modifier = Modifier.graphicsLayer(scaleX = scale * 1.5f, scaleY = scale * 1.5f, alpha = 0.6f)
                    )
                    Text(
                        text = "📦", // Açılmış kutu
                        fontSize = 100.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    RewardItem("200 💰", delayTime = 0)
                    RewardItem("5 💎", delayTime = 200)
                    RewardItem("50 ⚡", delayTime = 400)
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    "TEBRİKLER!",
                    color = Color.Green,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
fun RewardItem(text: String, delayTime: Int) {
    val animatedY = remember { Animatable(50f) }
    val animatedAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(delayTime.toLong())
        launch { animatedY.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
        launch { animatedAlpha.animateTo(1f, tween(600)) }
    }

    Surface(
        color = Color(0xFF2C3E50),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .graphicsLayer(translationY = animatedY.value, alpha = animatedAlpha.value)
            .border(2.dp, Color.Yellow, RoundedCornerShape(12.dp))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SideQuestRewardAnimation(
    reward: ChestReward?,
    onDismiss: () -> Unit
) {
    if (reward == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .zIndex(300f) // En üstte görünmesi için
            .clickable { onDismiss() }, // Ekrana tıklandığında kapanır
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "GÖREV TAMAMLANDI!",
                color = Color(0xFFF1C40F),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Parlama Efekti ve İkon
            Box(contentAlignment = Alignment.Center) {
                Text("🌟", fontSize = 120.sp, modifier = Modifier.graphicsLayer(alpha = 0.4f, scaleX = scale * 1.5f))
                Text("📜", fontSize = 90.sp) // Görev parşömeni ikonu
            }

            Spacer(modifier = Modifier.height(30.dp))

            // DİNAMİK ÖDÜLLER: Sadece değeri 0'dan büyük olanlar görünür
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                if (reward.money > 0) RewardItem("${reward.money} 💰", delayTime = 100)
                if (reward.diamonds > 0) RewardItem("${reward.diamonds} 💎", delayTime = 300)
                if (reward.energy > 0) RewardItem("${reward.energy} ⚡", delayTime = 500)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Devam etmek için dokun",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}