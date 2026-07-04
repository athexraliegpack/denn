package com.example.game

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.random.Random
data class MergeResult(val newGrid: Array<MergeItem?>, val xpGain: Int = 0, val addedToStorage: Boolean = false, val diamondGain: Int = 0)
data class OrderResult(val newGrid: Array<MergeItem?>, val xpGain: Int, val diamondGain: Int = 0)
object GameLogic {
    fun playSound(context: Context, resId: Int) {
        try {
            val mp = MediaPlayer.create(context, resId)
            mp?.setOnCompletionListener { it.release() }
            mp?.start()
        } catch (e: Exception) {}
    }

    fun canFulfillOrder(order: Order, grid: Array<MergeItem?>): Boolean {
        val availableItems = grid.filterNotNull().filter { !it.isLocked }.toMutableList()
        for (reqItem in order.requiredItems) {
            val foundIdx = availableItems.indexOfFirst { it.emoji == reqItem.emoji }
            if (foundIdx != -1) { availableItems.removeAt(foundIdx) } else { return false }
        }
        return true
    }

    fun generateOrder(id: Int, level: Int): Order {
        val count = if (level < 4) (1..2).random() else (2..3).random()
        val items = List(count) {
            val lib = GameData.allLibraries.take(level.coerceAtMost(8)).random()
            val maxLvl = if (level < 5) 3 else 5
            lib[(0 until maxLvl.coerceAtMost(lib.size)).random()]
        }
        val reward = items.sumOf { it.level } * 30 + (count * 15)
        return Order(id, items, reward)
    }

    fun loadOrders(prefs: SharedPreferences, level: Int): List<Order> {
        val saved = prefs.getString("ordersData", null)
        if (!saved.isNullOrEmpty()) {
            val list = saved.split("|").mapNotNull { SaveManager.deserializeOrder(it) }
            if (list.isNotEmpty()) return list
        }
        return List(5) { generateOrder(Random.nextInt(10000), level) }
    }





    fun handleMergeOrMove(
        sourceIdx: Int,
        dragPos: Offset,
        grid: Array<MergeItem?>,
        cellBounds: Map<Int, Rect>,
        isStorageOpen: Boolean,
        storageBounds: Rect,
        storageItems: MutableList<MergeItem>,
        storageCap: Int
    ): MergeResult? {
        val sourceItem = grid[sourceIdx] ?: return null
        if (isStorageOpen && storageBounds.contains(dragPos)) {
            if (storageItems.size < storageCap && !sourceItem.isGenerator) {
                storageItems.add(sourceItem)
                val newGrid = grid.copyOf(); newGrid[sourceIdx] = null
                return MergeResult(newGrid, addedToStorage = true)
            }
            return null
        }
        val targetIdx = cellBounds.entries.find { it.value.contains(dragPos) }?.key ?: return null
        if (targetIdx == sourceIdx) return null
        val targetItem = grid[targetIdx]; val newGrid = grid.copyOf()
        if (targetItem == null) {
            newGrid[targetIdx] = sourceItem; newGrid[sourceIdx] = null
            return MergeResult(newGrid)
        } else if (targetItem.emoji == sourceItem.emoji && targetItem.level == sourceItem.level) {
            if (targetItem.isLocked) {
                newGrid[targetIdx] = targetItem.copy(isLocked = false); newGrid[sourceIdx] = null
                unlockNeighbors(targetIdx, newGrid)
                return MergeResult(newGrid, 5)
            } else if (!sourceItem.isGenerator) {
                // ELMAS BİRLEŞTİRME MANTIĞI
                if (sourceItem.emoji.contains("💎")) {
                    if (sourceItem.level < 4) {
                        newGrid[targetIdx] = MergeItem(sourceItem.level + 1, "💎", Color(0xFF4FC3F7))
                        newGrid[sourceIdx] = null
                        return MergeResult(newGrid, sourceItem.level * 10)
                    }
                    return null
                }
                // NORMAL EŞYA BİRLEŞTİRME MANTIĞI
                val lib = GameData.allLibraries.find { it.any { i -> i.emoji == targetItem.emoji } } ?: GameData.foodLib
                if (targetItem.level < lib.size) {
                    newGrid[targetIdx] = lib[targetItem.level]; newGrid[sourceIdx] = null
                    unlockNeighbors(targetIdx, newGrid)
                    return MergeResult(newGrid, targetItem.level * 8)
                }
            }
        }
        return null
    }

