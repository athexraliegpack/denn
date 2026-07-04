package com.example.game
import android.content.Context; import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.ViewModel // Bunu importlara ekle
import androidx.compose.runtime.*; import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect; import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.coerceAtLeast
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch // BU EKSİKTİ: Coroutine başlatmak için şart
import java.text.SimpleDateFormat; import java.util.*
import kotlin.random.Random
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf

data class BubbleItem(val item: MergeItem, val index: Int, var timeLeft: Int = 30)
data class MarketItem(
    val id: Int,
    val name: String,
    val emoji: String,
    val price: Int,
    val category: String
)

data class SideQuest(
    val id: Int,
    val title: String,
    val description: String,
    val targetCount: Int,
    var currentCount: Int,
    val rewardMoney: Int = 0,
    val rewardDiamonds: Int = 0,
    val type: String, // "merge", "order", "spend_energy" vb.
    var level: Int = 1
)




enum class TaskType { MERGE, GENERATE, CANCEL_ORDER, COMPLETE_ORDER, SPIN_WHEEL, FEED_PET, COLLECT_DIAMOND, USE_HAMMER, USE_MAGNET, UPGRADE_BUILDING, OPEN_CHEST }
data class DailyTask(
    val id: Int,
    val description: String,
    val type: TaskType,
    val targetCount: Int,
    var currentCount: Int = 0,
    var isClaimed: Boolean = false,
    val pointValue: Int = 10 // Her görevin vereceği puan
)
data class ChestReward(val money: Int = 0, val energy: Int = 0, val specialItemEmoji: String? = null, val diamonds: Int = 0, val milestone: Int = 0)
data class Building(
    val id: Int,
    val name: String,
    var level: Int = 0,
    val maxLevel: Int = 5,
    val baseCost: Int,
    val emojis: List<String>,
    val bonusDescription: String
)

data class MapRegion(
    val id: Int,
    val name: String,
    val unlockCost: Int,
    var isUnlocked: Boolean = false,
    val emoji: String,
    val bonusDescription: String,
    val rewardEnergy: Int = 0,
    val rewardMoney: Int = 0,
    val rewardDiamonds: Int = 0,
    val buildings: MutableList<Building> = mutableListOf() // Binalar buraya eklendi
)
// 1. Veri tipini tanımla (Class'ın dışına veya içine koyabilirsin)
data class PetLevelReward(val level: Int, val energyTimer: Int, val diamondChance: Int, val instantGift: String)



class MergeGameViewModel(val context: Context, val sharedPref: SharedPreferences) : ViewModel() {
    var gridItems by mutableStateOf(Array<MergeItem?>(56) { null }); var money by mutableIntStateOf(1000); var energy by mutableIntStateOf(100); var diamonds by mutableIntStateOf(0)
    var playerLevel by mutableIntStateOf(1); var playerXp by mutableIntStateOf(0); var xpToNextLevel by mutableIntStateOf(150);
    val baseEnergyTime = 200; var timerSeconds by mutableIntStateOf(baseEnergyTime);val maxEnergy = 200
    val bubbles = mutableStateListOf<BubbleItem>(); val generatorUsage = mutableStateMapOf<Int, Int>(); val generatorCooldowns = mutableStateMapOf<Int, Long>()
    var discoveredItems = mutableStateListOf<String>(); val dailyTasks = mutableStateListOf<DailyTask>(); val claimedChests = mutableStateListOf<Int>(); val regions = mutableStateListOf<MapRegion>()
    var isStorageOpen by mutableStateOf(false); var isShopOpen by mutableStateOf(false); var isTasksOpen by mutableStateOf(false); var isMapOpen by mutableStateOf(false)
    var showDailyBonus by mutableStateOf(false); var showCollection by mutableStateOf(false); var showLevelAnim by mutableStateOf(false)
    var unlockedChestReward by mutableStateOf<ChestReward?>(null); var unlockedRegionReward by mutableStateOf<MapRegion?>(null)
    var isSpinOpen by mutableStateOf(false); var isSpinning by mutableStateOf(false); var lastSpinDate by mutableStateOf("")
    val spinRewards = listOf("⚡ 50", "💰 500", "💎 10", "🔨 x1", "🧲 x1", "⏰ x1", "⚡ 100", "💎 25")
    var taskProgress by mutableIntStateOf(0); var storageItems = mutableStateListOf<MergeItem>(); var storageCapacity by mutableIntStateOf(1)
    var purchaseCounts by mutableStateOf(mapOf<Int, Int>()); var lastResetDate by mutableStateOf("LOADING"); var lastDailyBonusDate by mutableStateOf("")
    var infoLibrary by mutableStateOf<List<MergeItem>>(emptyList()); var draggingIndex by mutableStateOf<Int?>(null); var dragPositionRoot by mutableStateOf(Offset.Zero)
    var cellBoundsRoot = mutableMapOf<Int, Rect>(); var storageBoundsRoot by mutableStateOf(Rect.Zero); var orders = mutableStateListOf<Order>(); var claimedRewards by mutableStateOf(setOf<Int>())
    var hammerCount by mutableIntStateOf(5); var magnetCount by mutableIntStateOf(5); var clockCount by mutableIntStateOf(5)
    var isPetOpen by mutableStateOf(false)
    var petLevel by mutableIntStateOf(1)
    var petHunger by mutableIntStateOf(100)
    var petXp by mutableIntStateOf(0)
    var hungerTimer by mutableIntStateOf(30) // 30 saniyelik tokluk sayacı
    var showPetLevelUpAnimation by mutableStateOf(false)
    // 2. ViewModel Class'ının içine şunları ekle:
    val petLevelRewards = listOf(
        PetLevelReward(1, 0, 1, "Başlangıç"),      // 0 saniye azaltma
        PetLevelReward(2, 1, 2, "10 Elmas"),      // 5 saniye azaltma (15-5=10)
        PetLevelReward(3, 2, 5, "50 Enerji"),     // 6 saniye azaltma (15-6=9)
        PetLevelReward(4, 3, 7, "20 Elmas"),      // 6 saniye azaltma
        PetLevelReward(5, 4, 8, "100 Enerji"),    // 6 saniye azaltma
        PetLevelReward(6, 5, 9, "50 Elmas"),      // 7 saniye azaltma (15-7=8)
        PetLevelReward(7, 6, 10, "50 Elmas")       // 7 saniye azaltma
    )

    var selectedRegionForBuilding by mutableStateOf<MapRegion?>(null)
    var selectedGeneratorInfo by mutableStateOf<String?>(null)

    var isLibraryMenuOpen by mutableStateOf(false)
    var selectedLibraryIndex by mutableStateOf(0)

    var showChestAnimation by mutableStateOf(false) // Animasyon tetikleyici

