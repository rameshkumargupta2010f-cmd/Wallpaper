package com.example.model

import androidx.annotation.DrawableRes

enum class WallpaperCategory(val displayName: String) {
    ALL("All (1,000+)"),
    GOKU("Goku"),
    VEGETA("Vegeta"),
    GOHAN("Gohan"),
    FUSIONS("Fusions (Vegito/Gogeta)"),
    TRUNKS("Future Trunks"),
    PICCOLO("Piccolo"),
    Z_WARRIORS("Z-Fighters"),
    VILLAINS("Legendary Villains"),
    GODS("Gods & Angels"),
    SUPER("Super & Movies"),
    GT("GT Era"),
    RELICS("Dragon Balls & Relics"),
    AMOLED("AMOLED Dark (Pitch Black)")
}

enum class WallpaperStyle {
    DRAWABLE_ASSET,
    CANVAS_AURA,
    CANVAS_KANJI,
    CANVAS_KI_SPHERE,
    CANVAS_BEAM,
    CANVAS_DRAGON_BALLS,
    CANVAS_SCENE,
    CANVAS_CYBER_TECH,
    CANVAS_MANGA_SKETCH,
    CANVAS_COSMIC_VOID,
    CANVAS_LIGHTNING_STORM
}

data class Wallpaper(
    val id: String,
    val title: String,
    val character: String,
    val saga: String,
    val form: String,
    val category: WallpaperCategory,
    @DrawableRes val drawableResId: Int? = null,
    val styleType: WallpaperStyle = WallpaperStyle.CANVAS_AURA,
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
