package com.example.game
import androidx.compose.ui.graphics.Color

object GameData {

    // GameData.kt içine ekle
    val libraryNames = listOf(
        "Manav 🍎", "Hırdavat 🔨", "Gardırop 👕", "Oyuncakçı 🧸",
        "Spor Salonu ⚽", "Teknoloji 📱", "Bahçe 🌱", "Pet Shop 🦴"
    )


    val foodLib = listOf(
        MergeItem(1, "🍎", Color(0xFFFFCDD2)), MergeItem(2, "🍏", Color(0xFFC8E6C9)),
        MergeItem(3, "🍐", Color(0xFFDCEDC8)), MergeItem(4, "🍊", Color(0xFFFFE0B2)),
        MergeItem(5, "🍋", Color(0xFFFFF9C4)), MergeItem(6, "🍌", Color(0xFFFFF176)),
        MergeItem(7, "🍉", Color(0xFFFF8A80)), MergeItem(8, "🍇", Color(0xFFE1BEE7))
    )
    val toolLib = listOf(
        MergeItem(1, "🔨", Color(0xFFD1C4E9)), MergeItem(2, "🪚", Color(0xFFB3E5FC)),
        MergeItem(3, "🔧", Color(0xFFB2DFDB)), MergeItem(4, "🪛", Color(0xFFF0F4C3)),
        MergeItem(5, "🪓", Color(0xFFFFCCBC)), MergeItem(6, "⛏️", Color(0xFFCFD8DC))
    )
    val clothLib = listOf(
        MergeItem(1, "👕", Color(0xFFBBDEFB)), MergeItem(2, "👖", Color(0xFF90CAF9)),
        MergeItem(3, "👗", Color(0xFFF48FB1)), MergeItem(4, "👘", Color(0xFFCE93D8)),
        MergeItem(5, "🧥", Color(0xFFBCAAA4)), MergeItem(6, "👔", Color(0xFFB0BEC5)),
        MergeItem(7, "🎓", Color(0xFFB39DDB))
    )
    val toyLib = listOf(
        MergeItem(1, "🧸", Color(0xFFFFF59D)), MergeItem(2, "🎈", Color(0xFFFFAB91)),
        MergeItem(3, "🪁", Color(0xFFA5D6A7)), MergeItem(4, "🎲", Color(0xFF80CBC4)),
        MergeItem(5, "🎮", Color(0xFF9FA8DA))
    )
    val sportLib = listOf(
        MergeItem(1, "⚽", Color(0xFFEEEEEE)), MergeItem(2, "🏀", Color(0xFFFFCC80)),
        MergeItem(3, "🎾", Color(0xFFE6EE9C)), MergeItem(4, "🏐", Color(0xFFFFF59D)),
        MergeItem(5, "🏈", Color(0xFFA1887F)), MergeItem(6, "🥊", Color(0xFFEF9A9A)),
        MergeItem(7, "🏆", Color(0xFFFFF176)), MergeItem(8, "🥇", Color(0xFFFFD54F))
    )
    val techLib = listOf(
        MergeItem(1, "📱", Color(0xFFE0E0E0)), MergeItem(2, "💻", Color(0xFFB0BEC5)),
        MergeItem(3, "📷", Color(0xFF90A4AE)), MergeItem(4, "🎧", Color(0xFF7986CB)),
        MergeItem(5, "⌚", Color(0xFF4DB6AC))
    )
    val gardenLib = listOf(
        MergeItem(1, "🌱", Color(0xFFA5D6A7)), MergeItem(2, "🪴", Color(0xFF81C784)),
        MergeItem(3, "🌻", Color(0xFFFFF176)), MergeItem(4, "🌳", Color(0xFF66BB6A)),
        MergeItem(5, "🚜", Color(0xFFFFD54F))
    )
    val petLib = listOf(
        MergeItem(1, "🦴", Color(0xFFD7CCC8)), MergeItem(2, "🧶", Color(0xFFF48FB1)),
        MergeItem(3, "🏠", Color(0xFFFFE0B2)), MergeItem(4, "🐱", Color(0xFFFFCC80)),
        MergeItem(5, "🐶", Color(0xFFA1887F))
    )

    val allLibraries = listOf(foodLib, toolLib, clothLib, toyLib, sportLib, techLib, gardenLib, petLib)
    val genEmojis = listOf("⚡🏡", "⚡🏭", "⚡👗", "⚡🧸", "⚡🏆", "⚡💻", "⚡🌳", "⚡🐶")
}