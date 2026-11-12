package com.example.data

import android.content.Context
import com.example.R
import com.example.model.Wallpaper
import com.example.model.WallpaperCategory
import com.example.model.WallpaperStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

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

    // CMS dynamic additions and deletions
    private val _deletedIds = MutableStateFlow<Set<String>>(
        prefs.getStringSet("cms_deleted_wallpapers", emptySet())?.toSet() ?: emptySet()
    )

    private val _featuredIds = MutableStateFlow<Set<String>>(
        prefs.getStringSet("cms_featured_wallpapers", emptySet())?.toSet() ?: emptySet()
    )

    private val _trendingIds = MutableStateFlow<Set<String>>(
        prefs.getStringSet("cms_trending_wallpapers", emptySet())?.toSet() ?: emptySet()
    )

    private val _customWallpapers = MutableStateFlow<List<Wallpaper>>(
        loadCustomWallpapersFromPrefs()
    )

    // Master Wallpapers list updated when custom wallpapers are added or deleted
    private val _wallpapersFlow = MutableStateFlow(computeActiveWallpapers())
    val wallpapersFlow: StateFlow<List<Wallpaper>> = _wallpapersFlow.asStateFlow()

    private fun computeActiveWallpapers(): List<Wallpaper> {
        val deleted = _deletedIds.value
        val featured = _featuredIds.value
        val trending = _trendingIds.value

        val baseList = ALL_WALLPAPERS.filterNot { deleted.contains(it.id) }
        val customList = _customWallpapers.value.filterNot { deleted.contains(it.id) }

        return (customList + baseList).map { wp ->
            var updated = wp
            if (featured.contains(wp.id)) updated = updated.copy(isFeatured = true)
            if (trending.contains(wp.id)) updated = updated.copy(isTrending = true)
            updated
        }
    }

    private fun updateWallpapers() {
        _wallpapersFlow.value = computeActiveWallpapers()
    }

    fun getAllWallpapers(): List<Wallpaper> = _wallpapersFlow.value

    fun getWallpaperById(id: String): Wallpaper? = _wallpapersFlow.value.find { it.id == id }

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

    // CMS Operations
    fun addCustomWallpaper(wallpaper: Wallpaper) {
        val current = _customWallpapers.value.toMutableList()
        current.add(0, wallpaper)
        _customWallpapers.value = current
        saveCustomWallpapersToPrefs(current)
        updateWallpapers()
    }

    fun deleteWallpaper(wallpaperId: String) {
        val currentDeleted = _deletedIds.value.toMutableSet()
        currentDeleted.add(wallpaperId)
        _deletedIds.value = currentDeleted
        prefs.edit().putStringSet("cms_deleted_wallpapers", currentDeleted).apply()

        // If it was custom, remove from custom list
        val currentCustom = _customWallpapers.value.filterNot { it.id == wallpaperId }
        _customWallpapers.value = currentCustom
        saveCustomWallpapersToPrefs(currentCustom)

        updateWallpapers()
    }

    fun toggleFeatured(wallpaperId: String) {
        val current = _featuredIds.value.toMutableSet()
        if (current.contains(wallpaperId)) current.remove(wallpaperId) else current.add(wallpaperId)
        _featuredIds.value = current
        prefs.edit().putStringSet("cms_featured_wallpapers", current).apply()
        updateWallpapers()
    }

    fun toggleTrending(wallpaperId: String) {
        val current = _trendingIds.value.toMutableSet()
        if (current.contains(wallpaperId)) current.remove(wallpaperId) else current.add(wallpaperId)
        _trendingIds.value = current
        prefs.edit().putStringSet("cms_trending_wallpapers", current).apply()
        updateWallpapers()
    }

    fun getDailyWallpaper(): Wallpaper {
        val all = getAllWallpapers()
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return all[dayOfYear % all.size]
    }

    fun getRandomWallpaper(): Wallpaper {
        val all = getAllWallpapers()
        return all.random()
    }

    fun getCategoryCounts(): Map<WallpaperCategory, Int> {
        val all = getAllWallpapers()
        val counts = mutableMapOf<WallpaperCategory, Int>()
        WallpaperCategory.values().forEach { cat ->
            counts[cat] = if (cat == WallpaperCategory.ALL) {
                all.size
            } else if (cat == WallpaperCategory.AMOLED) {
                all.count { it.isAmoled }
            } else {
                all.count { it.category == cat }
            }
        }
        return counts
    }

    private fun loadCustomWallpapersFromPrefs(): List<Wallpaper> {
        val jsonStr = prefs.getString("custom_wallpapers_json", null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<Wallpaper>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val catName = obj.optString("category", WallpaperCategory.ALL.name)
                val category = runCatching { WallpaperCategory.valueOf(catName) }.getOrDefault(WallpaperCategory.ALL)
                val styleName = obj.optString("styleType", WallpaperStyle.CANVAS_AURA.name)
                val style = runCatching { WallpaperStyle.valueOf(styleName) }.getOrDefault(WallpaperStyle.CANVAS_AURA)

                val tagsJson = obj.optJSONArray("tags")
                val tags = mutableListOf<String>()
                if (tagsJson != null) {
                    for (t in 0 until tagsJson.length()) {
                        tags.add(tagsJson.getString(t))
                    }
                }

                list.add(
                    Wallpaper(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        character = obj.getString("character"),
                        saga = obj.optString("saga", "Custom Upload"),
                        form = obj.optString("form", "Custom Form"),
                        category = category,
                        styleType = style,
                        primaryColor = obj.optLong("primaryColor", 0xFFFF6D00),
                        secondaryColor = obj.optLong("secondaryColor", 0xFF00B0FF),
                        accentColor = obj.optLong("accentColor", 0xFFFFD600),
                        kanjiSymbol = obj.optString("kanjiSymbol", "氣"),
                        quote = obj.optString("quote", ""),
                        tags = tags,
                        isAmoled = obj.optBoolean("isAmoled", false),
                        isTrending = obj.optBoolean("isTrending", false),
                        isFeatured = obj.optBoolean("isFeatured", false),
                        description = obj.optString("description", "Custom wallpaper uploaded via Creator Studio."),
                        resolution = obj.optString("resolution", "4K Ultra HD"),
                        downloadsCount = obj.optInt("downloadsCount", 0),
                        likesCount = obj.optInt("likesCount", 0)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCustomWallpapersToPrefs(list: List<Wallpaper>) {
        try {
            val array = JSONArray()
            list.forEach { wp ->
                val obj = JSONObject().apply {
                    put("id", wp.id)
                    put("title", wp.title)
                    put("character", wp.character)
                    put("saga", wp.saga)
                    put("form", wp.form)
                    put("category", wp.category.name)
                    put("styleType", wp.styleType.name)
                    put("primaryColor", wp.primaryColor)
                    put("secondaryColor", wp.secondaryColor)
                    put("accentColor", wp.accentColor)
                    put("kanjiSymbol", wp.kanjiSymbol)
                    put("quote", wp.quote)
                    put("isAmoled", wp.isAmoled)
                    put("isTrending", wp.isTrending)
                    put("isFeatured", wp.isFeatured)
                    put("description", wp.description)
                    put("resolution", wp.resolution)
                    put("downloadsCount", wp.downloadsCount)
                    put("likesCount", wp.likesCount)

                    val tagsArr = JSONArray()
                    wp.tags.forEach { tagsArr.put(it) }
                    put("tags", tagsArr)
                }
                array.put(obj)
            }
            prefs.edit().putString("custom_wallpapers_json", array.toString()).apply()
        } catch (_: Exception) {}
    }

    companion object {

        private data class CharacterProfile(
            val id: String,
            val name: String,
            val form: String,
            val saga: String,
            val category: WallpaperCategory,
            val primaryColor: Long,
            val secondaryColor: Long,
            val accentColor: Long,
            val kanji: String,
            val quote: String,
            val tags: List<String>
        )

        private val CHARACTER_PROFILES: List<CharacterProfile> = listOf(
            // Goku & Transformations (11 profiles × 15 editions = 165 Goku Wallpapers!)
            CharacterProfile("goku_base", "Goku", "Kaio-Ken x20", "Saiyan Saga", WallpaperCategory.GOKU, 0xFFFF1744, 0xFFFF6D00, 0xFFFFD600, "悟", "Kaio-ken times twenty!", listOf("Goku", "KaioKen", "Saiyan")),
            CharacterProfile("goku_ssj", "Goku", "Super Saiyan 1", "Namek Saga", WallpaperCategory.GOKU, 0xFFFFD600, 0xFFFF6D00, 0xFFFFFFFF, "悟", "I am the hope of the universe!", listOf("Goku", "SSJ", "Namek")),
            CharacterProfile("goku_ssj2", "Goku", "Super Saiyan 2", "Buu Saga", WallpaperCategory.GOKU, 0xFFFFD600, 0xFF00E5FF, 0xFFFFFFFF, "悟", "And this is Super Saiyan 2!", listOf("Goku", "SSJ2", "Lightning")),
            CharacterProfile("goku_ssj3", "Goku", "Super Saiyan 3", "Buu Saga", WallpaperCategory.GOKU, 0xFFFFAB00, 0xFFFFEA00, 0xFF00B0FF, "悟", "To go even further beyond!", listOf("Goku", "SSJ3", "LimitBreak")),
            CharacterProfile("goku_god", "Goku", "Super Saiyan God", "Battle of Gods", WallpaperCategory.GODS, 0xFFFF1744, 0xFFFF5252, 0xFFFFD700, "神", "A whole new realm of power!", listOf("Goku", "God", "Crimson")),
            CharacterProfile("goku_blue", "Goku", "Super Saiyan Blue", "Resurrection F", WallpaperCategory.GOKU, 0xFF00B0FF, 0xFF00E5FF, 0xFFFFFFFF, "神", "Divine ki with Super Saiyan strength!", listOf("Goku", "SSJBlue", "GodKi")),
            CharacterProfile("goku_blue_kaioken", "Goku", "SSJ Blue Kaio-Ken", "Universe 6 Saga", WallpaperCategory.SUPER, 0xFF00B0FF, 0xFFFF1744, 0xFFFF5252, "界", "Blue Kaio-Ken times ten!", listOf("Goku", "Hit", "KaioKenBlue")),
            CharacterProfile("goku_ui_sign", "Goku", "Ultra Instinct Sign", "Tournament of Power", WallpaperCategory.GOKU, 0xFF90A4AE, 0xFF00E5FF, 0xFFFFFFFF, "極", "The heat of divine awakening!", listOf("Goku", "UISign", "Silver")),
            CharacterProfile("goku_ui_mastered", "Goku", "Mastered Ultra Instinct", "Tournament of Power", WallpaperCategory.GOKU, 0xFFECEFF1, 0xFF90CAF9, 0xFFFFD600, "身", "Power beyond mortal comprehension!", listOf("Goku", "UltraInstinct", "SilverHair")),
            CharacterProfile("kid_goku", "Kid Goku", "Flying Nimbus", "Classic Dragon Ball", WallpaperCategory.GOKU, 0xFFFF6D00, 0xFFFFD600, 0xFF29B6F6, "亀", "Nimbus, let's go on an adventure!", listOf("KidGoku", "Classic", "Nimbus")),
            CharacterProfile("goku_ssj4", "Goku", "Super Saiyan 4", "Dragon Ball GT", WallpaperCategory.GT, 0xFFFF1744, 0xFFFFD600, 0xFF212121, "猿", "Primal Saiyan ferocity unsealed!", listOf("Goku", "SSJ4", "GT", "Primal")),

            // Vegeta & Transformations (8 profiles × 15 editions = 120 Vegeta Wallpapers!)
            CharacterProfile("vegeta_scouter", "Vegeta", "Saiyan Prince", "Saiyan Saga", WallpaperCategory.VEGETA, 0xFF2979FF, 0xFFFFD600, 0xFFE040FB, "王", "Bow before the Prince of all Saiyans!", listOf("Vegeta", "Scouter", "Prince")),
            CharacterProfile("vegeta_ssj", "Vegeta", "Super Saiyan", "Android Saga", WallpaperCategory.VEGETA, 0xFFFFD600, 0xFF2979FF, 0xFFFFFFFF, "王", "Does an Android feel fear?!", listOf("Vegeta", "SSJ", "BigBang")),
            CharacterProfile("vegeta_super", "Vegeta", "Super Vegeta", "Cell Saga", WallpaperCategory.VEGETA, 0xFFFFAB00, 0xFFFFD600, 0xFF00E5FF, "王", "I am Super Vegeta!", listOf("Vegeta", "Ascended", "FinalFlash")),
            CharacterProfile("vegeta_majin", "Vegeta", "Majin Vegeta", "Buu Saga", WallpaperCategory.VEGETA, 0xFF9C27B0, 0xFFFFD600, 0xFFE040FB, "M", "One thing a Saiyan always keeps: PRIDE!", listOf("Vegeta", "Majin", "Pride")),
            CharacterProfile("vegeta_blue", "Vegeta", "Super Saiyan Blue", "Dragon Ball Super", WallpaperCategory.VEGETA, 0xFF0091EA, 0xFF00E5FF, 0xFFFFFFFF, "王", "I will surpass Kakarot my own way!", listOf("Vegeta", "SSJBlue", "God")),
            CharacterProfile("vegeta_blue_evolved", "Vegeta", "SSJ Blue Evolved", "Tournament of Power", WallpaperCategory.VEGETA, 0xFF0052CC, 0xFF00E5FF, 0xFFFFD600, "誇", "My pride is my absolute power!", listOf("Vegeta", "Evolved", "Royal")),
            CharacterProfile("vegeta_ultra_ego", "Vegeta", "Ultra Ego Destruction", "Granolah Arc", WallpaperCategory.VEGETA, 0xFFAA00FF, 0xFFD500F9, 0xFFFF1744, "我", "Unbound battle spirit! Hakai!", listOf("Vegeta", "UltraEgo", "Hakai")),
            CharacterProfile("vegeta_ssj4", "Vegeta", "Super Saiyan 4", "Dragon Ball GT", WallpaperCategory.GT, 0xFFD50000, 0xFF2979FF, 0xFFFFD600, "猿", "I have surpassed the limit of Saiyans!", listOf("Vegeta", "SSJ4", "GT")),

            // Gohan (7 profiles × 15 editions = 105 Gohan Wallpapers!)
            CharacterProfile("gohan_kid", "Gohan", "Kid Rage Unleashed", "Saiyan Saga", WallpaperCategory.GOHAN, 0xFFFF6D00, 0xFF76FF03, 0xFFFF1744, "魔", "Leave my dad alone!", listOf("Gohan", "Kid", "Masenko")),
            CharacterProfile("gohan_teen_ssj", "Gohan", "Teen Super Saiyan", "Cell Games", WallpaperCategory.GOHAN, 0xFFFFD600, 0xFF7C4DFF, 0xFFFFFFFF, "悟", "I will protect the Earth!", listOf("Gohan", "Teen", "CellGames")),
            CharacterProfile("gohan_teen_ssj2", "Gohan", "Super Saiyan 2 Father-Son", "Cell Games", WallpaperCategory.GOHAN, 0xFF00E5FF, 0xFFFFD600, 0xFFFFFFFF, "悟", "Now Gohan! Full Power Kamehameha!", listOf("Gohan", "SSJ2", "FatherSon")),
            CharacterProfile("gohan_ultimate", "Gohan", "Ultimate Mystic Potential", "Buu Saga", WallpaperCategory.GOHAN, 0xFFECEFF1, 0xFF9E9E9E, 0xFFFFFFFF, "悟", "I'm not here to fight. I'm here to finish you.", listOf("Gohan", "Mystic", "Ultimate")),
            CharacterProfile("great_saiyaman", "Gohan", "Great Saiyaman", "Buu Saga", WallpaperCategory.GOHAN, 0xFF00C853, 0xFFFF6D00, 0xFF2979FF, "正", "The champion of justice has arrived!", listOf("Gohan", "Saiyaman", "Justice")),
            CharacterProfile("gohan_beast", "Gohan", "Beast Transformation", "Super Hero", WallpaperCategory.GOHAN, 0xFFFF1744, 0xFF7C4DFF, 0xFFE040FB, "獣", "I will never forgive you!", listOf("Gohan", "Beast", "RedEyes")),
            CharacterProfile("future_gohan", "Future Gohan", "One-Armed Defender", "History of Trunks", WallpaperCategory.GOHAN, 0xFFFFD600, 0xFFFF6D00, 0xFF00E5FF, "飯", "My spirit will live on forever!", listOf("Gohan", "Future", "TrunksMentor")),

            // Fusions (8 profiles × 15 editions = 120 Fusions Wallpapers!)
            CharacterProfile("vegito_base", "Vegito", "Supreme Potara Warrior", "Buu Saga", WallpaperCategory.FUSIONS, 0xFF2979FF, 0xFFFF6D00, 0xFFFFD600, "合", "All right! Call me Vegito!", listOf("Vegito", "Potara", "Fusion")),
            CharacterProfile("super_vegito", "Vegito", "Super Vegito", "Buu Saga", WallpaperCategory.FUSIONS, 0xFFFFD600, 0xFF2979FF, 0xFFFFFFFF, "合", "Are you ready for the real thing?!", listOf("Vegito", "SuperVegito", "Sword")),
            CharacterProfile("vegito_blue", "Vegito", "Vegito Blue Final Kamehameha", "Future Trunks Arc", WallpaperCategory.FUSIONS, 0xFF00B0FF, 0xFF00E5FF, 0xFFFFD600, "神", "FINAL KAMEHAMEHA!", listOf("Vegito", "VegitoBlue", "GodKi")),
            CharacterProfile("gogeta_super", "Gogeta", "Super Gogeta Soul Punisher", "Fusion Reborn", WallpaperCategory.FUSIONS, 0xFFFFD600, 0xFFFF6D00, 0xFF00E5FF, "ゴ", "Every force you create has an echo!", listOf("Gogeta", "SoulPunisher", "Janemba")),
            CharacterProfile("gogeta_blue", "Gogeta", "Gogeta Blue Stardust Breaker", "DBS Broly", WallpaperCategory.FUSIONS, 0xFF00B0FF, 0xFF7C4DFF, 0xFFFFD600, "ゴ", "We are Gogeta!", listOf("Gogeta", "GogetaBlue", "BrolyFight")),
            CharacterProfile("gogeta_ssj4", "Gogeta", "SSJ4 Gogeta 100x Big Bang", "Dragon Ball GT", WallpaperCategory.GT, 0xFFFF1744, 0xFFFFD600, 0xFF00E5FF, "極", "Big Bang Kamehameha times 100!", listOf("Gogeta", "SSJ4Gogeta", "GT")),
            CharacterProfile("gotenks_base", "Gotenks", "Grim Reaper of Justice", "Buu Saga", WallpaperCategory.FUSIONS, 0xFF7C4DFF, 0xFFFF6D00, 0xFFFFD600, "天", "Ta-da! The Grim Reaper of Justice!", listOf("Gotenks", "Fusion", "Kamikaze")),
            CharacterProfile("gotenks_ssj3", "Gotenks", "Super Saiyan 3 Gotenks", "Buu Saga", WallpaperCategory.FUSIONS, 0xFFFFD600, 0xFF7C4DFF, 0xFF00E5FF, "天", "Super Ghost Kamikaze Attack!", listOf("Gotenks", "SSJ3", "Ghosts")),

            // Trunks & Goten (60 Trunks Wallpapers)
            CharacterProfile("trunks_future_sword", "Trunks", "Future Sword of Hope", "Future Trunks Arc", WallpaperCategory.TRUNKS, 0xFF00E5FF, 0xFFFFD600, 0xFFFFFFFF, "希", "This sword carries the will of mankind!", listOf("Trunks", "SwordOfHope", "Future")),
            CharacterProfile("trunks_future_ssj", "Trunks", "Future Super Saiyan", "Android Saga", WallpaperCategory.TRUNKS, 0xFFFFD600, 0xFF7C4DFF, 0xFF00E5FF, "剣", "You're about to find out what a real Saiyan can do!", listOf("Trunks", "MechaFrieza", "Sword")),
            CharacterProfile("trunks_rage", "Trunks", "Future SSJ Rage", "Future Trunks Arc", WallpaperCategory.TRUNKS, 0xFF00E5FF, 0xFFFFD600, 0xFFFF1744, "怒", "If wanting to save everyone is a sin, I don't care!", listOf("Trunks", "Rage", "DualAura")),
            CharacterProfile("trunks_kid", "Trunks", "Kid Capsule Corp Heir", "Buu Saga", WallpaperCategory.TRUNKS, 0xFFFFD600, 0xFF2979FF, 0xFF00E5FF, "CC", "I can turn Super Saiyan too, you know!", listOf("KidTrunks", "CapsuleCorp", "Gotenks")),
            CharacterProfile("goten_kid", "Goten", "Little Saiyan Prodigy", "Buu Saga", WallpaperCategory.Z_WARRIORS, 0xFFFFD600, 0xFFFF6D00, 0xFF00E5FF, "悟", "Kamehameha!", listOf("Goten", "GokuSon", "Prodigy")),

            // Piccolo & Namekians (60 Piccolo Wallpapers)
            CharacterProfile("piccolo_base", "Piccolo", "Special Beam Cannon", "Saiyan Saga", WallpaperCategory.PICCOLO, 0xFF76FF03, 0xFFFFD600, 0xFFFF1744, "魔", "Special Beam Cannon!", listOf("Piccolo", "Makankosappo", "Namek")),
            CharacterProfile("piccolo_super", "Piccolo", "Super Namekian Fusion", "Cell Saga", WallpaperCategory.PICCOLO, 0xFF00E676, 0xFFFF6D00, 0xFFFFFFFF, "神", "I am neither Kami nor Piccolo now!", listOf("Piccolo", "SuperNamekian", "Kami")),
            CharacterProfile("piccolo_orange", "Piccolo", "Orange Piccolo Awakened", "Super Hero", WallpaperCategory.PICCOLO, 0xFFFF6D00, 0xFFFFD600, 0xFF76FF03, "橙", "A little extra power from Shenron!", listOf("Piccolo", "OrangePiccolo", "SuperHero")),
            CharacterProfile("king_piccolo", "King Piccolo", "Great Demon King", "Classic Dragon Ball", WallpaperCategory.PICCOLO, 0xFF00C853, 0xFF7C4DFF, 0xFFFF1744, "魔", "Fear the Great Demon King Piccolo!", listOf("KingPiccolo", "Demon", "Classic")),

            // Z-Fighters & Earthlings
            CharacterProfile("master_roshi", "Master Roshi", "Max Power Buff Kamehameha", "Classic & Super", WallpaperCategory.Z_WARRIORS, 0xFFFF6D00, 0xFF00E5FF, 0xFFFFD600, "亀", "Work hard, study well, and rest plenty!", listOf("Roshi", "TurtleSchool", "MaxPower")),
            CharacterProfile("krillin", "Krillin", "Destructo Disc Ki Razor", "All Sagas", WallpaperCategory.Z_WARRIORS, 0xFFFFD600, 0xFFFF6D00, 0xFFFFFFFF, "気", "Destructo Disc! Kienzan!", listOf("Krillin", "DestructoDisc", "Brave")),
            CharacterProfile("tien_shinhan", "Tien Shinhan", "Neo Tri-Beam Kikoho", "Cell Saga", WallpaperCategory.Z_WARRIORS, 0xFFFFD600, 0xFF76FF03, 0xFFFFFFFF, "鶴", "Kikoho! Tri-Beam!", listOf("Tien", "Kikoho", "CellStall")),
            CharacterProfile("yamcha", "Yamcha", "Wolf Fang Fist", "Classic & Z", WallpaperCategory.Z_WARRIORS, 0xFFFF6D00, 0xFF2979FF, 0xFFFFFFFF, "狼", "Roga Fufu Ken! Wolf Fang Fist!", listOf("Yamcha", "WolfFang", "DesertBandit")),
            CharacterProfile("chiaotzu", "Chiaotzu", "Psychic Telekinesis", "Saiyan Saga", WallpaperCategory.Z_WARRIORS, 0xFFFFFFFF, 0xFFFF1744, 0xFF00E5FF, "餃", "Goodbye, Tien!", listOf("Chiaotzu", "Psychic", "Crane")),
            CharacterProfile("bardock", "Bardock", "Lone Saiyan Rebel", "Bardock Special", WallpaperCategory.Z_WARRIORS, 0xFFFF1744, 0xFF00E676, 0xFFFFD600, "父", "Kakarot! Carry on my will!", listOf("Bardock", "FatherOfGoku", "Rebel")),
            CharacterProfile("bardock_ssj", "Bardock", "Super Saiyan Bardock", "Episode of Bardock", WallpaperCategory.Z_WARRIORS, 0xFFFFD600, 0xFFFF1744, 0xFFFFFFFF, "父", "I'm just a Saiyan who won't be pushed around!", listOf("Bardock", "SSJ", "Legend")),

            // Villains - Frieza & Family
            CharacterProfile("frieza_final", "Frieza", "Emperor of the Universe", "Namek Saga", WallpaperCategory.VILLAINS, 0xFF7C4DFF, 0xFFFF1744, 0xFFFFFFFF, "帝", "You will perish by my hands!", listOf("Frieza", "DeathBeam", "Emperor")),
            CharacterProfile("frieza_100_percent", "Frieza", "100% Full Power Bulk", "Namek Saga", WallpaperCategory.VILLAINS, 0xFF9C27B0, 0xFFFF1744, 0xFFFFD600, "帝", "This is 100% of my full strength!", listOf("Frieza", "FullPower", "Bulky")),
            CharacterProfile("frieza_golden", "Golden Frieza", "Glittering Gold Emperor", "Resurrection F", WallpaperCategory.VILLAINS, 0xFFFFD600, 0xFF7C4DFF, 0xFFFF1744, "金", "Gold! To show how truly superior I am!", listOf("Frieza", "GoldenFrieza", "Gold")),
            CharacterProfile("frieza_black", "Black Frieza", "Supreme Reality Eraser", "Granolah Arc", WallpaperCategory.VILLAINS, 0xFF212121, 0xFF7C4DFF, 0xFFFF1744, "黒", "Ten years of training in another dimension!", listOf("Frieza", "BlackFrieza", "Amoled")),
            CharacterProfile("mecha_frieza", "Mecha Frieza", "Cybernetic Rebirth", "Trunks Saga", WallpaperCategory.VILLAINS, 0xFF9E9E9E, 0xFF7C4DFF, 0xFFFF1744, "機", "Earth will burn before my eyes!", listOf("Frieza", "Mecha", "Cyber")),
            CharacterProfile("cooler_final", "Cooler", "Armored Final Form", "Cooler's Revenge", WallpaperCategory.VILLAINS, 0xFF7C4DFF, 0xFF00E5FF, 0xFFFF1744, "冷", "The strongest in the universe is me!", listOf("Cooler", "MovieVillain", "DeathBall")),
            CharacterProfile("metal_cooler", "Metal Cooler", "Big Gete Star Clone", "Return of Cooler", WallpaperCategory.VILLAINS, 0xFF00E5FF, 0xFF90A4AE, 0xFFFFD600, "星", "The Big Gete Star enabled me to cheat death!", listOf("MetalCooler", "Clones", "Chrome")),

            // Villains - Cell & Androids
            CharacterProfile("cell_imperfect", "Cell", "Bio-Android Cocoon", "Android Saga", WallpaperCategory.VILLAINS, 0xFF00E676, 0xFFFF9100, 0xFF7C4DFF, "生", "I must absorb 17 and 18!", listOf("Cell", "Imperfect", "Bio")),
            CharacterProfile("cell_perfect", "Cell", "Perfect Lifeform", "Cell Games", WallpaperCategory.VILLAINS, 0xFF76FF03, 0xFFFFD600, 0xFF7C4DFF, "完", "I have achieved absolute perfection!", listOf("Cell", "PerfectCell", "SolarKamehameha")),
            CharacterProfile("cell_super_perfect", "Cell", "Super Perfect Lightning", "Cell Games", WallpaperCategory.VILLAINS, 0xFF00E5FF, 0xFFFFD600, 0xFF76FF03, "完", "Not only am I complete, I have Saiyan power!", listOf("Cell", "SuperPerfect", "Lightning")),
            CharacterProfile("cell_max", "Cell Max", "Colossal Red Beast", "Super Hero", WallpaperCategory.SUPER, 0xFFFF1744, 0xFFFF9100, 0xFF76FF03, "巨", "ROOOAAAAR!", listOf("CellMax", "RedBeast", "Giant")),
            CharacterProfile("android_18", "Android 18", "Infinite Energy Rebel", "Android Saga", WallpaperCategory.Z_WARRIORS, 0xFF00B0FF, 0xFFFFD600, 0xFFE040FB, "18", "Don't underestimate an Android!", listOf("Android18", "Lazuli", "InfiniteEnergy")),
            CharacterProfile("android_17", "Android 17", "Tournament MVP Ranger", "Tournament of Power", WallpaperCategory.SUPER, 0xFF00E676, 0xFF2979FF, 0xFFFFD600, "17", "I'll protect the wildlife and universe!", listOf("Android17", "Lapis", "Barrier")),
            CharacterProfile("android_16", "Android 16", "Nature Loving Colossus", "Cell Saga", WallpaperCategory.Z_WARRIORS, 0xFF00C853, 0xFFFF6D00, 0xFFFFD600, "16", "Protect the nature and birds I loved...", listOf("Android16", "RocketPunch", "Gentle")),
            CharacterProfile("android_21", "Android 21", "Majin Sweet Predator", "FighterZ", WallpaperCategory.VILLAINS, 0xFFFF4081, 0xFF7C4DFF, 0xFF00E5FF, "21", "Snack time is starting!", listOf("Android21", "Majin", "FighterZ")),

            // Villains - Majin Buu & Demons
            CharacterProfile("buu_fat", "Majin Buu", "Innocent Candy Maker", "Buu Saga", WallpaperCategory.VILLAINS, 0xFFFF80AB, 0xFFFFD600, 0xFF7C4DFF, "魔", "Buu turn you into candy!", listOf("FatBuu", "Candy", "Innocent")),
            CharacterProfile("super_buu", "Super Buu", "Vicious Extinction Bomber", "Buu Saga", WallpaperCategory.VILLAINS, 0xFFFF4081, 0xFF7C4DFF, 0xFFFFFFFF, "魔", "Human Extinction Attack!", listOf("SuperBuu", "Extinction", "Absorb")),
            CharacterProfile("kid_buu", "Kid Buu", "Planet Burst Primordial Chaos", "Buu Saga", WallpaperCategory.VILLAINS, 0xFFFF1744, 0xFFFF4081, 0xFFE040FB, "滅", "Pure unadulterated madness!", listOf("KidBuu", "PlanetBurst", "Chaos")),
            CharacterProfile("janemba", "Janemba", "Super Dimensional Demon", "Fusion Reborn", WallpaperCategory.VILLAINS, 0xFFFF1744, 0xFF7C4DFF, 0xFFFFD600, "邪", "Dimension Sword slice!", listOf("Janemba", "Demon", "Dimension")),

            // Broly
            CharacterProfile("broly_z", "Broly", "Legendary Super Saiyan Z", "Broly Movie", WallpaperCategory.VILLAINS, 0xFF76FF03, 0xFFFFD600, 0xFF00E676, "鬼", "My power is maximum! Kakarot!", listOf("Broly", "LSSJ", "ZBroly")),
            CharacterProfile("broly_super_wrath", "Broly", "Wrathful Great Ape Power", "DBS Broly", WallpaperCategory.SUPER, 0xFF00E676, 0xFFFF9100, 0xFFFF1744, "怒", "Channeling the Great Ape without transforming!", listOf("Broly", "Wrathful", "Ikari")),
            CharacterProfile("broly_super_lssj", "Broly", "Full Power Berserk Emerald", "DBS Broly", WallpaperCategory.SUPER, 0xFF00E676, 0xFF76FF03, 0xFFFFFFFF, "狂", "Raw overwhelming cosmic Saiyan strength!", listOf("Broly", "Berserk", "Emerald")),

            // Goku Black & Future Villains
            CharacterProfile("goku_black_base", "Goku Black", "Zero Mortal Plan", "Future Trunks Arc", WallpaperCategory.VILLAINS, 0xFF212121, 0xFFFF1744, 0xFFE040FB, "黒", "This pain makes me stronger!", listOf("GokuBlack", "ZeroMortals", "Zamasu")),
            CharacterProfile("goku_black_rose", "Goku Black", "Super Saiyan Rosé Scythe", "Future Trunks Arc", WallpaperCategory.VILLAINS, 0xFFFF4081, 0xFF7C4DFF, 0xFFFF80AB, "薔", "My beauty is truly divine! Rosé!", listOf("GokuBlack", "Rose", "DivineScythe")),
            CharacterProfile("zamasu_fused", "Fused Zamasu", "Divine Corrupted Light", "Future Trunks Arc", WallpaperCategory.VILLAINS, 0xFF76FF03, 0xFF7C4DFF, 0xFFFFFFFF, "神", "Praise me, revere me!", listOf("Zamasu", "FusedZamasu", "Halo")),

            // Gods, Angels & Multiverse
            CharacterProfile("beerus", "Lord Beerus", "God of Destruction Hakai", "Battle of Gods", WallpaperCategory.GODS, 0xFFAA00FF, 0xFFFF6D00, 0xFFFFD600, "破", "Before creation must come destruction. Hakai!", listOf("Beerus", "Hakai", "GodOfDestruction")),
            CharacterProfile("whis", "Whis", "Angel Attendant", "Super", WallpaperCategory.GODS, 0xFF00E5FF, 0xFFECEFF1, 0xFFFFD600, "天", "Mind and body moving autonomously!", listOf("Whis", "Angel", "Autonomous")),
            CharacterProfile("champa", "Champa", "Universe 6 Destroyer", "Super", WallpaperCategory.GODS, 0xFF7C4DFF, 0xFFFF6D00, 0xFFFFD600, "壊", "Hey Beerus, my universe has better food!", listOf("Champa", "Universe6", "Destroyer")),
            CharacterProfile("grand_priest", "Grand Priest", "Daishinkan Supreme Angel", "Super", WallpaperCategory.GODS, 0xFF00E5FF, 0xFF7C4DFF, 0xFFFFFFFF, "大", "Welcome to the Tournament of Power.", listOf("GrandPriest", "Daishinkan", "Angels")),
            CharacterProfile("zeno", "Omni-King Zeno", "Lord of Everything", "Super", WallpaperCategory.GODS, 0xFF00E5FF, 0xFFFF4081, 0xFFFFD600, "全", "Squish! All gone!", listOf("Zeno", "OmniKing", "Erasure")),

            // Tournament of Power Rivals
            CharacterProfile("jiren_base", "Jiren", "Pride Trooper Champion", "Tournament of Power", WallpaperCategory.SUPER, 0xFFFF1744, 0xFF212121, 0xFFFFFFFF, "力", "Strength is absolute!", listOf("Jiren", "PrideTrooper", "Universe11")),
            CharacterProfile("jiren_full", "Jiren", "Burning Fiery Aura Full Power", "Tournament of Power", WallpaperCategory.SUPER, 0xFFFF1744, 0xFFFF9100, 0xFFFFD600, "炎", "This heat is everything I have!", listOf("Jiren", "BurningKi", "FinalBattle")),
            CharacterProfile("toppo_god", "Toppo", "God of Destruction Mode", "Tournament of Power", WallpaperCategory.SUPER, 0xFFAA00FF, 0xFFFF1744, 0xFFD500F9, "破", "I have cast aside justice for destruction!", listOf("Toppo", "HakaiToppo", "Universe11")),
            CharacterProfile("hit_assassin", "Hit", "Time-Skip Assassin", "Universe 6", WallpaperCategory.SUPER, 0xFF7C4DFF, 0xFF00E5FF, 0xFF212121, "時", "It's time to make the donuts. Time-Skip!", listOf("Hit", "TimeSkip", "Universe6")),
            CharacterProfile("kefla_ssj2", "Kefla", "Universe 6 Potara Berserk", "Tournament of Power", WallpaperCategory.SUPER, 0xFF76FF03, 0xFFFF1744, 0xFFFFD600, "合", "I feel like I could blow away the whole universe!", listOf("Kefla", "Kale", "Caulifla")),

            // Super Manga & Super Hero
            CharacterProfile("granolah", "Granolah", "Sole Cerealian Sniper", "Granolah Arc", WallpaperCategory.SUPER, 0xFF00E5FF, 0xFF76FF03, 0xFFFFFFFF, "狙", "My right eye never misses a vitals target!", listOf("Granolah", "Cereal", "Sniper")),
            CharacterProfile("moro", "Moro", "Planet Eater Sorcerer", "Moro Arc", WallpaperCategory.SUPER, 0xFF2979FF, 0xFFFF1744, 0xFF212121, "魔", "Give me all your planet's life energy!", listOf("Moro", "PlanetEater", "Magic")),
            CharacterProfile("gamma_1", "Gamma 1", "Red Ribbon Superhero", "Super Hero", WallpaperCategory.SUPER, 0xFFFF1744, 0xFFFFD600, 0xFF00E5FF, "1", "We are real superheroes!", listOf("Gamma1", "RedRibbon", "Superhero")),
            CharacterProfile("gamma_2", "Gamma 2", "Core Breaker Hero", "Super Hero", WallpaperCategory.SUPER, 0xFF2979FF, 0xFFFFD600, 0xFFFF1744, "2", "Maximum output! Core Breaker!", listOf("Gamma2", "CoreBreaker", "Hero")),

            // GT Saga & Shadow Dragons
            CharacterProfile("baby_vegeta", "Super Baby 2", "Tuffle Revenge Ruler", "Dragon Ball GT", WallpaperCategory.GT, 0xFFECEFF1, 0xFFFFD600, 0xFF7C4DFF, "寄", "Bow before the rightful king of Tuffles!", listOf("BabyVegeta", "Tuffle", "GT")),
            CharacterProfile("super_17", "Super 17", "Hell Fighter Ultimate Fusion", "Dragon Ball GT", WallpaperCategory.GT, 0xFF212121, 0xFF00E676, 0xFFFFD600, "17", "Flash Bomber! Your ki belongs to me!", listOf("Super17", "HellFighter", "GT")),
            CharacterProfile("omega_shenron", "Omega Shenron", "Minus Energy Shadow Dragon", "Dragon Ball GT", WallpaperCategory.GT, 0xFFECEFF1, 0xFFFF1744, 0xFF212121, "邪", "Minus Energy Power Ball!", listOf("OmegaShenron", "ShadowDragon", "GT")),

            // Dragon Relics & Classic Icons
            CharacterProfile("shenron_relic", "Shenron", "Eternal Dragon Wish Granter", "Mythology", WallpaperCategory.RELICS, 0xFF00E676, 0xFFFF9100, 0xFFFFD600, "龍", "I will grant any one wish!", listOf("Shenron", "Dragon", "Wish")),
            CharacterProfile("porunga", "Porunga", "Dragon of Dreams Namek", "Namek Saga", WallpaperCategory.RELICS, 0xFF00C853, 0xFFFFD600, 0xFF2979FF, "夢", "State your three wishes!", listOf("Porunga", "NamekDragon", "3Wishes")),
            CharacterProfile("dragon_balls_7", "The 7 Dragon Balls", "Sacred Wish Orbs", "Mythology", WallpaperCategory.RELICS, 0xFFFF9100, 0xFFFFD600, 0xFFD50000, "玉", "When gathered, the sky turns to night!", listOf("DragonBalls", "7Balls", "AmberStars")),
            CharacterProfile("capsule_corp_tech", "Capsule Corp", "Future Cybernetics", "All Sagas", WallpaperCategory.AMOLED, 0xFF00E5FF, 0xFF2979FF, 0xFFE040FB, "CC", "Bulma's revolutionary Capsule Technology!", listOf("CapsuleCorp", "Tech", "Cyber")),
            CharacterProfile("time_chamber_realm", "Time Chamber", "Room of Spirit and Time", "Sacred Realm", WallpaperCategory.RELICS, 0xFFFFD600, 0xFF00E5FF, 0xFFFFFFFF, "刻", "One day outside is one year inside!", listOf("TimeChamber", "InfiniteVoid", "Training")),
            CharacterProfile("ginyu_force", "Captain Ginyu", "Special Fighting Pose", "Namek Saga", WallpaperCategory.VILLAINS, 0xFF7C4DFF, 0xFFFFD600, 0xFFFF1744, "隊", "Tokusentai! Change now!", listOf("Ginyu", "GinyuForce", "Pose")),
            CharacterProfile("raditz_nappa", "Raditz & Nappa", "Saiyan Invaders", "Saiyan Saga", WallpaperCategory.VILLAINS, 0xFFFF6D00, 0xFF2979FF, 0xFFFFD600, "侵", "Double Sunday! Your brother is a warrior!", listOf("Raditz", "Nappa", "Invaders"))
        )

        private data class EditionSpec(
            val suffix: String,
            val nameSuffix: String,
            val style: WallpaperStyle,
            val isAmoled: Boolean,
            val resolution: String,
            val tags: List<String>
        )

        private val EDITIONS: List<EditionSpec> = listOf(
            EditionSpec("face_and_body", "Face & Full Body Duo 4K", WallpaperStyle.CANVAS_FACE_AND_BODY, false, "4K Ultra HD", listOf("FaceAndBody", "Portrait", "FullBody", "4K")),
            EditionSpec("full_body_stance", "Heroic Full Body Stance 4K", WallpaperStyle.CANVAS_FULL_BODY_HERO, false, "4K Ultra HD", listOf("FullBody", "HeroicStance", "Combat", "4K")),
            EditionSpec("cinematic_face", "Fierce Face Close-Up 4K", WallpaperStyle.CANVAS_CINEMATIC_FACE, true, "4K Ultra HD", listOf("FaceCloseUp", "IntenseEyes", "Gaze", "4K")),
            EditionSpec("dual_awakening", "Dual Perspective Awakening 4K", WallpaperStyle.CANVAS_FACE_AND_BODY, true, "4K Ultra HD", listOf("DualPerspective", "Face", "Body", "Awakening")),
            EditionSpec("aura_flare", "Blazing Ki Flare Full Body", WallpaperStyle.CANVAS_AURA, false, "4K Ultra HD", listOf("Aura", "Blazing", "FullBody", "Energy")),
            EditionSpec("amoled_dual", "Midnight AMOLED Face & Body", WallpaperStyle.CANVAS_FACE_AND_BODY, true, "4K Ultra HD", listOf("Amoled", "Dark", "FaceAndBody")),
            EditionSpec("ki_sphere", "Supernova Ki Blast Stance", WallpaperStyle.CANVAS_KI_SPHERE, true, "2K Quad HD", listOf("KiSphere", "FullBody", "Supernova")),
            EditionSpec("energy_beam", "Ultimate Beam Surge Pose", WallpaperStyle.CANVAS_BEAM, true, "4K Ultra HD", listOf("Beam", "Kamehameha", "FullBody", "Surge")),
            EditionSpec("manga_sketch", "Manga Speedline Face & Body", WallpaperStyle.CANVAS_MANGA_SKETCH, true, "Full HD 1080p", listOf("Manga", "Speedline", "Monochrome", "FaceAndBody")),
            EditionSpec("cyber_scouter", "Capsule Scouter HUD & Stance", WallpaperStyle.CANVAS_CYBER_TECH, true, "2K Quad HD", listOf("Scouter", "Cyber", "FaceAndBody", "HUD")),
            EditionSpec("lightning_storm", "Limit Break Thunderstorm Body", WallpaperStyle.CANVAS_LIGHTNING_STORM, true, "4K Ultra HD", listOf("Lightning", "Electric", "FullBody", "Storm")),
            EditionSpec("cosmic_galaxy", "Celestial Galaxy Face & Stance", WallpaperStyle.CANVAS_COSMIC_VOID, true, "4K Ultra HD", listOf("Cosmic", "Galaxy", "Nebula", "FaceAndBody")),
            EditionSpec("sacred_kanji", "Sacred Martial Kanji & Body", WallpaperStyle.CANVAS_KANJI, false, "Full HD 1080p", listOf("Kanji", "MartialArts", "FullBody", "Emblem")),
            EditionSpec("wasteland_horizon", "Battlefield Horizon Stance", WallpaperStyle.CANVAS_SCENE, false, "2K Quad HD", listOf("Wasteland", "Battlefield", "FullBody")),
            EditionSpec("dragon_summon", "Shenron Star Formation Duo", WallpaperStyle.CANVAS_DRAGON_BALLS, true, "4K Ultra HD", listOf("DragonBalls", "Shenron", "FaceAndBody"))
        )

        val ALL_WALLPAPERS: List<Wallpaper> = buildList {
            // 1. Core 4 High-Res Key Visual Artworks
            add(
                Wallpaper(
                    id = "goku_ultra_instinct",
                    title = "Ultra Instinct Mastered 4K",
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
                    isTrending = true,
                    isFeatured = true,
                    description = "Son Goku achieving complete mastery of Ultra Instinct with shimmering silver aura and divine speed.",
                    resolution = "4K Ultra HD",
                    downloadsCount = 28450,
                    likesCount = 14200
                )
            )
            add(
                Wallpaper(
                    id = "vegeta_ssj_blue_evolved",
                    title = "Super Saiyan Blue Evolved 4K",
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
                    isTrending = true,
                    isFeatured = true,
                    description = "Prince Vegeta pushing past the boundaries of Super Saiyan Blue with intense sapphire aura and unbreakable pride.",
                    resolution = "4K Ultra HD",
                    downloadsCount = 24100,
                    likesCount = 12310
                )
            )
            add(
                Wallpaper(
                    id = "gohan_beast_awakening",
                    title = "Beast Gohan Awakening 4K",
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
                    isTrending = true,
                    isFeatured = true,
                    description = "Son Gohan unleashing his latent inner beast with crimson pupils, sharp lavender spikes, and furious electrical aura.",
                    resolution = "4K Ultra HD",
                    downloadsCount = 22890,
                    likesCount = 11650
                )
            )
            add(
                Wallpaper(
                    id = "shenron_eternal_dragon",
                    title = "Shenron The Eternal Dragon 4K",
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
                    isTrending = true,
                    isFeatured = true,
                    description = "The mythical green dragon Shenron soaring through stormy skies surrounded by the seven glowing Dragon Balls.",
                    resolution = "4K Ultra HD",
                    downloadsCount = 29800,
                    likesCount = 15200
                )
            )

            // 2. Generate 15 distinct editions for all 75 characters = 1,125 wallpapers
            // Total catalog will be 1,129 wallpapers!
            var counter = 1
            for (charProfile in CHARACTER_PROFILES) {
                for (edition in EDITIONS) {
                    val uniqueId = "${charProfile.id}_${edition.suffix}"
                    val uniqueTitle = "${charProfile.name} - ${edition.nameSuffix}"
                    val category = if (edition.isAmoled && counter % 3 == 0) WallpaperCategory.AMOLED else charProfile.category

                    val isTrending = (counter % 7 == 0)
                    val isFeatured = (counter % 19 == 0)

                    add(
                        Wallpaper(
                            id = uniqueId,
                            title = uniqueTitle,
                            character = charProfile.name,
                            saga = charProfile.saga,
                            form = charProfile.form,
                            category = category,
                            drawableResId = null,
                            styleType = edition.style,
                            primaryColor = charProfile.primaryColor,
                            secondaryColor = charProfile.secondaryColor,
                            accentColor = charProfile.accentColor,
                            kanjiSymbol = charProfile.kanji,
                            kanjiMeaning = charProfile.form,
                            quote = charProfile.quote,
                            tags = charProfile.tags + edition.tags,
                            isAmoled = edition.isAmoled,
                            isTrending = isTrending,
                            isFeatured = isFeatured,
                            description = "${charProfile.name} in ${charProfile.form} state during the ${charProfile.saga}. Stylized in high-definition ${edition.nameSuffix} anime wallpaper format.",
                            resolution = edition.resolution,
                            downloadsCount = 1200 + (counter * 37) % 8500,
                            likesCount = 650 + (counter * 19) % 4300,
                            createdTimestamp = System.currentTimeMillis() - (counter * 3600000L)
                        )
                    )
                    counter++
                }
            }
        }
    }
}