    var showSideQuestReward by mutableStateOf(false)
    var currentSideQuestReward by mutableStateOf<ChestReward?>(null)

    // Yan Görevler State'leri
    var isSideQuestsOpen by mutableStateOf(false)
    var activeSideQuests = mutableStateListOf<SideQuest>(
        SideQuest(1, "Hızlı Birleştirici", "10 kez eşleştirme yap", 10, 0, 100, 2, "merge", 1),
        SideQuest(2, "İnşaatçı", "Bahçe Kulübesini 1. Yıldız yap", 1, 0, 500, 10, "upgrade_101", 1)
    )

    // ViewModel'in en üstüne veya uygun bir yere ekle
    private val sideQuestPool = listOf(
        // BİRLEŞTİRME GÖREVLERİ (Merge)
        SideQuest(1, "Hızlı Birleştirici I", "10 kez eşleştirme yap", 10, 0, 100, 2, "merge", 1),
        SideQuest(2, "Hızlı Birleştirici II", "25 kez eşleştirme yap", 25, 0, 250, 5, "merge", 2),
        SideQuest(3, "Hızlı Birleştirici III", "50 kez eşleştirme yap", 50, 0, 500, 10, "merge", 3),

        // BİNA GELİŞTİRME GÖREVLERİ (Upgrade - ID: 101 Bahçe Kulübesi)
        SideQuest(101, "Bahçe Hazırlığı", "Bahçe Kulübesini 1 Yıldız yap", 1, 0, 100, 1, "upgrade_101", 1),
        SideQuest(102, "Bahçe Çırağı", "Bahçe Kulübesini 2 Yıldız yap", 2, 0, 200, 2, "upgrade_101", 2),
        SideQuest(103, "Bahçe Ustası", "Bahçe Kulübesini 3 Yıldız yap", 3, 0, 300, 3, "upgrade_101", 3),
        SideQuest(104, "Bahçe Mimarı", "Bahçe Kulübesini 4 Yıldız yap", 4, 0, 400, 4, "upgrade_101", 4),
        SideQuest(105, "Bahçe Kralı", "Bahçe Kulübesini 5 Yıldız yap", 5, 0, 500, 5, "upgrade_101", 5),

        // BİNA GELİŞTİRME GÖREVLERİ (Upgrade - ID: 201 Bahçe Kulübesi)
        SideQuest(201, "Çiçek Tarhı", "Çiçek Tarhı 1 Yıldız yap", 1, 0, 100, 1, "upgrade_102", 1),
        SideQuest(202, "Çiçek Çırağı", "Çiçek Tarhı 2 Yıldız yap", 2, 0, 200, 2, "upgrade_102", 2),
        SideQuest(203, "Çiçek Ustası", "Çiçek Tarhı 3 Yıldız yap", 3, 0, 300, 3, "upgrade_102", 3),
        SideQuest(204, "Çiçek Mimarı", "Çiçek Tarhı 4 Yıldız yap", 4, 0, 400, 4, "upgrade_102", 4),
        SideQuest(205, "Çiçek Kralı", "Çiçek Tarhı 5 Yıldız yap", 5, 0, 500, 5, "upgrade_102", 5)
    )



    init {
        initRegions()

        // 2. SONRA hafızadaki gerçek seviyeleri (Lvl 3, 5 vs.) binaların üzerine yaz
        loadGame()

        // 3. Yan görev listesini yükle
        loadSideQuests()
// 4. KRİTİK: Binaların gerçek seviyelerini yan görevlere aktar (Eksik olan buydu)
        syncQuestsWithBuildings()

        // Diğer kontroller
       // checkDailyReset()
        // checkDailyBonus() //burayı en son tekrar aç loadgamede var
        restoreMissingGenerators()



       // generateDailyTasks() // Görevleri listeye ekler

        viewModelScope.launch {
            while(true) {
                delay(1000)
                updateEnergy()
            }
        }



    }

    fun syncQuestsWithBuildings() {
        regions.forEach { region ->
            region.buildings.forEach { building ->
                val type = "upgrade_${building.id}"
                val index = activeSideQuests.indexOfFirst { it.type == type }

                if (index != -1) {
                    val quest = activeSideQuests[index]
                    // Eğer bina seviyesi görev seviyesinden büyükse (Örn: Bina 3, Görev 1)
                    // Görevi tamamlanmış (current = target) olarak göster ki ödül alınabilsin.
                    if (quest.level < building.level) {
                        activeSideQuests[index] = quest.copy(currentCount = quest.targetCount)
                    } else if (quest.level == building.level) {
                        // Aynı seviyedelerse gerçek ilerlemeyi yaz
                        activeSideQuests[index] = quest.copy(currentCount = building.level)
                    }
                } else {
                    // Görev listede yoksa (Örn: Çiçek Tarhı yeni açıldıysa) ekle
                    updateQuestProgress(type, building.level)
                }
            }
        }
    }


    // Sadece zaman değiştiğinde rastgele 5 tane seçer
    fun generateDailyTasks() {
        dailyTasks.clear()
        val taskPool = getFullTaskPool()
        val randomTasks = taskPool.shuffled().take(5)
        dailyTasks.addAll(randomTasks)
        // save()
    }

    // Hafızadan yükleme yaparken listeyi hazırlamak için kullanılır
    private fun generateDailyTasksPool() {
        dailyTasks.clear()
        dailyTasks.addAll(getFullTaskPool())
    }

    private fun getFullTaskPool(): List<DailyTask> {
       // dailyTasks.clear()
       // MERGE, GENERATE, CANCEL_ORDER, COMPLETE_ORDER, SPIN_WHEEL, FEED_PET, COLLECT_DIAMOND, USE_HAMMER, USE_MAGNET, UPGRADE_BUILDING, OPEN_CHEST
        return listOf(
            // ELLE GÖREV EKLEME ÖRNEĞİ:
            DailyTask(1, "Birleştirme Yap", TaskType.MERGE, 15, pointValue = 10),
            DailyTask(2, "Eşya Üret", TaskType.GENERATE, 5, pointValue = 10),
            DailyTask(3, "Sipariş Tamamla", TaskType.COMPLETE_ORDER, 1, pointValue = 10),
            DailyTask(4, "Siparişi İptal Et", TaskType.CANCEL_ORDER, 2, pointValue = 10),
            DailyTask(5, "Elmas Topla", TaskType.COLLECT_DIAMOND, 1, pointValue = 10),
            DailyTask(6, "Çekiç Kullan", TaskType.USE_HAMMER, 1, pointValue = 10),
            DailyTask(7, "Çarkı Döndür", TaskType.SPIN_WHEEL, 1, pointValue = 10),
            DailyTask(8, "Ejderhayı Besle", TaskType.FEED_PET, 1, pointValue = 10),
            DailyTask(9, "Mıknatıs Kullan", TaskType.USE_MAGNET, 1, pointValue = 10),
            DailyTask(10, "Bina Yükselt", TaskType.UPGRADE_BUILDING, 1, pointValue = 10),
            DailyTask(11, "Sandık Aç", TaskType.OPEN_CHEST, 1, pointValue = 10)
        )

    }

