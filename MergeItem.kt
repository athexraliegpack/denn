package com.example.game
import androidx.compose.ui.graphics.Color

data class MergeItem(
    val level: Int,
    val emoji: String,
    val color: Color,
    val isGenerator: Boolean = false,
    val isLocked: Boolean = false,
    val genType: Int = 0,
    val itemGroup: Int = 0
)

data class Order(
    val id: Int,
    val requiredItems: List<MergeItem>,
    val reward: Int
)