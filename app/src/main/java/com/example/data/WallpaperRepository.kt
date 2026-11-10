package com.example.data

import android.content.Context
import com.example.R
import com.example.model.Wallpaper
import com.example.model.WallpaperCategory
import com.example.model.WallpaperStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WallpaperRepository(context: Context) {

    private val prefs = context.getSharedPreferences("dragon_wallpapers_prefs", Context.MODE_PRIVATE)

    private val _favoriteIds = MutableStateFlow<Set<String>>(
        prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet()
    )
    val favoriteIdsFlow: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _downloadedIds = MutableStateFlow<Set<String>>(
        prefs.getStringSet("downloaded", emptySet())?.toSet() ?: emptySet()
    )
    val downloadedIdsFlow: StateFlow<Set<String>> = _downloadedIds.asStateFlow()

    fun toggleFavorite(wallpaperId: String) {
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(wallpaperId)) {
            current.remove(wallpaperId)
        } else {
            current.add(wallpaperId)
        }
        prefs.edit().putStringSet("favorites", current).apply()
        _favoriteIds.value = current
    }

    fun recordDownload(wallpaperId: String) {
        val current = _downloadedIds.value.toMutableSet()
        current.add(wallpaperId)
        prefs.edit().putStringSet("downloaded", current).apply()
        _downloadedIds.value = current
    }

    fun getAllWallpapers(): List<Wallpaper> = ALL_WALLPAPERS

    fun getWallpaperById(id: String): Wallpaper? = ALL_WALLPAPERS.find { it.id == id }

    companion object {
        val ALL_WALLPAPERS: List<Wallpaper> = listOf(
            // Key Visual High-Res Wallpapers (bundled images)
            Wallpaper(
                id = "goku_ultra_instinct",
                title = "Ultra Instinct Mastered",
                character = "Goku",
                saga = "Tournament of Power",
                form = "Ultra Instinct",
                category = WallpaperCategory.GOKU,
                drawableResId = R.drawable.img_wallpaper_goku_ui,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFECEFF1,
                secondaryColor = 0xFF90CAF9,
                accentColor = 0xFFFFD600,
                quote = "This is the power of Trust!",
                tags = listOf("Ultra Instinct", "God Ki", "Silver Aura", "Silver Hair", "4K"),
                isAmoled = true,
                description = "Son Goku achieving complete mastery of Ultra Instinct with shimmering silver aura and divine speed.",
                downloadsCount = 18450,
                likesCount = 9420
            ),
            Wallpaper(
                id = "vegeta_ssj_blue_evolved",
                title = "Super Saiyan Blue Evolved",
                character = "Vegeta",
                saga = "Tournament of Power",
                form = "SSJ Blue Evolved",
                category = WallpaperCategory.VEGETA,
                drawableResId = R.drawable.img_wallpaper_vegeta_blue,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFF0091EA,
                secondaryColor = 0xFF00E5FF,
                accentColor = 0xFFFFD600,
                quote = "A Saiyan's strength knows no limits!",
                tags = listOf("Vegeta", "SSJ Blue", "Prince of Saiyans", "Royal Ki", "4K"),
                isAmoled = true,
                description = "Prince Vegeta pushing past the boundaries of Super Saiyan Blue with intense sapphire aura and unbreakable pride.",
                downloadsCount = 16200,
                likesCount = 8310
            ),
            Wallpaper(
                id = "gohan_beast_awakening",
                title = "Beast Gohan Awakening",
                character = "Gohan",
                saga = "Super Hero",
                form = "Gohan Beast",
                category = WallpaperCategory.GOHAN,
                drawableResId = R.drawable.img_wallpaper_gohan_beast,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFFF1744,
                secondaryColor = 0xFF7C4DFF,
                accentColor = 0xFFE040FB,
                quote = "I won't let you hurt my family!",
                tags = listOf("Gohan", "Beast", "Red Eyes", "Purple Gi", "Special Beam Cannon"),
                isAmoled = true,
                description = "Son Gohan unleashing his latent inner beast with crimson pupils, sharp lavender spikes, and furious electrical aura.",
                downloadsCount = 14890,
                likesCount = 7650
            ),
            Wallpaper(
                id = "shenron_eternal_dragon",
                title = "Shenron The Eternal Dragon",
                character = "Shenron",
                saga = "Dragon Ball Legend",
                form = "Eternal Dragon",
                category = WallpaperCategory.RELICS,
                drawableResId = R.drawable.img_wallpaper_shenron,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFF00E676,
                secondaryColor = 0xFFFF9100,
                accentColor = 0xFFFFD600,
                quote = "State your wish, and it shall be granted!",
                tags = listOf("Shenron", "Dragon Balls", "Mythical", "Grant Wish", "Emerald"),
                isAmoled = true,
                description = "The mythical green dragon Shenron soaring through stormy skies surrounded by the seven glowing Dragon Balls.",
                downloadsCount = 19800,
                likesCount = 11200
            ),

            // Goku Collection
            Wallpaper(
                id = "goku_kanji_go",
                title = "Goku 'GO' Kanji Emblem",
                character = "Goku",
                saga = "Namek Saga",
                form = "Kanji Symbol",
                category = WallpaperCategory.AMOLED,
                styleType = WallpaperStyle.CANVAS_KANJI,
                primaryColor = 0xFFFF6D00,
                secondaryColor = 0xFFFFAB40,
                accentColor = 0xFFFFD600,
                kanjiSymbol = "悟",
                kanjiMeaning = "Wisdom & Enlightenment",
                quote = "I am the hope of the universe!",
                tags = listOf("Amoled", "Kanji", "Minimalist", "Symbol", "Turtle School"),
                isAmoled = true,
                description = "The iconic 'GO' symbol worn on Goku's gi after training in 100x gravity on his voyage to Planet Namek."
            ),
            Wallpaper(
                id = "goku_super_saiyan_namek",
                title = "Legendary Super Saiyan 1",
                character = "Goku",
                saga = "Namek Saga",
                form = "Super Saiyan",
                category = WallpaperCategory.GOKU,
                styleType = WallpaperStyle.CANVAS_AURA,
                primaryColor = 0xFFFFD600,
                secondaryColor = 0xFFFF6D00,
                accentColor = 0xFFFFFFFF,
                quote = "I am Son Goku, and I am a Super Saiyan!",
                tags = listOf("Super Saiyan", "Golden Aura", "Namek", "First Awakening"),
                isAmoled = false,
                description = "The first legendary transformation that broke a thousand-year prophecy on the dying planet Namek."
            ),
            Wallpaper(
                id = "goku_super_saiyan_3",
                title = "Super Saiyan 3 Limit Break",
                character = "Goku",
                saga = "Majin Buu Saga",
                form = "Super Saiyan 3",
                category = WallpaperCategory.GOKU,
                styleType = WallpaperStyle.CANVAS_AURA,
                primaryColor = 0xFFFFAB00,
                secondaryColor = 0xFFFFEA00,
                accentColor = 0xFF00E5FF,
                quote = "And this... is to go even further beyond!",
                tags = listOf("SSJ3", "Majin Buu", "Lightning", "Shaking Earth"),
                isAmoled = true,
                description = "The tremendous transformation that shook the entire Earth and reached into the afterlife realm."
            ),
            Wallpaper(
                id = "goku_ssj_god_crimson",
                title = "Super Saiyan God Flame",
                character = "Goku",
                saga = "Battle of Gods",
                form = "Super Saiyan God",
                category = WallpaperCategory.GODS,
                styleType = WallpaperStyle.CANVAS_AURA,
                primaryColor = 0xFFFF1744,
                secondaryColor = 0xFFFF5252,
                accentColor = 0xFFFFD700,
                quote = "This power is on a completely different level!",
                tags = listOf("SSJ God", "Divine Ki", "Crimson", "Beerus"),
                isAmoled = true,
                description = "The ritual of five righteous Saiyans giving rise to the fiery crimson aura of a Super Saiyan God."
            ),
            Wallpaper(
                id = "goku_kamehameha_beam",
                title = "God Kamehameha Wave",
                character = "Goku",
                saga = "Dragon Ball Super",
                form = "Super Saiyan Blue",
                category = WallpaperCategory.GOKU,
                styleType = WallpaperStyle.CANVAS_BEAM,
                primaryColor = 0xFF00B0FF,
                secondaryColor = 0xFF00E5FF,
                accentColor = 0xFFFFFFFF,
                quote = "KA... ME... HA... ME... HAAA!",
                tags = listOf("Kamehameha", "Energy Beam", "Blue Ki", "Signature"),
                isAmoled = true,
                description = "Master Roshi's legendary technique amplified with divine Super Saiyan Blue God ki."
            ),
            Wallpaper(
                id = "goku_spirit_bomb",
                title = "Universal Spirit Bomb",
                character = "Goku",
                saga = "Majin Buu Saga",
                form = "Genki Dama",
                category = WallpaperCategory.GOKU,
                styleType = WallpaperStyle.CANVAS_KI_SPHERE,
                primaryColor = 0xFF00E5FF,
                secondaryColor = 0xFF0091EA,
                accentColor = 0xFFFFFFFF,
                quote = "People of Earth! Share your energy with me!",
                tags = listOf("Spirit Bomb", "Genki Dama", "Earth Energy", "Ultimate Move"),
                isAmoled = true,
                description = "Gathering the vitality of every living soul across the cosmos into one colossal sphere of pure light."
            ),

            // Vegeta Collection
            Wallpaper(
                id = "vegeta_majin_symbol",
                title = "Majin 'M' Pride",
                character = "Vegeta",
                saga = "Majin Buu Saga",
                form = "Majin Vegeta",
                category = WallpaperCategory.VEGETA,
                styleType = WallpaperStyle.CANVAS_KANJI,
                primaryColor = 0xFF9C27B0,
                secondaryColor = 0xFFBA68C8,
                accentColor = 0xFFFFD600,
                kanjiSymbol = "M",
                kanjiMeaning = "Saiyan Prince Pride",
                quote = "There is one thing a Saiyan always keeps... his PRIDE!",
                tags = listOf("Majin", "Vegeta", "Prince", "Amoled", "Dark"),
                isAmoled = true,
                description = "The seal of Babidi rejected by the unbreakable spirit of the Prince of all Saiyans."
            ),
            Wallpaper(
                id = "vegeta_ultra_ego",
                title = "Ultra Ego God of Destruction",
                character = "Vegeta",
                saga = "Granolah Arc",
                form = "Ultra Ego",
                category = WallpaperCategory.VEGETA,
                styleType = WallpaperStyle.CANVAS_AURA,
                primaryColor = 0xFFAA00FF,
                secondaryColor = 0xFFD500F9,
                accentColor = 0xFFFF1744,
                quote = "The more burning battle spirit, the stronger I become!",
                tags = listOf("Ultra Ego", "Hakai", "Purple Aura", "Manga Form"),
                isAmoled = true,
                description = "Vegeta's pinnacle warrior state powered by the destructive energy of the Gods of Destruction."
            ),
            Wallpaper(
                id = "vegeta_final_flash",
                title = "Maximum Final Flash",
                character = "Vegeta",
                saga = "Cell Games",
                form = "Super Vegeta",
                category = WallpaperCategory.VEGETA,
                styleType = WallpaperStyle.CANVAS_BEAM,
                primaryColor = 0xFFFFD600,
                secondaryColor = 0xFFFFAB00,
                accentColor = 0xFFFFFFFF,
                quote = "FINAL... FLASHHH!",
                tags = listOf("Final Flash", "Cell", "Golden Beam", "Epic"),
                isAmoled = true,
                description = "The earth-shattering blast that vaporized half of Perfect Cell's body from space."
            ),
            Wallpaper(
                id = "vegeta_big_bang_attack",
                title = "Big Bang Attack",
                character = "Vegeta",
                saga = "Android Saga",
                form = "Super Saiyan",
                category = WallpaperCategory.VEGETA,
                styleType = WallpaperStyle.CANVAS_KI_SPHERE,
                primaryColor = 0xFF00B0FF,
                secondaryColor = 0xFF2979FF,
                accentColor = 0xFFFFFFFF,
                quote = "Welcome to the end of your life, Android 19!",
                tags = listOf("Big Bang Attack", "Androids", "Azure Sphere"),
                isAmoled = true,
                description = "Vegeta's devastating single-hand energy sphere that obliterated Android 19."
            ),

            // Gohan Collection
            Wallpaper(
                id = "gohan_father_son_kamehameha",
                title = "Father-Son Kamehameha",
                character = "Gohan",
                saga = "Cell Games",
                form = "Super Saiyan 2",
                category = WallpaperCategory.GOHAN,
                styleType = WallpaperStyle.CANVAS_BEAM,
                primaryColor = 0xFF00E5FF,
                secondaryColor = 0xFFFFD600,
                accentColor = 0xFFFFFFFF,
                quote = "NOW GOHAN! RELEASE EVERYTHING!",
                tags = listOf("SSJ2", "Cell Games", "Father Son", "Legendary"),
                isAmoled = true,
                description = "The emotional climax of the Cell Games where Gohan avenged his father with a one-handed Kamehameha."
            ),
            Wallpaper(
                id = "gohan_ultimate_mystic",
                title = "Ultimate Mystic Potential",
                character = "Gohan",
                saga = "Majin Buu Saga",
                form = "Ultimate Gohan",
                category = WallpaperCategory.GOHAN,
                styleType = WallpaperStyle.CANVAS_AURA,
                primaryColor = 0xFFE0E0E0,
                secondaryColor = 0xFF9E9E9E,
                accentColor = 0xFFFFFFFF,
                quote = "Fight you? No, I'm going to kill you.",
                tags = listOf("Mystic", "Old Kai", "Elder Potential", "Buu"),
                isAmoled = true,
                description = "The unsealed potential unlocked by Old Kai, boasting calm white ki with overwhelming power."
            ),

            // Future Trunks Collection
            Wallpaper(
                id = "trunks_sword_of_hope",
                title = "Sword of Hope",
                character = "Trunks",
                saga = "Future Trunks Arc",
                form = "Super Saiyan Rage",
                category = WallpaperCategory.TRUNKS,
                styleType = WallpaperStyle.CANVAS_BEAM,
                primaryColor = 0xFF00E5FF,
                secondaryColor = 0xFFFFD600,
                accentColor = 0xFFFFFFFF,
                quote = "My sword carries the will of everyone who survived!",
                tags = listOf("Trunks", "Sword of Hope", "Goku Black", "Future"),
                isAmoled = true,
                description = "The shining broadsword infused with the spirit and prayers of every survivor of the future world."
            ),
            Wallpaper(
                id = "trunks_capsule_corp",
                title = "Capsule Corp Minimalist",
                character = "Trunks",
                saga = "Future Saga",
                form = "Tech Emblem",
                category = WallpaperCategory.AMOLED,
                styleType = WallpaperStyle.CANVAS_KANJI,
                primaryColor = 0xFF00E5FF,
                secondaryColor = 0xFF0091EA,
                accentColor = 0xFFE040FB,
                kanjiSymbol = "CC",
                kanjiMeaning = "Capsule Corporation",
                quote = "I will protect the future!",
                tags = listOf("Capsule Corp", "Cyber", "Amoled", "Bulma", "Trunks"),
                isAmoled = true,
                description = "The futuristic logo of Capsule Corporation in high-contrast cyan and midnight black."
            ),

            // Piccolo Collection
            Wallpaper(
                id = "piccolo_makankosappo",
                title = "Special Beam Cannon",
                character = "Piccolo",
                saga = "Saiyan Saga",
                form = "Makankosappo",
                category = WallpaperCategory.PICCOLO,
                styleType = WallpaperStyle.CANVAS_BEAM,
                primaryColor = 0xFFFFD600,
                secondaryColor = 0xFF76FF03,
                accentColor = 0xFFFF1744,
                quote = "MA-KAN-KO-SAP-PO!",
                tags = listOf("Piccolo", "Special Beam Cannon", "Namekian", "Spiral"),
                isAmoled = true,
                description = "Piccolo's signature piercing spiral laser concentrated at the fingertips."
            ),
            Wallpaper(
                id = "piccolo_kanji_demon",
                title = "Demon King 'MA' Kanji",
                character = "Piccolo",
                saga = "Classic DB / Saiyan Arc",
                form = "Demon Clan Symbol",
                category = WallpaperCategory.PICCOLO,
                styleType = WallpaperStyle.CANVAS_KANJI,
                primaryColor = 0xFF76FF03,
                secondaryColor = 0xFF00C853,
                accentColor = 0xFF9C27B0,
                kanjiSymbol = "魔",
                kanjiMeaning = "Demon / Mystic Magic",
                quote = "Even with the celestial gods, Namekians endure!",
                tags = listOf("Piccolo", "Demon", "Namek", "Kanji"),
                isAmoled = true,
                description = "The ancient demon crest of the Great Demon King Piccolo and the protector of Earth."
            ),
            Wallpaper(
                id = "piccolo_orange_power",
                title = "Orange Piccolo Awakening",
                character = "Piccolo",
                saga = "Super Hero",
                form = "Orange Piccolo",
                category = WallpaperCategory.PICCOLO,
                styleType = WallpaperStyle.CANVAS_AURA,
                primaryColor = 0xFFFF6D00,
                secondaryColor = 0xFFFFD600,
                accentColor = 0xFF76FF03,
                quote = "Shenron threw in a little extra!",
                tags = listOf("Orange Piccolo", "Super Hero", "Namekian Pride", "Bulky"),
                isAmoled = true,
                description = "The gargantuan state granted by Shenron's extra wish, towering with heavy golden-orange power."
            ),

            // Villains & Rivals Collection
            Wallpaper(
                id = "frieza_golden_emperor",
                title = "Golden Frieza",
                character = "Frieza",
                saga = "Resurrection F",
                form = "Golden Frieza",
                category = WallpaperCategory.VILLAINS,
                styleType = WallpaperStyle.CANVAS_AURA,
                primaryColor = 0xFFFFD600,
                secondaryColor = 0xFF9C27B0,
                accentColor = 0xFFFF1744,
                quote = "I chose gold to make my triumph obvious!",
                tags = listOf("Frieza", "Golden", "Emperor", "Death Beam"),
                isAmoled = true,
                description = "The glittering golden transformation forged through four months of rigorous training."
            ),
            Wallpaper(
                id = "frieza_death_ball",
                title = "Supernova Death Ball",
                character = "Frieza",
                saga = "Planet Namek / Planet Vegeta",
                form = "Supernova",
                category = WallpaperCategory.VILLAINS,
                styleType = WallpaperStyle.CANVAS_KI_SPHERE,
                primaryColor = 0xFFFF1744,
                secondaryColor = 0xFFFF9100,
                accentColor = 0xFFFFD600,
                quote = "Before you die, enjoy the fireworks!",
                tags = listOf("Death Ball", "Supernova", "Destruction", "Namek"),
                isAmoled = true,
                description = "The miniature star of boiling plasma that obliterated Planet Vegeta."
            ),
            Wallpaper(
                id = "broly_legendary_berserk",
                title = "Legendary Berserk Broly",
                character = "Broly",
                saga = "DBS: Broly",
                form = "Full Power LSSJ",
                category = WallpaperCategory.VILLAINS,
                styleType = WallpaperStyle.CANVAS_AURA,
                primaryColor = 0xFF00E676,
                secondaryColor = 0xFF76FF03,
                accentColor = 0xFFFFFFFF,
                quote = "RAAAAGHHH!",
                tags = listOf("Broly", "Emerald Ki", "Legendary Super Saiyan", "Berserk"),
                isAmoled = true,
                description = "The raw, unchecked mutant Saiyan power glowing with explosive emerald green ki."
            ),
            Wallpaper(
                id = "cell_perfect_form",
                title = "Perfect Cell Bio-Android",
                character = "Cell",
                saga = "Cell Games",
                form = "Perfect Form",
                category = WallpaperCategory.VILLAINS,
                styleType = WallpaperStyle.CANVAS_AURA,
                primaryColor = 0xFF76FF03,
                secondaryColor = 0xFFFFD600,
                accentColor = 0xFF9C27B0,
                quote = "I have reached absolute perfection!",
                tags = listOf("Cell", "Perfect", "Bio-Android", "Cell Games"),
                isAmoled = true,
                description = "The ultimate bio-mechanical creation combining the DNA of Goku, Vegeta, Frieza, and Piccolo."
            ),
            Wallpaper(
                id = "buu_planet_burst",
                title = "Kid Buu Planet Burst",
                character = "Majin Buu",
                saga = "Majin Buu Saga",
                form = "Pure Kid Buu",
                category = WallpaperCategory.VILLAINS,
                styleType = WallpaperStyle.CANVAS_KI_SPHERE,
                primaryColor = 0xFFFF4081,
                secondaryColor = 0xFFE040FB,
                accentColor = 0xFFFF1744,
                quote = "Keee-he-he-he!",
                tags = listOf("Kid Buu", "Planet Burst", "Chaos", "Pink Ki"),
                isAmoled = true,
                description = "The primordial sphere of utter chaos that disintegrated the Earth in a single blast."
            ),
            Wallpaper(
                id = "goku_black_rose",
                title = "Super Saiyan Rosé",
                character = "Goku Black",
                saga = "Future Trunks Arc",
                form = "SSJ Rosé",
                category = WallpaperCategory.VILLAINS,
                styleType = WallpaperStyle.CANVAS_AURA,
                primaryColor = 0xFFFF4081,
                secondaryColor = 0xFF7C4DFF,
                accentColor = 0xFFFF80AB,
                quote = "My beauty is truly divine!",
                tags = listOf("Goku Black", "Rosé", "Zamasu", "Divine"),
                isAmoled = true,
                description = "The godly Ki of Zamasu coursing through a Saiyan body, glowing with intoxicating crimson-violet rosé."
            ),

            // Gods & Relics Collection
            Wallpaper(
                id = "beerus_destruction_sphere",
                title = "Sphere of Destruction",
                character = "Beerus",
                saga = "Battle of Gods",
                form = "God of Destruction",
                category = WallpaperCategory.GODS,
                styleType = WallpaperStyle.CANVAS_KI_SPHERE,
                primaryColor = 0xFFAA00FF,
                secondaryColor = 0xFFFF6D00,
                accentColor = 0xFFFFD600,
                quote = "Before creation... must come destruction.",
                tags = listOf("Beerus", "Hakai", "God of Destruction", "Cat God"),
                isAmoled = true,
                description = "Lord Beerus's sun-like sphere of absolute erasure capable of erasing entire solar systems."
            ),
            Wallpaper(
                id = "seven_dragon_balls_orbit",
                title = "The 7 Sacred Dragon Balls",
                character = "Dragon Balls",
                saga = "Mythology",
                form = "Sacred Formation",
                category = WallpaperCategory.RELICS,
                styleType = WallpaperStyle.CANVAS_DRAGON_BALLS,
                primaryColor = 0xFFFF9100,
                secondaryColor = 0xFFFFD600,
                accentColor = 0xFFD50000,
                quote = "Come forth, Shenron! And grant my wish!",
                tags = listOf("Dragon Balls", "Shenron", "Wish", "Amber", "Stars"),
                isAmoled = true,
                description = "The seven crystal orbs created by the Namekian Kami to summon the great dragon."
            ),
            Wallpaper(
                id = "master_roshi_kame_kanji",
                title = "Turtle School 'KAME' Kanji",
                character = "Master Roshi",
                saga = "Dragon Ball Classic",
                form = "Turtle Hermit",
                category = WallpaperCategory.AMOLED,
                styleType = WallpaperStyle.CANVAS_KANJI,
                primaryColor = 0xFFFF6D00,
                secondaryColor = 0xFFFFAB40,
                accentColor = 0xFFFFFFFF,
                kanjiSymbol = "亀",
                kanjiMeaning = "Turtle School",
                quote = "Work hard, study well, and eat and sleep plenty!",
                tags = listOf("Roshi", "Turtle School", "Classic", "Kanji", "Amoled"),
                isAmoled = true,
                description = "The foundational martial arts motto of Master Roshi, worn by Goku and Krillin."
            ),
            Wallpaper(
                id = "hyperbolic_time_chamber",
                title = "Hyperbolic Time Chamber",
                character = "Sacred Realm",
                saga = "All Sagas",
                form = "Room of Spirit & Time",
                category = WallpaperCategory.RELICS,
                styleType = WallpaperStyle.CANVAS_SCENE,
                primaryColor = 0xFFFFD600,
                secondaryColor = 0xFF00B0FF,
                accentColor = 0xFFFFFFFF,
                quote = "A single day outside is a year of training inside.",
                tags = listOf("Time Chamber", "Kami Lookout", "Training", "Infinity"),
                isAmoled = false,
                description = "The limitless white void with ten times Earth's gravity where warriors push past mortal limits."
            )
        )
    }
}