    // Ödül Alma Fonksiyonu
    fun claimSideQuest(quest: SideQuest) {
        money += quest.rewardMoney
        diamonds += quest.rewardDiamonds
        currentSideQuestReward = ChestReward(money = quest.rewardMoney, diamonds = quest.rewardDiamonds)
        showSideQuestReward = true

        val indexInActive = activeSideQuests.indexOf(quest)
        if (indexInActive != -1) {
            val nextQuestInPool = sideQuestPool.find { it.type == quest.type && it.level == quest.level + 1 }

            if (nextQuestInPool != null) {
                // ÖNEMLİ: Yeni göreve geçerken ilerlemeyi (currentCount) sıfırlama!
                // Binanın gerçek seviyesini koru.
                activeSideQuests[indexInActive] = nextQuestInPool.copy(
                    currentCount = quest.currentCount
                )
            } else {
                activeSideQuests.removeAt(indexInActive)
            }
        }

        // Önce yan görevleri kaydet, sonra genel save yap

        save()
    }



    fun loadSideQuests() {
        // Anahtarı "side_quests_v_final" yaparak temiz bir başlangıç yapalım
        val savedData = sharedPref.getString("side_quests_v_final", null)
        activeSideQuests.clear()

        if (!savedData.isNullOrEmpty()) {
            try {
                val loaded = savedData.split(";").filter { it.isNotBlank() }.mapNotNull { data ->
                    val p = data.split("|")
                    if (p.size >= 9) {
                        SideQuest(
                            id = p[0].toInt(),
                            title = p[1],
                            description = p[2],
                            targetCount = p[3].toInt(),
                            currentCount = p[4].toInt(),
                            rewardMoney = p[5].toInt(),
                            rewardDiamonds = p[6].toInt(),
                            type = p[7],
                            level = p[8].toInt()
                        )
                    } else null
                }
                if (loaded.isNotEmpty()) {
                    activeSideQuests.addAll(loaded)
                    return // Yükleme başarılı, fonksiyondan çık
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Eğer hafıza boşsa veya hata oluştuysa manuel listedeki 1. seviye görevleri ekle
        setDefaultQuests()
    }

    private fun setDefaultQuests() {
        activeSideQuests.clear()

        sideQuestPool.find { it.type == "merge" && it.level == 1 }?.let { activeSideQuests.add(it.copy()) }

        // 2. Bahçe Kulübesi (101) görevini ekle
        sideQuestPool.find { it.type == "upgrade_101" && it.level == 1 }?.let { activeSideQuests.add(it.copy()) }

        // 3. Çiçek Tarhı (102) görevini ekle
        sideQuestPool.find { it.type == "upgrade_102" && it.level == 1 }?.let { activeSideQuests.add(it.copy()) }
    }

    // İlerlemeyi güncelleme fonksiyonunu da buraya ekleyelim (Eksikse)
    fun updateQuestProgress(type: String, progressValue: Int) {
        val index = activeSideQuests.indexOfFirst { it.type == type }

        if (index != -1) {
            val quest = activeSideQuests[index]
            if (type.startsWith("upgrade")) {
                // Eğer bina seviyesi hedeften büyükse, hedefte sabitle (tekrar ödül çıkmasın diye)
                // Ancak claimSideQuest yapıldığında zaten bir sonraki göreve geçecek.
                activeSideQuests[index] = quest.copy(currentCount = progressValue)
            } else {
                val newCount = (quest.currentCount + progressValue).coerceAtMost(quest.targetCount)
                activeSideQuests[index] = quest.copy(currentCount = newCount)
            }
        } else {
            // Eğer görev listede yoksa (Çiçek Tarhı gibi) havuzdan ekle
            if (type.startsWith("upgrade")) {
                val initialQuest = sideQuestPool.find { it.type == type && it.level == 1 }
                if (initialQuest != null) {
                    activeSideQuests.add(initialQuest.copy(currentCount = progressValue))
                }
            }
        }
        save()
    }


    private fun initRegions() {
        regions.clear()
        val r = listOf(
            MapRegion(1, "Ana Bahçe", 0, true, "🏡", "Başlangıç").apply {
                buildings.addAll(listOf(
                    Building(101, "Bahçe Kulübesi", 0, 5, 50, listOf("🏗️", "🛖", "🏠", "🏡", "🏢", "🏰"), "+%1 Elmas Şansı"),
                    Building(102, "Çiçek Tarhı", 0, 5, 100, listOf("🏗️", "🌱", "🌿", "🌸", "🌺", "🌻"), "+%1 Elmas Şansı"),
                    Building(103, "Süs Havuzu", 0, 5, 100, listOf("🏗️", "🧱", "⛲", "💧", "🌊", "⛲"), "-2sn Enerji Süresi"),
                    Building(104, "Bahçe Yolu", 0, 5, 150, listOf("🏗️", "🪵", "🧱", "🛤️", "🛣️", "🏰"), "+%1 Elmas Şansı"),
                    Building(105, "Kuş Yuvası", 0, 5, 150, listOf("🏗️", "🪵", "🛖", "🐦", "🕊️", "🦜"), "-2sn Enerji Süresi"),
                    Building(106, "Piknik Alanı", 0, 5, 200, listOf("🏗️", "🧺", "🥪", "🍎", "🍷", "🧺"), "-2sn Enerji Süresi"),
                    Building(107, "Sera", 0, 5, 200, listOf("🏗️", "🌱", "🌿", "🪴", "🏠", "🌳"), "-2sn Enerji Süresi")
                ))
            },
            MapRegion(2, "Eski Kafe", 50, false, "☕", "+5 Enerji, +500 Altın, +5 Elmas", 5, 500, 5).apply {
                buildings.addAll(listOf(
                    Building(201, "Kahve Tezgahı", 0, 5, 150, listOf("🏗️", "🛖", "🏠", "🏡", "🏢", "☕"), "-2sn Enerji Süresi"),
                    Building(202, "Müşteri Masaları", 0, 5, 200, listOf("🏗️", "🪑", "🪑", "🛋️", "🛋️", "⛱️"), "+%1 Elmas Şansı"),
                    Building(203, "Kahve Çekirdekleri", 0, 5, 250, listOf("🏗️", "🌱", "🌿", "🍒", "🫘", "☕"), "-2sn Enerji Süresi")
                ))
            },
            MapRegion(3, "Liman", 150, false, "⛵", "+10 Enerji, +1000 Altın, +10 Elmas", 10, 1000, 10).apply {
                buildings.addAll(listOf(
                    Building(301, "Balıkçı İskelesi", 0, 5, 300, listOf("🏗️", "🪵", "🛶", "⛵", "🛥️", "🚢"), "+%1 Elmas Şansı"),
                    Building(302, "Deniz Feneri", 0, 5, 350, listOf("🏗️", "🧱", "🗼", "🚨", "🏮", "✨"), "-2sn Enerji Süresi")
                ))
            },
            MapRegion(4, "Eğitim Kampüsü", 500, false, "🎓", "+15 Enerji, +1500 Altın, +15 Elmas", 15, 1500, 15).apply {
                buildings.addAll(listOf(
                    // 1. Kütüphane (4 Ögeli - Elmas Bonusu)
                    Building(401, "Şehir Kütüphanesi", 0, 5, 400, listOf("🏗️", "📜", "📚", "🏛️"), "+%1 Elmas Şansı"),

                    // 2. Okul (5 Ögeli - Enerji Bonusu)
                    Building(402, "Aakdemi", 0, 5, 450, listOf("🏗️", "📝", "🏫", "🏢", "🎓"), "-2sn Enerji Süresi"),

                    // 3. Spor Salonu (5 Ögeli - Enerji Bonusu)
                    Building(403, "Kampüs Spor Salonu", 0, 5, 500, listOf("🏗️", "🧱", "🏀", "🏐", "🏟️"), "-2sn Enerji Süresi"),

                    // 4. Sanat Atölyesi (5 Ögeli - Elmas Bonusu)
                    Building(404, "Sanat Atölyesi", 0, 5, 550, listOf("🏗️", "🎨", "🖌️", "🖼️", "🎭"), "+%1 Elmas Şansı"),

                    // 5. Teknoloji Laboratuvarı (5 Ögeli - Enerji Bonusu)
                    Building(405, "Teknoloji Laboratuvarı", 0, 5, 600, listOf("🏗️", "⚙️", "💻", "🧪", "🚀"), "-2sn Enerji Süresi")
                ))
            },
            MapRegion(5, "Sağlık Merkezi", 1000, false, "🏥", "+20 Enerji, +2000 Altın, +20 Elmas", 20, 2000, 20).apply {
                buildings.addAll(listOf(
                    // 1. Şehir Hastanesi (5 Ögeli - Enerji Bonusu)
// 1. Şehir Hastanesi (6 Ögeli - Enerji Bonusu)
                    Building(501, "Şehir Hastanesi", 0, 5, 500, listOf("🏗️", "🚑", "🏥", "🏢", "🩺", "🏨"), "-2sn Enerji Süresi"),

// 2. Eczane (6 Ögeli - Elmas Bonusu)
                    Building(502, "Eczane", 0, 5, 550, listOf("🏗️", "💊", "🧪", "🩹", "🔬", "✨"), "+%1 Elmas Şansı"),

// 3. Ambulans İstasyonu (6 Ögeli - Enerji Bonusu)
                    Building(503, "Ambulans İstasyonu", 0, 5, 600, listOf("🏗️", "🚨", "🚐", "🚑", "🚁", "🏥"), "-2sn Enerji Süresi"),

// 4. Fizik Tedavi Merkezi (6 Ögeli - Elmas Bonusu)
                    Building(504, "Fizik Tedavi", 0, 5, 650, listOf("🏗️", "🩼", "🚶", "🧘", "💪", "🏡"), "+%1 Elmas Şansı")
                ))
            }
        )
        regions.addAll(r)
    }

    fun buyMarketItem(item: MarketItem) {
        val currentCount = purchaseCounts[item.id] ?: 0
        // Limit 3 ve para yeterliyse
        if (money >= item.price && currentCount < 3) {
            money -= item.price

            // Satın alma sayısını güncelle
            val newCounts = purchaseCounts.toMutableMap()
            newCounts[item.id] = currentCount + 1
            purchaseCounts = newCounts

            // Eşyayı ızgaraya ekle
            val emptyIdx = gridItems.indexOfFirst { it == null }
            if (emptyIdx != -1) {
                gridItems[emptyIdx] = MergeItem(
                    level = 1,
                    emoji = item.emoji,
                    color = Color(0xFFF1C40F)
                )
            }
            save() // Marketten çıkınca sıfırlanmaması için şart
        } else {
            Toast.makeText(context, "Limit doldu veya para yetersiz!", Toast.LENGTH_SHORT).show()
        }
    }


    fun upgradeBuilding(building: Building) {
        val cost = building.baseCost * (building.level + 1)
        if (money >= cost && building.level < building.maxLevel) {
            money -= cost
            val regionIdx = regions.indexOfFirst { it.buildings.contains(building) }
            if (regionIdx != -1) {
                building.level++
                val updatedRegion = regions[regionIdx]
                regions[regionIdx] = updatedRegion.copy()

                when (building.level) {
                    2 -> { hammerCount++; Toast.makeText(context, "Hediye: 🔨 Balyoz!", Toast.LENGTH_SHORT).show() }
                    4 -> { magnetCount++; Toast.makeText(context, "Hediye: 🧲 Mıknatıs!", Toast.LENGTH_SHORT).show() }
                    5 -> { clockCount++; diamonds += 10; Toast.makeText(context, "MAX SEVİYE!", Toast.LENGTH_LONG).show() }
                }
                // --- TEST İÇİN EKLENEN KISIM ---
                // Bina geliştiği an yeni süreyi hesapla ve sayacı o sayıya set et
                val energyBuildingIds = listOf(103, 105, 106, 107, 201, 203, 302, 402, 403, 405, 501, 503)
                if (building.id in energyBuildingIds) {
                    timerSeconds = getMaxEnergyTimer() // Sayacı hemen güncelle
                }

                updateTaskProgress(TaskType.UPGRADE_BUILDING)

                if (building.id == 101) {
                    updateQuestProgress("upgrade_101", building.level)
                }
                updateQuestProgress("upgrade_${building.id}", building.level)
                save() // İlerlemeyi kalıcı olarak kaydet
            }
        }
    }

    fun feedPet(item: MergeItem, index: Int) {
        // Eşyanın seviyesine göre XP ver (Örn: Lvl 1 = 10 XP, Lvl 5 = 50 XP)
        val gainXp = item.level * 10
        petXp += gainXp

        // Tokluk artır
        petHunger = (petHunger + (item.level * 1)).coerceAtMost(100)

        // Eşyayı ızgaradan sil
        gridItems[index] = null

        // Seviye atlama kontrolü (Her 100 XP'de bir seviye)
        if (petXp >= 100) {
            petLevel++; petXp -= 100;
            val reward = petLevelRewards.firstOrNull { it.level == petLevel };
            reward?.let { if (it.instantGift.contains("Elmas")) diamonds += it.instantGift.split(" ")[0].toInt(); if (it.instantGift.contains("Enerji")) energy += it.instantGift.split(" ")[0].toInt() };
            showPetLevelUpAnimation = true; save()
        }
        updateTaskProgress(TaskType.FEED_PET) // Bunu ekle
        save()
    }

    fun checkSpinReady() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) != lastSpinDate
    fun claimSpinReward(idx: Int) {
        val r = spinRewards[idx]; when { r.contains("⚡")->energy+=r.split(" ")[1].toInt(); r.contains("💰")->money+=r.split(" ")[1].toInt(); r.contains("💎")->diamonds+=r.split(" ")[1].toInt(); r.contains("🔨")->hammerCount++; r.contains("🧲")->magnetCount++; r.contains("⏰")->clockCount++ }
        lastSpinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()); updateTaskProgress(TaskType.SPIN_WHEEL) // Bunu ekle
        save()
    }
    fun unlockRegion(reg: MapRegion) { if (diamonds >= reg.unlockCost && !reg.isUnlocked) { diamonds -= reg.unlockCost; regions[regions.indexOfFirst { it.id == reg.id }] = reg.copy(isUnlocked = true); energy += reg.rewardEnergy; money += reg.rewardMoney; diamonds += reg.rewardDiamonds; unlockedRegionReward = reg; save() } }
    fun save() {
        // Yükleme bitmeden veya tarih hatalıyken ASLA kaydetme
        if (lastResetDate == "LOADING" || lastResetDate.isEmpty()) return

        val e = sharedPref.edit()
        e.putInt("money", money)
        e.putInt("energy", energy)
        e.putInt("diamonds", diamonds)
        e.putInt("playerLevel", playerLevel)
        e.putInt("playerXp", playerXp)
        e.putInt("storageCapacity", storageCapacity)
        e.putInt("taskProgress", taskProgress)
        e.putString("lastResetDate", lastResetDate)
        e.putString("lastDailyBonusDate", lastDailyBonusDate)
        e.putString("lastSpinDate", lastSpinDate)
        e.putInt("petLevel", petLevel)
        e.putInt("petHunger", petHunger)
        e.putInt("petXp", petXp)
        e.putInt("hungerTimer", hungerTimer)
        e.putString("discoveredItems", discoveredItems.joinToString(","))
        e.putString("gridData", gridItems.joinToString(";") { SaveManager.serializeItem(it) })
        e.putString("storageData", storageItems.joinToString(";") { SaveManager.serializeItem(it) })
        e.putString("ordersData", orders.joinToString("|") { SaveManager.serializeOrder(it) })
        e.putString("claimedChests", claimedChests.joinToString(","))
        e.putString("unlockedRegions", regions.filter { it.isUnlocked }.map { it.id }.joinToString(","))

        purchaseCounts.forEach { (id, count) ->
            e.putInt("purchaseCount_$id", count)
        }

        e.putStringSet("claimedRewards", claimedRewards.map { it.toString() }.toSet())
        e.putInt("hammerCount", hammerCount)
        e.putInt("magnetCount", magnetCount)
        e.putInt("clockCount", clockCount)


        // 2. Yan Görevleri de AYNI editor içine ekle (Ayrı fonksiyon çağırma)
        val questData = activeSideQuests.joinToString(";") { q ->
            "${q.id}|${q.title}|${q.description}|${q.targetCount}|${q.currentCount}|${q.rewardMoney}|${q.rewardDiamonds}|${q.type}|${q.level}"
        }


        e.putString("side_quests_v_final", questData)

        val tasksData = dailyTasks.joinToString("|") { "${it.id}:${it.currentCount}:${it.isClaimed}" }
        e.putString("dailyTasksData", tasksData)

        val bData = regions.flatMap { r ->
            r.buildings.map { b -> "${r.id}:${b.id}:${b.level}" }
        }.joinToString(",")
        e.putString("buildingsData", bData)

        e.putInt("timerSeconds", timerSeconds)


        e.apply()

    }

    // Mevcut bonuslara göre olması gereken maksimum süreyi hesaplar
    fun getMaxEnergyTimer(): Int {

        val petReduction = currentPetBonus.energyTimer // Ejderhadan gelen indirim (0, 5, 6...)
        val buildingReduction = getEnergyTimeReduction() // Kahve tezgahından gelen indirim (2, 4...)

        // Toplam indirim temel süreden düşülür, minimum 3 saniyeye kadar iner
        return (baseEnergyTime - petReduction - buildingReduction).coerceAtLeast(3)
    }

    //SAATLİK/GÜNLÜK/DAKİKALIK GÖREV GERİ SAYIM SAYACI
    private fun getCurrentTimeKey(): String {
        // Format: Yıl-Ay-Gün-Saat (Örn: 2023-10-27-14)
        //return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) //günlük
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) //saatlik
        //return SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(Date()) //dakikalık
    }