    private fun unlockNeighbors(index: Int, grid: Array<MergeItem?>) {
        val row = index / 7
        val col = index % 7
        val neighbors = listOf(
            index - 7, index + 7,
            if (col > 0) index - 1 else -1,
            if (col < 6) index + 1 else -1
        )
        for (nIdx in neighbors) {
            if (nIdx in 0 until 56) {
                val item = grid[nIdx]
                if (item != null && item.isLocked) { grid[nIdx] = item.copy(isLocked = false) }
            }
        }
    }

    fun processOrder(order: Order, grid: Array<MergeItem?>, level: Int): OrderResult? {
        val tempGrid = grid.toMutableList()
        val indicesToRemove = mutableListOf<Int>()
        for (reqItem in order.requiredItems) {
            val foundIdx = tempGrid.indexOfFirst { it?.emoji == reqItem.emoji && !it.isLocked }
            if (foundIdx != -1) { indicesToRemove.add(foundIdx); tempGrid[foundIdx] = null } else return null
        }
        val newGrid = grid.copyOf()
        indicesToRemove.forEach { newGrid[it] = null }
        val diamondGain = if (order.reward >= 500) Random.nextInt(2, 6) else 0
        return OrderResult(newGrid, order.requiredItems.sumOf { it.level } * 12, diamondGain = diamondGain)
    }

    // GameLogic.kt içindeki ilgili kısımları bunlarla değiştir:

    fun loadGrid(prefs: SharedPreferences): Array<MergeItem?> {
        val saved = prefs.getString("gridData", null)
        if (saved != null) {
            val items = saved.split(";").map { SaveManager.deserializeItem(it) }.toTypedArray()
            if (items.size == 56) return items
        }

        val grid = arrayOfNulls<MergeItem>(56)
        for (i in 0 until 56) {
            val row = i / 7
            val col = i % 7
            val isMiddle = row in 2..6 && col in 1..5

            if (i == 24) {
                // İLK JENERATÖR: genType 1 olarak işaretlenmeli
                grid[i] = MergeItem(1, GameData.genEmojis[0], Color(0xFF9575CD), isGenerator = true, genType = 1)
            } else if (!isMiddle) {
                val randomLib = GameData.allLibraries.random()
                grid[i] = randomLib[0].copy(isLocked = true)
            }
        }
        return grid
    }

    fun handleLevelUp(grid: Array<MergeItem?>, level: Int): Array<MergeItem?> {
        val newGrid = grid.copyOf()
        // Seviye 1-8 arasındaysa ve o seviyenin jeneratörü henüz yoksa ekle
        if (level <= GameData.genEmojis.size) {
            val exists = newGrid.any { it?.isGenerator == true && it.genType == level }
            if (!exists) {
                val emptyIdx = newGrid.indexOfFirst { it == null }
                if (emptyIdx != -1) {
                    newGrid[emptyIdx] = MergeItem(
                        level = 1,
                        emoji = GameData.genEmojis[level - 1],
                        color = Color(0xFFF39C12),
                        isGenerator = true,
                        genType = level
                    )
                }
            }
        }
        return newGrid
    }

    fun getNextLevelItem(item: MergeItem): MergeItem? {
        if (item.emoji.contains("💎")) {
            if (item.level < 4) return MergeItem(item.level + 1, "💎", Color(0xFF4FC3F7))
            return null
        }
        val library = GameData.allLibraries.find { lib -> lib.any { i -> i.emoji == item.emoji } }
        if (library != null) {
            val currentIndex = library.indexOfFirst { it.emoji == item.emoji }
            if (currentIndex != -1 && currentIndex < library.size - 1) { return library[currentIndex + 1] }
        }
        return null
    }
}