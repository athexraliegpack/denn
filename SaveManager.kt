package com.example.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object SaveManager {
    // Eşyaları kaydetmek için metne dönüştürür
    fun serializeItem(item: MergeItem?): String {
        if (item == null) return "null"
        return "${item.level}|${item.emoji}|${item.color.toArgb()}|${item.isGenerator}|${item.isLocked}|${item.genType}|${item.itemGroup}"
    }

    // Kayıtlı metni tekrar eşya objesine dönüştürür
    fun deserializeItem(data: String): MergeItem? {
        try {
            if (data == "null" || data.isBlank()) return null
            val parts = data.split("|")
            if (parts.size < 7) return null
            return MergeItem(
                level = parts[0].toIntOrNull() ?: 1,
                emoji = parts[1],
                color = Color(parts[2].toIntOrNull() ?: -1),
                isGenerator = parts[3].toBoolean(),
                isLocked = parts[4].toBoolean(),
                genType = parts[5].toIntOrNull() ?: 0,
                itemGroup = parts[6].toIntOrNull() ?: 0
            )
        } catch (e: Exception) {
            return null
        }
    }

    // Siparişleri kaydetmek için metne dönüştürür
    fun serializeOrder(order: Order): String {
        val itemsStr = order.requiredItems.joinToString("*") { it.emoji }
        return "${order.id}^${order.reward}^$itemsStr"
    }

    // Kayıtlı metni tekrar sipariş objesine dönüştürür
    fun deserializeOrder(data: String): Order? {
        return try {
            val parts = data.split("^")
            val id = parts[0].toInt()
            val reward = parts[1].toInt()
            val items = parts[2].split("*").map { emoji ->
                // Sipariş eşyaları görsel amaçlıdır, temel özellikleri yeterlidir
                MergeItem(level = 1, emoji = emoji, color = Color.White)
            }
            Order(id, items, reward)
        } catch (e: Exception) { null }
    }
}