    fun checkLiveTaskReset(): Boolean {
        if (lastResetDate == "LOADING" || lastResetDate.isEmpty()) return false

        val currentKey = getCurrentTimeKey()

        if (lastResetDate != currentKey) {

            performAtomicDailyReset(currentKey)

            return true
        }
        return false
    }


    private fun loadGame() {
        // --- 1. TEMEL VERİLERİ YÜKLE ---
        money = sharedPref.getInt("money", 1000)
        energy = sharedPref.getInt("energy", 20)
        diamonds = sharedPref.getInt("diamonds", 10)
        playerLevel = sharedPref.getInt("playerLevel", 1)
        playerXp = sharedPref.getInt("playerXp", 0)
        storageCapacity = sharedPref.getInt("storageCapacity", 1)
        lastDailyBonusDate = sharedPref.getString("lastDailyBonusDate", "") ?: ""
        lastSpinDate = sharedPref.getString("lastSpinDate", "") ?: ""

        // --- 2. IZGARA VE BİNALAR (Resetten önce yüklenmeli ki save() bunları sıfırlamasın) ---
        gridItems = GameLogic.loadGrid(sharedPref)
        storageItems.clear()
        sharedPref.getString("storageData", null)?.split(";")?.forEach {
            SaveManager.deserializeItem(it)?.let { item -> storageItems.add(item) }
        }

        val bData = sharedPref.getString("buildingsData", "") ?: ""
        if (bData.isNotEmpty()) {
            bData.split(",").forEach { data ->
                val parts = data.split(":")
                if (parts.size == 3) {
                    val rId = parts[0].toIntOrNull(); val bId = parts[1].toIntOrNull(); val lvl = parts[2].toIntOrNull() ?: 0
                    regions.find { it.id == rId }?.buildings?.find { it.id == bId }?.let { it.level = lvl }
                }
            }
        }

        // --- 3. GÜNLÜK RESET KONTROLÜ ---
        val currentTimeKey = getCurrentTimeKey()
        val savedDate = sharedPref.getString("lastResetDate", "") ?: ""
        val savedTasksData = sharedPref.getString("dailyTasksData", "") ?: ""

        // KRİTİK: LOADING kilidini hemen açıyoruz
        lastResetDate = if (savedDate.isEmpty()) currentTimeKey else savedDate

        if (savedDate == currentTimeKey && savedTasksData.isNotEmpty()) {
            // AYNI GÜN: Verileri yükle
            taskProgress = sharedPref.getInt("taskProgress", 0)

            val c = sharedPref.getString("claimedChests", "") ?: ""
            claimedChests.clear()
            if (c.isNotEmpty()) claimedChests.addAll(c.split(",").mapNotNull { it.toIntOrNull() })

            dailyTasks.clear()
            val pool = getFullTaskPool()
            savedTasksData.split("|").forEach { data ->
                val parts = data.split(":")
                if (parts.size == 3) {
                    val id = parts[0].toIntOrNull(); val count = parts[1].toIntOrNull() ?: 0; val isClaimed = parts[2].toBoolean()
                    pool.find { it.id == id }?.let { dailyTasks.add(it.copy(currentCount = count, isClaimed = isClaimed)) }
                }
            }
        } else {

            performAtomicDailyReset(currentTimeKey)

        }

        // --- 4. DİĞER TÜM YÜKLEMELER ---
        petLevel = sharedPref.getInt("petLevel", 1)
        petHunger = sharedPref.getInt("petHunger", 100)
        petXp = sharedPref.getInt("petXp", 0)
        hungerTimer = sharedPref.getInt("hungerTimer", 30)

        val d = sharedPref.getString("discoveredItems", "") ?: ""
        discoveredItems.clear()
        if (d.isNotEmpty()) discoveredItems.addAll(d.split(","))

        val u = sharedPref.getString("unlockedRegions", "1") ?: "1"
        val unlockedIds = u.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        for (i in regions.indices) {
            if (unlockedIds.contains(regions[i].id)) regions[i] = regions[i].copy(isUnlocked = true)
        }

        orders.clear()
        orders.addAll(GameLogic.loadOrders(sharedPref, playerLevel))
        xpToNextLevel = (playerLevel * 150) + (playerLevel * playerLevel * 25)

        claimedRewards = (sharedPref.getStringSet("claimedRewards", emptySet()) ?: emptySet())
            .mapNotNull { it.toIntOrNull() }.toSet()

        hammerCount = sharedPref.getInt("hammerCount", 5)
        magnetCount = sharedPref.getInt("magnetCount", 5)
        clockCount = sharedPref.getInt("clockCount", 5)
        timerSeconds = sharedPref.getInt("timerSeconds", getMaxEnergyTimer())

        val newPurchaseCounts = mutableMapOf<Int, Int>()
        for (i in 1..20) {
            val count = sharedPref.getInt("purchaseCount_$i", 0)
            if (count > 0) newPurchaseCounts[i] = count
        }
        purchaseCounts = newPurchaseCounts

        restoreMissingGenerators()
        discoverFirstItemsOfGenerators()
        // checkDailyBonus() //burayı tekrar aç inittede var
        loadSideQuests()
    }
    // MergeGameViewModel.kt içine bu fonksiyonu yapıştır:





