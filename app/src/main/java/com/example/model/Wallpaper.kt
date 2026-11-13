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
    CANVAS_FACE_AND_BODY("Face & Full Body Duo"),
    CANVAS_FULL_BODY_HERO("Full Body Action Stance"),
    CANVAS_CINEMATIC_FACE("Fierce Face Close-Up"),
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
    ALL("All Realistic Art"),
    FULL_BODY("Full Body Stance"),
    DIVINE_AURA("Divine Aura & God Ki"),
    VILLAIN_POWER("Dark Villain Power"),
    AMOLED("AMOLED Deep Dark")
}

enum class HomeSection(val displayName: String) {
    ALL("All"),
    CINEMATIC("⚡ Full Body Realistic"),
    TRENDING("🔥 Trending"),
    MOST_DOWNLOADED("⚡ Top Saved"),
    RECENT("✨ Latest")
}

enum class RealisticTonePreset(val label: String, val badge: String) {
    STUDIO_RAW("Studio Raw", "RAW 4K"),
    CINEMATIC_HDR("Cinematic HDR", "HDR+"),
    AMOLED_VIVID("AMOLED Vivid", "AMOLED"),
    GOLDEN_RIM("Golden Rim", "WARM")
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
