package com.example.model

import androidx.annotation.DrawableRes

enum class WallpaperCategory(val displayName: String) {
    ALL("All"),
    GOKU("Goku"),
    VEGETA("Vegeta"),
    GOHAN("Gohan"),
    TRUNKS("Trunks"),
    PICCOLO("Piccolo"),
    VILLAINS("Villains"),
    GODS("Gods & Angels"),
    RELICS("Dragon & Relics"),
    AMOLED("AMOLED Dark")
}

enum class WallpaperStyle {
    DRAWABLE_ASSET,
    CANVAS_AURA,
    CANVAS_KANJI,
    CANVAS_KI_SPHERE,
    CANVAS_BEAM,
    CANVAS_DRAGON_BALLS,
    CANVAS_SCENE
}

data class Wallpaper(
    val id: String,
    val title: String,
    val character: String,
    val saga: String,
    val form: String,
    val category: WallpaperCategory,
    @DrawableRes val drawableResId: Int? = null,
    val styleType: WallpaperStyle = WallpaperStyle.DRAWABLE_ASSET,
    val primaryColor: Long = 0xFFFF6D00,
    val secondaryColor: Long = 0xFF00B0FF,
    val accentColor: Long = 0xFFFFD600,
    val kanjiSymbol: String = "",
    val kanjiMeaning: String = "",
    val quote: String = "",
    val tags: List<String> = emptyList(),
    val isAmoled: Boolean = false,
    val description: String = "",
    val resolution: String = "1440 x 2560 Ultra HD",
    val downloadsCount: Int = 1240,
    val likesCount: Int = 890
)

enum class WallpaperScreenTarget {
    HOME_SCREEN,
    LOCK_SCREEN,
    BOTH_SCREENS
}