    private fun restoreMissingGenerators() {
        // gridItems'ın bir kopyasını alıyoruz (Compose'un değişikliği anlaması için şart!)
        val currentGrid = gridItems.copyOf()
        var isChanged = false

        // Oyuncunun seviyesine kadar olması gereken tüm jeneratörleri tara
        for (lvl in 1..playerLevel) {
            // Senin oyun mantığına göre jeneratörler hangi seviyelerde geliyorsa (Örn: her seviye)
            val targetGenType = lvl

            if (targetGenType > GameData.genEmojis.size) continue

            // 1. Izgarada var mı? 2. Depoda var mı?
            val existsInGrid = currentGrid.any { it?.isGenerator == true && it.genType == targetGenType }
            val existsInStorage = storageItems.any { it.isGenerator && it.genType == targetGenType }

            // Eğer hiçbir yerde yoksa (balon silmişse veya hata oluşmuşsa)
            if (!existsInGrid && !existsInStorage) {
                val emptyIdx = currentGrid.indexOfFirst { it == null }
                if (emptyIdx != -1) {
                    // Jeneratörü oluştur ve yerleştir
                    currentGrid[emptyIdx] = MergeItem(
                        level = 1,
                        emoji = GameData.genEmojis[targetGenType - 1],
                        color = Color(0xFFF39C12),
                        isGenerator = true,
                        genType = targetGenType
                    )
                    isChanged = true
                }
            }
        }

        if (isChanged) {
            gridItems = currentGrid // Ekrana yansıması için state'i güncelle
            save() // Kalıcı olarak kaydet
        }
    }



