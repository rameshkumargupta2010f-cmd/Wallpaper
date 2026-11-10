package com.example.model

import androidx.annotation.DrawableRes

enum class WallpaperCategory(val displayName: String) {
    ALL("All Characters"),
    GOKU("Goku"),
    VEGETA("Vegeta"),
    GOHAN("Gohan"),
    FUSIONS("Vegito & Gogeta"),
    TRUNKS("Future Trunks"),
    PICCOLO("Piccolo"),
    Z_WARRIORS("Z-Fighters"),
    VILLAINS("Legendary Villains"),
    GODS("Gods & Angels"),
    SUPER("Dragon Ball Super"),
    GT("GT Era"),
    RELICS("Dragon Balls & Relics"),
    AMOLED("AMOLED Dark")
}

enum class WallpaperStyle(val label: String) {
    DRAWABLE_ASSET("Key Visual Art"),
    CANVAS_AURA("Neon Ki Aura"),
    CANVAS_KANJI("Kanji Emblem"),
    CANVAS_KI_SPHERE("Ki Sphere Supernova"),
    CANVAS_BEAM("Energy Beam Surge"),
    CANVAS_DRAGON_BALLS("Dragon Balls Orbit"),
    CANVAS_SCENE("Cinematic Wasteland"),
    CANVAS_CYBER_TECH("Capsule Scouter HUD"),
    CANVAS_MANGA_SKETCH("Manga Ink Speedlines"),
    CANVAS_COSMIC_VOID("Cosmic Stardust Void"),
    CANVAS_LIGHTNING_STORM("Electric Thunderstorm")
}

enum class ResolutionFilter(val displayName: String) {
    ALL("All Resolutions"),
    RES_4K("4K Ultra HD"),
    RES_2K("2K Quad HD"),
    RES_FHD("Full HD 1080p")
}

enum class StyleFilter(val displayName: String) {
    ALL("All Art Styles"),
    AURA("Neon Ki Auras"),
    AMOLED("AMOLED Pure Black"),
    BEAM("Kamehameha & Beams"),
    KI_SPHERE("Planetary Ki Spheres"),
    MANGA("Manga Ink Action"),
    CYBER("Capsule Corp Tech"),
    LIGHTNING("Limit Break Lightning"),
    COSMIC("Cosmic Stardust")
}

enum class HomeSection(val displayName: String) {
    ALL("All"),
    TRENDING("🔥 Trending"),
    MOST_DOWNLOADED("⚡ Most Downloaded"),
    RECENT("✨ Recently Added")
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
    val isTrending: Boolean = false,
    val isFeatured: Boolean = false,
    val description: String = "",
    val resolution: String = "4K Ultra HD",
    val downloadsCount: Int = 1240,
    val likesCount: Int = 890,
    val createdTimestamp: Long = System.currentTimeMillis()
)

enum class WallpaperScreenTarget {
    HOME_SCREEN,
    LOCK_SCREEN,
    BOTH_SCREENS
}