    private fun discoverFirstItemsOfGenerators() { gridItems.filterNotNull().forEach { if (it.isGenerator) { val first = GameData.allLibraries[(it.genType - 1).coerceIn(0, GameData.allLibraries.size - 1)][0]; if (!discoveredItems.contains(first.emoji)) discoveredItems.add(first.emoji) } }; save() }

    fun updateTaskProgress(t: TaskType) {
        dailyTasks.forEach { task ->
            if (task.type == t && task.currentCount < task.targetCount) {
                task.currentCount++
                if (task.currentCount == task.targetCount) {
                    taskProgress += task.pointValue
                }
            }
        }
        save()
    }



    fun openChest(m: Int) {
        // Eğer puan yetiyorsa ve daha önce alınmadıysa
        if (taskProgress >= m && !claimedChests.contains(m)) {
            val reward = when(m) {
                20  -> ChestReward(money = 200, energy = 10, diamonds = 5)
                40  -> ChestReward(money = 400, energy = 20, diamonds = 10, specialItemEmoji = "💎")
                60  -> ChestReward(money = 600, energy = 30, diamonds = 15, specialItemEmoji = "🎁")
                80  -> ChestReward(money = 800, energy = 40, diamonds = 25, specialItemEmoji = "🏆")
                90  -> ChestReward(money = 1200, energy = 60, diamonds = 50)
                100 -> ChestReward(money = 2500, energy = 100, diamonds = 100, specialItemEmoji = "👑")
                else -> ChestReward(money = 50)
            }

            // Ödülleri ekle
            money += reward.money
            energy += reward.energy
            diamonds += reward.diamonds

            // Özel eşya varsa ızgaraya koy
            reward.specialItemEmoji?.let { emoji ->
                val emptyIdx = gridItems.indexOfFirst { it == null }
                if (emptyIdx != -1) {
                    gridItems[emptyIdx] = MergeItem(level = 10, emoji = emoji, color = Color.Magenta)
                }
            }

            // Listeye ekle ve kaydet
            claimedChests.add(m)
            unlockedChestReward = reward // UI'da popup göstermek için

            updateTaskProgress(TaskType.OPEN_CHEST) // Varsa görevi ilerlet
            save()
        }
    }

    fun onDragEnd() {
        val dragIdx = draggingIndex ?: return
        GameLogic.handleMergeOrMove(dragIdx, dragPositionRoot, gridItems, cellBoundsRoot, isStorageOpen, storageBoundsRoot, storageItems, storageCapacity)?.let { res ->
            val targetIdx = cellBoundsRoot.entries.find { it.value.contains(dragPositionRoot) }?.key ?: -1
            if (res.xpGain > 0) {
                updateTaskProgress(TaskType.MERGE)
                updateQuestProgress("merge", 1)
                // 1. Ejderhadan gelen şansı al (Örn: %20)
                val currentReward = petLevelRewards.find { it.level == petLevel } ?: petLevelRewards.last()
                val petChance = currentReward.diamondChance // Bu değer petLevelRewards listenden gelir

// 2. Şehir geliştirmeden gelen ek şansı al
                val buildingBonus = getTotalDiamondBonusChance()

// 3. MANUEL KONTROL: İstersen buraya sabit bir ekleme de yapabilirsin
                val manualExtraBoost = 0 // Buraya ne yazarsan direkt yüzde olarak eklenir

// TOPLAM ŞANS
                val totalChance = getTotalDiamondChanceDisplay() // Artık yukarıdaki fonksiyondan alıyor

                if (Random.nextInt(100) < totalChance) {
                    res.newGrid.indexOfFirst { it == null }.takeIf { it != -1 }?.let {
                        res.newGrid[it] = MergeItem(1, "💎", Color(0xFF4FC3F7))
                    }
                }

                // Sadece jeneratör OLMAYAN eşyalar balon çıkarabilir
                if (Random.nextInt(100) < 10) { // Şans kontrolü
                    val merged = if (targetIdx != -1) res.newGrid[targetIdx] else null
                    if (merged != null && !merged.isGenerator) {
                        // Boş hücreyi bulurken:
                        // 1. Izgara boş olmalı (null)
                        // 2. O hücrede hali hazırda başka bir balon olmamalı
                        val emptyIdx = res.newGrid.indices.firstOrNull { idx ->
                            res.newGrid[idx] == null && bubbles.none { it.index == idx }
                        }

                        if (emptyIdx != null) {
                            bubbles.add(BubbleItem(merged, emptyIdx))
                        }
                    }
                }
            }
            gridItems = res.newGrid; playerXp += res.xpGain; if (targetIdx != -1) addToCollection(gridItems[targetIdx]); checkLevelUp()
        }; draggingIndex = null; save()
    }

    fun collectDiamond(idx: Int) { gridItems[idx]?.let { if (it.emoji.contains("💎")) { diamonds += when(it.level) { 1->2; 2->6; 3->12; 4->25; else->2 }; gridItems[idx] = null;updateTaskProgress(TaskType.COLLECT_DIAMOND); save() } } }

    // UI'da elmas ikonunun yanında gösterilecek toplam yüzdeyi hesaplar
    fun getTotalDiamondChanceDisplay(): Int {
        val petChance = currentPetBonus.diamondChance
        val buildingBonus = getTotalDiamondBonusChance()
        val manualExtraBoost = 0 // Eğer manuel bir boost eklediysen buraya yazabilirsin
        return petChance + buildingBonus + manualExtraBoost
    }



    fun useGenerator(idx: Int) {
        val gen = gridItems[idx] ?: return

        // 1. Temel Kontroller (Jeneratör mü? Bekleme süresinde mi? Enerji var mı?)
        if (!gen.isGenerator || !isGeneratorReady(idx) || energy <= 0) return

        // 2. Boş yer kontrolü
        val empty = gridItems.indexOfFirst { it == null }.takeIf { it != -1 } ?: return

        // 3. Enerji tüketimi
        energy--

        // 4. Eşya üretme mantığı
        // GameData içindeki kütüphanelerden jeneratörün emojisine uygun olanı bulur ve ilk seviye eşyayı üretir.
        GameData.allLibraries.find { lib -> lib.any { it.emoji == gen.emoji } }?.firstOrNull()?.let { spawned ->
            gridItems[empty] = spawned.copy(isLocked = false, isGenerator = false)
            addToCollection(spawned)
        }

        // 5. Kullanım sayacı ve Cooldown (Bekleme Süresi) yönetimi
        val usage = (generatorUsage[idx] ?: 0) + 1
        updateTaskProgress(TaskType.GENERATE)

        if (usage >= 20) {
            // 20 kullanımda bir 60 saniye soğuma süresine girer
            generatorUsage[idx] = 0
            generatorCooldowns[idx] = System.currentTimeMillis() + 60000
        } else {
            generatorUsage[idx] = usage
        }

        save()
    }



    fun onOrderComplete(o: Order) {
        GameLogic.processOrder(o, gridItems, playerLevel)?.let {
            res -> updateTaskProgress(TaskType.COMPLETE_ORDER);
            if (o.reward >= 500) diamonds += Random.nextInt(2, 6);
            if (Random.nextInt(100) < 20) when (Random.nextInt(3)) { 0->hammerCount++; 1->magnetCount++; 2->clockCount++ };
            gridItems = res.newGrid;
            money += o.reward;
            playerXp += res.xpGain;
            orders.remove(o);
            orders.add(GameLogic.generateOrder(Random.nextInt(10000), playerLevel));
            checkLevelUp();
            save()
        }
    }

    fun onOrderCancel(o: Order) { updateTaskProgress(TaskType.CANCEL_ORDER); orders.remove(o); orders.add(GameLogic.generateOrder(Random.nextInt(10000), playerLevel)); save() }
    // fun checkDailyReset() { val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()); if (today != lastResetDate) { purchaseCounts = emptyMap(); lastResetDate = today; generateDailyTasks(); claimedChests.clear(); taskProgress = 0; save() } }
    fun checkDailyBonus() { if (SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) != lastDailyBonusDate) showDailyBonus = true }

    fun claimDailyBonus() {
        if (showDailyBonus) {
            money += 200
            diamonds += 5
           // energy = (energy + 50).coerceAtMost(maxEnergy + 100)
            energy += 50

            // Tarihi bugüne set et
            lastDailyBonusDate = getCurrentTimeKey()

            showDailyBonus = false
            showChestAnimation = true

            // ANINDA KAYIT: Tarihi diske yaz ki bir daha çıkmasın
            save()

            viewModelScope.launch {
                delay(4000)
                showChestAnimation = false
            }
        }
    }


    fun isGeneratorReady(idx: Int) = System.currentTimeMillis() >= (generatorCooldowns[idx] ?: 0L)
    fun checkLevelUp() { if (playerXp >= xpToNextLevel) { playerXp -= xpToNextLevel; playerLevel++; diamonds += 10; xpToNextLevel = (playerLevel * 150) + (playerLevel * playerLevel * 25); showLevelAnim = true; gridItems = GameLogic.handleLevelUp(gridItems, playerLevel); discoverFirstItemsOfGenerators(); save() } }




    fun updateEnergy() {
        // GÜVENLİK: Yükleme bitmeden hiçbir mantık çalışmasın
        if (lastResetDate == "LOADING") return

        var needsSave = false // Kayıt gerekli mi bayrağı

        // 1. Saatlik Görev Kontrolü
        if (checkLiveTaskReset()) {
            needsSave = true
        }

        // 2. Enerji Yenilenmesi
        if (energy < maxEnergy) {
            if (timerSeconds > 0) {
                timerSeconds--
            } else {
                energy++
                timerSeconds = getMaxEnergyTimer()
                needsSave = true // Enerji arttı, kaydetmemiz gerekecek
            }
        }

        // 3. Pet Açlık Kontrolü
        if (petHunger > 0) {
            if (hungerTimer > 0) {
                hungerTimer--
            } else {
                petHunger = (petHunger - 2).coerceAtLeast(0)
                hungerTimer = 30
                needsSave = true // Pet durumu değişti, kaydetmemiz gerekecek
            }
        }

        // 4. Balonların Süresi
        val it = bubbles.iterator()
        while (it.hasNext()) {
            val b = it.next()
            b.timeLeft--
            if (b.timeLeft <= 0) {
                it.remove()
                needsSave = true // Balon silindi, kaydetmemiz gerekecek
            }
        }

        // --- FİNAL: Eğer yukarıdaki işlemlerden herhangi biri "true" yaptıysa tek bir kayıt yap ---
        if (needsSave) {
            save()
        }
    }

    val currentPetBonus get() = petLevelRewards.find { it.level == petLevel } ?: petLevelRewards.last()
    fun getNextPetBonus() = petLevelRewards.find { it.level == petLevel + 1 }
    fun getPetBonusDescription(): String = "⚡ Enerji: ${currentPetBonus.energyTimer}sn | 💎 Elmas: %${currentPetBonus.diamondChance}"

    // --- KALICI BONUS HESAPLAYICILAR ---
    fun getEnergyTimeReduction(): Int {
        val energyBuildingIds = listOf(103, 105, 106, 107, 201, 203, 302, 402, 403, 405, 501, 503)
        val totalLevel = regions.flatMap { it.buildings }
            .filter { it.id in energyBuildingIds }
            .sumOf { it.level }
        return totalLevel * 2 // Her seviye için 2 saniye
    }

    fun getTotalDiamondBonusChance(): Int {
        val energyBuildingIds = listOf(103, 105, 106, 107, 201, 203, 302, 402, 403, 405, 501, 503)
        val totalBuildingStars = regions.flatMap { it.buildings }
            .filter { it.id !in energyBuildingIds }
            .sumOf { it.level }
        return totalBuildingStars // Her seviye %1 şans
    }

    fun addToCollection(item: MergeItem?) { if (item != null && !item.isLocked && !item.isGenerator && !discoveredItems.contains(item.emoji)) { discoveredItems.add(item.emoji); save() } }
    fun useHammer(idx: Int) { if (hammerCount > 0 && gridItems[idx] != null) {updateTaskProgress(TaskType.USE_HAMMER); gridItems[idx] = null; hammerCount--; save() } }
    fun useClock() { if (clockCount > 0) { generatorCooldowns.clear(); clockCount--; save() } }
    fun useMagnet() {
        if (magnetCount <= 0) return
        var merged = false; for (i in gridItems.indices) { val a = gridItems[i] ?: continue; if (a.isLocked || a.isGenerator) continue; for (j in (i + 1) until gridItems.size) { val b = gridItems[j] ?: continue; if (a.emoji == b.emoji && !b.isLocked && a.level < 10) { GameLogic.getNextLevelItem(a)?.let { next -> gridItems[i] = next; gridItems[j] = null; addToCollection(next); playerXp += (a.level * 2); merged = true }; if (merged) break } }; if (merged) break }
        if (merged) { updateTaskProgress(TaskType.USE_MAGNET); magnetCount--; updateTaskProgress(TaskType.MERGE); checkLevelUp(); save() }
    }
    fun onBuyEnergy(price: Int, amount: Int, id: Int) {
        val currentCount = purchaseCounts[id] ?: 0

        // 1. Kontrol: Para yetiyor mu? Limit doldu mu? Enerji zaten full mü?
        if (money >= price && currentCount < 3) {
            money -= price
            //energy = (energy + amount).coerceAtMost(maxEnergy + 100)
            energy = energy + amount

            // 2. KRİTİK NOKTA: purchaseCounts bir State olduğu için
            // içeriğini toMutableMap ile kopyalayıp yeni bir Map atamalıyız.
            val newCounts = purchaseCounts.toMutableMap()
            newCounts[id] = currentCount + 1
            purchaseCounts = newCounts

            // 3. Değişikliği anında kaydet
            save()
        } else {
            Toast.makeText(context, "Limit doldu veya para yetersiz!", Toast.LENGTH_SHORT).show()
        }
    }
    fun buyBooster(t: String, c: Int) { if (money >= c) { money -= c; when (t) { "hammer"->hammerCount++; "magnet"->magnetCount++; "clock"->clockCount++ }; save() } }
    fun claimCategoryReward(idx: Int) {
        if (!claimedRewards.contains(idx)) {
            claimedRewards = claimedRewards.toMutableSet().apply { add(idx) };
            diamonds += 10;
           // energy = (energy + 25).coerceAtMost(maxEnergy + 50); save()
            energy += 25
        } }

    // 1. Yeni Atomik Reset Fonksiyonu
    private fun performAtomicDailyReset(newKey: String) {
        // Önce kritik değişkenleri güncelle
        lastResetDate = newKey
        taskProgress = 0
        claimedChests.clear()

        // Günlük görevleri yeniden oluştur
        generateDailyTasks()

        // Market limitlerini sıfırla (İsteğe bağlı, istersen kalsın)
        purchaseCounts = emptyMap()

        // KRİTİK: Tüm değerleri (Para, Elmas, Enerji dahil) o anki haliyle diske mühürle
        save()
    }


}