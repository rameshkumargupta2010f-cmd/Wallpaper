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
        val resolvedDrawable = wallpaper.drawableResId ?: resolveRealisticDrawableForCharacter(wallpaper.character, wallpaper.category)
        val enriched = wallpaper.copy(
            drawableResId = resolvedDrawable,
            styleType = WallpaperStyle.DRAWABLE_ASSET
        )
        val current = _customWallpapers.value.toMutableList()
        current.add(0, enriched)
        _customWallpapers.value = current
        saveCustomWallpapersToPrefs(current)
        updateWallpapers()
    }

    fun deleteWallpaper(wallpaperId: String) {
        val currentDeleted = _deletedIds.value.toMutableSet()
        currentDeleted.add(wallpaperId)
        _deletedIds.value = currentDeleted
        prefs.edit().putStringSet("cms_deleted_wallpapers", currentDeleted).apply()

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
        val all = getAllWallpapers().ifEmpty { ALL_WALLPAPERS }
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return all[dayOfYear % all.size]
    }

    fun getRandomWallpaper(): Wallpaper {
        val all = getAllWallpapers().ifEmpty { ALL_WALLPAPERS }
        return all.random()
    }

    fun getCategoryCounts(): Map<WallpaperCategory, Int> {
        val all = getAllWallpapers()
        val counts = mutableMapOf<WallpaperCategory, Int>()
        WallpaperCategory.values().forEach { cat ->
            counts[cat] = when (cat) {
                WallpaperCategory.ALL -> all.size
                WallpaperCategory.AMOLED -> all.count { it.isAmoled }
                else -> all.count { it.category == cat }
            }
        }
        return counts
    }

    private fun resolveRealisticDrawableForCharacter(character: String, category: WallpaperCategory): Int {
        val c = character.lowercase()
        return when {
            c.contains("trunks") -> R.drawable.img_wallpaper_trunks_full_body
            c.contains("vegeta") && c.contains("ego") -> R.drawable.img_wallpaper_vegeta_ultra_ego
            c.contains("majin") -> R.drawable.img_wallpaper_majin_vegeta
            c.contains("vegeta") -> R.drawable.img_wallpaper_vegeta_blue
            c.contains("beast") -> R.drawable.img_wallpaper_gohan_beast
            c.contains("gohan") -> R.drawable.img_wallpaper_gohan_ssj2
            c.contains("gogeta") -> R.drawable.img_wallpaper_gogeta_blue
            c.contains("vegito") -> R.drawable.img_wallpaper_vegito_blue
            c.contains("piccolo") -> R.drawable.img_wallpaper_piccolo_orange
            c.contains("broly") -> R.drawable.img_wallpaper_broly_lssj
            c.contains("frieza") -> R.drawable.img_wallpaper_frieza_black
            c.contains("cell") -> R.drawable.img_wallpaper_perfect_cell
            c.contains("buu") -> R.drawable.img_wallpaper_kid_buu
            c.contains("beerus") -> R.drawable.img_wallpaper_beerus_hakai
            c.contains("whis") -> R.drawable.img_wallpaper_whis_angel
            c.contains("jiren") -> R.drawable.img_wallpaper_jiren_full_power
            c.contains("black") || c.contains("rose") -> R.drawable.img_wallpaper_goku_black_rose
            c.contains("bardock") -> R.drawable.img_wallpaper_bardock_saiyan
            c.contains("17") || c.contains("18") -> R.drawable.img_wallpaper_android_17_18
            c.contains("shenron") -> R.drawable.img_wallpaper_shenron
            c.contains("ssj4") || category == WallpaperCategory.GT -> R.drawable.img_wallpaper_goku_ssj4
            c.contains("ssj3") -> R.drawable.img_wallpaper_goku_ssj3
            else -> R.drawable.img_wallpaper_goku_full_body
        }
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
                val charName = obj.getString("character")
                val storedRes = obj.optInt("drawableResId", 0)
                val drawableId = if (storedRes != 0) storedRes else resolveRealisticDrawableForCharacter(charName, category)

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
                        character = charName,
                        saga = obj.optString("saga", "Custom Upload"),
                        form = obj.optString("form", "Custom Form"),
                        category = category,
                        drawableResId = drawableId,
                        styleType = WallpaperStyle.DRAWABLE_ASSET,
                        primaryColor = obj.optLong("primaryColor", 0xFFFF6D00),
                        secondaryColor = obj.optLong("secondaryColor", 0xFF00B0FF),
                        accentColor = obj.optLong("accentColor", 0xFFFFD600),
                        kanjiSymbol = obj.optString("kanjiSymbol", "氣"),
                        quote = obj.optString("quote", ""),
                        tags = tags,
                        isAmoled = obj.optBoolean("isAmoled", false),
                        isTrending = obj.optBoolean("isTrending", false),
                        isFeatured = obj.optBoolean("isFeatured", false),
                        description = obj.optString("description", "Realistic custom artwork curated in Creator Studio."),
                        resolution = obj.optString("resolution", "4K Ultra HD"),
                        downloadsCount = obj.optInt("downloadsCount", 0),
                        likesCount = obj.optInt("likesCount", 0)
                    )
                )
            }
            list
        } catch (_: Exception) {
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
                    put("drawableResId", wp.drawableResId ?: 0)
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
        private val NOW = System.currentTimeMillis()

        /**
         * Curated 100% Realistic Cinematic Anime Collection.
         * Every single wallpaper is a high-definition, realistic artwork — zero cartoon placeholders.
         */
        val ALL_WALLPAPERS: List<Wallpaper> = listOf(
            Wallpaper(
                id = "goku_ui_full_body_cinematic",
                title = "Son Goku Ultra Instinct Full Body 4K",
                character = "Goku",
                saga = "Tournament of Power",
                form = "Mastered Ultra Instinct",
                category = WallpaperCategory.GOKU,
                drawableResId = R.drawable.img_wallpaper_goku_full_body,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFECEFF1,
                secondaryColor = 0xFF00E5FF,
                accentColor = 0xFFFFD600,
                quote = "There are boundaries... and I will shatter them all!",
                tags = listOf("Goku", "Ultra Instinct", "FullBody", "Realistic", "GodKi", "4K"),
                isAmoled = true,
                isTrending = true,
                isFeatured = true,
                description = "Hyper-realistic full-body artwork of Son Goku standing head-to-toe in Mastered Ultra Instinct on floating dimensional rocks with divine silver Ki.",
                resolution = "4K Ultra HD",
                downloadsCount = 42100,
                likesCount = 21500,
                createdTimestamp = NOW - 3600000L
            ),
            Wallpaper(
                id = "vegeta_ultra_ego_realistic",
                title = "Vegeta Ultra Ego Destroyer 4K",
                character = "Vegeta",
                saga = "Granolah Saga",
                form = "Ultra Ego",
                category = WallpaperCategory.VEGETA,
                drawableResId = R.drawable.img_wallpaper_vegeta_ultra_ego,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFD500F9,
                secondaryColor = 0xFF651FFF,
                accentColor = 0xFFFFD600,
                quote = "Destruction is my greatest power!",
                tags = listOf("Vegeta", "Ultra Ego", "Hakai", "FullBody", "Realistic", "4K"),
                isAmoled = true,
                isTrending = true,
                isFeatured = true,
                description = "Full-body photorealistic anime portrait of Vegeta unleashing amethyst God of Destruction Hakai aura amidst cosmic ruin.",
                resolution = "4K Ultra HD",
                downloadsCount = 39800,
                likesCount = 20400,
                createdTimestamp = NOW - 2 * 3600000L
            ),
            Wallpaper(
                id = "trunks_full_body_sword_cinematic",
                title = "Future Trunks Warrior Stance 4K",
                character = "Future Trunks",
                saga = "Android Saga",
                form = "Super Saiyan",
                category = WallpaperCategory.TRUNKS,
                drawableResId = R.drawable.img_wallpaper_trunks_full_body,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFFFD600,
                secondaryColor = 0xFF3D5AFE,
                accentColor = 0xFFFF9100,
                quote = "I have come to change our cruel destiny!",
                tags = listOf("Trunks", "Future Trunks", "Sword", "FullBody", "Realistic", "4K"),
                isAmoled = true,
                isTrending = true,
                isFeatured = true,
                description = "Cinematic realistic full-body artwork of Future Trunks standing resolutely with broadsword drawn in an apocalyptic city with golden Super Saiyan aura.",
                resolution = "4K Ultra HD",
                downloadsCount = 38900,
                likesCount = 19700,
                createdTimestamp = NOW - 3 * 3600000L
            ),
            Wallpaper(
                id = "gogeta_blue_stardust",
                title = "Gogeta Blue Stardust Breaker 4K",
                character = "Gogeta",
                saga = "DBS: Broly",
                form = "Super Saiyan Blue",
                category = WallpaperCategory.FUSIONS,
                drawableResId = R.drawable.img_wallpaper_gogeta_blue,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFF00E5FF,
                secondaryColor = 0xFF0091EA,
                accentColor = 0xFFFFD600,
                quote = "Every strike from a Super Saiyan is justice!",
                tags = listOf("Gogeta", "SSJ Blue", "Fusion", "FullBody", "Realistic", "4K"),
                isAmoled = true,
                isTrending = true,
                isFeatured = true,
                description = "Supreme fusion Gogeta manifesting godly power in Super Saiyan Blue in ultra-detailed cinematic anime movie realism.",
                resolution = "4K Ultra HD",
                downloadsCount = 36400,
                likesCount = 18500,
                createdTimestamp = NOW - 4 * 3600000L
            ),
            Wallpaper(
                id = "broly_legendary_berserker",
                title = "Broly Legendary Berserker 4K",
                character = "Broly",
                saga = "DBS: Broly",
                form = "Legendary Super Saiyan",
                category = WallpaperCategory.VILLAINS,
                drawableResId = R.drawable.img_wallpaper_broly_lssj,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFF76FF03,
                secondaryColor = 0xFF00E676,
                accentColor = 0xFFFFD600,
                quote = "My power is rising... OVERFLOWING!",
                tags = listOf("Broly", "LSSJ", "Legendary", "FullBody", "Villain", "Realistic", "4K"),
                isAmoled = true,
                isTrending = true,
                isFeatured = true,
                description = "The unstoppable legendary titan Broly unleashing cataclysmic emerald energy in a shattered wasteland with photorealistic rim lighting.",
                resolution = "4K Ultra HD",
                downloadsCount = 35200,
                likesCount = 18200,
                createdTimestamp = NOW - 5 * 3600000L
            ),
            Wallpaper(
                id = "vegito_blue_sword_realistic",
                title = "Vegito Blue Spirit Sword 4K",
                character = "Vegito",
                saga = "Future Trunks Arc",
                form = "Super Saiyan Blue",
                category = WallpaperCategory.FUSIONS,
                drawableResId = R.drawable.img_wallpaper_vegito_blue,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFF00E5FF,
                secondaryColor = 0xFF2979FF,
                accentColor = 0xFFFFD600,
                quote = "Alright! Come at me with everything you've got!",
                tags = listOf("Vegito", "SSJ Blue", "Spirit Sword", "Fusion", "FullBody", "Realistic", "4K"),
                isAmoled = true,
                isTrending = true,
                isFeatured = true,
                description = "Potara fusion Vegito Blue standing full-body in combat stance wielding his luminous azure Spirit Sword with crackling blue lightning.",
                resolution = "4K Ultra HD",
                downloadsCount = 33900,
                likesCount = 17100,
                createdTimestamp = NOW - 6 * 3600000L
            ),
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
                tags = listOf("Gohan", "Beast", "GodKi", "Realistic", "4K"),
                isAmoled = true,
                isTrending = true,
                isFeatured = true,
                description = "Son Gohan unleashing his latent Beast form with piercing crimson eyes, razor silver-lavender spikes, and furious magenta lightning.",
                resolution = "4K Ultra HD",
                downloadsCount = 32800,
                likesCount = 16600,
                createdTimestamp = NOW - 7 * 3600000L
            ),
            Wallpaper(
                id = "piccolo_orange_awakened",
                title = "Orange Piccolo Awakened 4K",
                character = "Piccolo",
                saga = "Super Hero",
                form = "Orange Piccolo",
                category = WallpaperCategory.PICCOLO,
                drawableResId = R.drawable.img_wallpaper_piccolo_orange,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFFF6D00,
                secondaryColor = 0xFFFFAB00,
                accentColor = 0xFFFFD600,
                quote = "Shenron gave me a little extra!",
                tags = listOf("Piccolo", "Orange Piccolo", "Namekian", "FullBody", "Realistic", "4K"),
                isAmoled = false,
                isTrending = true,
                isFeatured = true,
                description = "Full-body realistic depiction of Orange Piccolo towering over rugged cliffs engulfed in blazing amber-orange Namekian flame aura.",
                resolution = "4K Ultra HD",
                downloadsCount = 30400,
                likesCount = 15400,
                createdTimestamp = NOW - 8 * 3600000L
            ),
            Wallpaper(
                id = "frieza_black_emperor",
                title = "Black Frieza Cosmic Emperor 4K",
                character = "Frieza",
                saga = "Granolah Saga",
                form = "Black Frieza",
                category = WallpaperCategory.VILLAINS,
                drawableResId = R.drawable.img_wallpaper_frieza_black,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFAB47BC,
                secondaryColor = 0xFFFFD600,
                accentColor = 0xFFFF1744,
                quote = "Observe the true pinnacle of the universe!",
                tags = listOf("Frieza", "Black Frieza", "Villain", "FullBody", "Realistic", "4K"),
                isAmoled = true,
                isTrending = true,
                isFeatured = true,
                description = "Menacing full-body realistic render of Black Frieza hovering in obsidian metallic bio-armor above a shattered alien world.",
                resolution = "4K Ultra HD",
                downloadsCount = 31500,
                likesCount = 15900,
                createdTimestamp = NOW - 9 * 3600000L
            ),
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
                tags = listOf("Shenron", "Dragon Balls", "GodKi", "Realistic", "4K"),
                isAmoled = true,
                isTrending = true,
                isFeatured = true,
                description = "Photorealistic emerald-scaled Eternal Dragon Shenron coiling through thunderclouds with the seven glowing Dragon Balls.",
                resolution = "4K Ultra HD",
                downloadsCount = 29800,
                likesCount = 15200,
                createdTimestamp = NOW - 10 * 3600000L
            ),
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
                tags = listOf("Goku", "Ultra Instinct", "GodKi", "Realistic", "4K"),
                isAmoled = true,
                isTrending = true,
                isFeatured = true,
                description = "Son Goku achieving complete mastery of Ultra Instinct with radiant silver aura and atmospheric divine lighting.",
                resolution = "4K Ultra HD",
                downloadsCount = 28450,
                likesCount = 14200,
                createdTimestamp = NOW - 11 * 3600000L
            ),
            Wallpaper(
                id = "goku_black_rose_scythe",
                title = "Goku Black Super Saiyan Rosé 4K",
                character = "Goku Black",
                saga = "Future Trunks Arc",
                form = "Super Saiyan Rosé",
                category = WallpaperCategory.SUPER,
                drawableResId = R.drawable.img_wallpaper_goku_black_rose,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFFF4081,
                secondaryColor = 0xFFAA00FF,
                accentColor = 0xFFFF80AB,
                quote = "What do you think of this color? Is it not beautiful?",
                tags = listOf("Goku Black", "Rose", "FullBody", "Villain", "Realistic", "4K"),
                isAmoled = true,
                isTrending = true,
                isFeatured = false,
                description = "Full-body realistic Goku Black Super Saiyan Rosé floating above rain-slicked ruins wielding a blazing magenta Ki scythe.",
                resolution = "4K Ultra HD",
                downloadsCount = 27600,
                likesCount = 13900,
                createdTimestamp = NOW - 12 * 3600000L
            ),
            Wallpaper(
                id = "majin_vegeta_realistic",
                title = "Majin Vegeta Final Atonement 4K",
                character = "Vegeta",
                saga = "Buu Saga",
                form = "Majin Super Saiyan 2",
                category = WallpaperCategory.VEGETA,
                drawableResId = R.drawable.img_wallpaper_majin_vegeta,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFFFD600,
                secondaryColor = 0xFF00E5FF,
                accentColor = 0xFFFFFFFF,
                quote = "There is one thing a Saiyan always keeps: his PRIDE!",
                tags = listOf("Majin Vegeta", "Vegeta", "SSJ2", "FullBody", "Realistic", "4K"),
                isAmoled = false,
                isTrending = true,
                isFeatured = false,
                description = "Full-body hyper-realistic Majin Vegeta standing in rocky canyon twilight surrounded by blazing gold Ki and intense lightning.",
                resolution = "4K Ultra HD",
                downloadsCount = 26900,
                likesCount = 13500,
                createdTimestamp = NOW - 13 * 3600000L
            ),
            Wallpaper(
                id = "goku_ssj4_gt_realistic",
                title = "Goku Super Saiyan 4 Primal Rage 4K",
                character = "Goku (GT)",
                saga = "Dragon Ball GT",
                form = "Super Saiyan 4",
                category = WallpaperCategory.GT,
                drawableResId = R.drawable.img_wallpaper_goku_ssj4,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFFF1744,
                secondaryColor = 0xFFFF9100,
                accentColor = 0xFFFFD600,
                quote = "The ultimate primal fusion of Saiyan spirit!",
                tags = listOf("Goku", "SSJ4", "GT", "FullBody", "Realistic", "4K"),
                isAmoled = false,
                isTrending = true,
                isFeatured = false,
                description = "Realistic full-body artwork of Super Saiyan 4 Goku standing on volcanic obsidian stone with detailed crimson fur and glowing golden eyes.",
                resolution = "4K Ultra HD",
                downloadsCount = 26100,
                likesCount = 13100,
                createdTimestamp = NOW - 14 * 3600000L
            ),
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
                tags = listOf("Vegeta", "SSJ Blue", "GodKi", "Realistic", "4K"),
                isAmoled = true,
                isTrending = false,
                isFeatured = false,
                description = "Prince Vegeta pushing past the limits of Super Saiyan Blue with sapphire royal Ki and photorealistic energy arcs.",
                resolution = "4K Ultra HD",
                downloadsCount = 24800,
                likesCount = 12310,
                createdTimestamp = NOW - 15 * 3600000L
            ),
            Wallpaper(
                id = "jiren_full_power_crimson",
                title = "Jiren Full Power Inferno 4K",
                character = "Jiren",
                saga = "Tournament of Power",
                form = "Full Power",
                category = WallpaperCategory.SUPER,
                drawableResId = R.drawable.img_wallpaper_jiren_full_power,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFFF1744,
                secondaryColor = 0xFFFF6D00,
                accentColor = 0xFFFFD600,
                quote = "Strength is absolute!",
                tags = listOf("Jiren", "Pride Troopers", "FullBody", "Villain", "Realistic", "4K"),
                isAmoled = true,
                isTrending = false,
                isFeatured = false,
                description = "Full-body realistic Jiren standing atop shattered Tournament arena ruins engulfed in colossal crimson heat-wave fire.",
                resolution = "4K Ultra HD",
                downloadsCount = 23900,
                likesCount = 11900,
                createdTimestamp = NOW - 16 * 3600000L
            ),
            Wallpaper(
                id = "gohan_ssj2_teen_realistic",
                title = "Teen Gohan Super Saiyan 2 4K",
                character = "Gohan",
                saga = "Cell Games",
                form = "Super Saiyan 2",
                category = WallpaperCategory.GOHAN,
                drawableResId = R.drawable.img_wallpaper_gohan_ssj2,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFFFD600,
                secondaryColor = 0xFF00E5FF,
                accentColor = 0xFFFFFFFF,
                quote = "I have had enough... NOW YOU WILL KNOW MY WRATH!",
                tags = listOf("Gohan", "Teen Gohan", "SSJ2", "FullBody", "Realistic", "4K"),
                isAmoled = false,
                isTrending = false,
                isFeatured = false,
                description = "Full-body cinematic Teen Gohan SSJ2 standing in thunderstorm rain with billowing white cape and fierce blue electric aura.",
                resolution = "4K Ultra HD",
                downloadsCount = 23400,
                likesCount = 11700,
                createdTimestamp = NOW - 17 * 3600000L
            ),
            Wallpaper(
                id = "beerus_hakai_destroyer",
                title = "Beerus God of Destruction 4K",
                character = "Beerus",
                saga = "Battle of Gods",
                form = "God of Destruction",
                category = WallpaperCategory.GODS,
                drawableResId = R.drawable.img_wallpaper_beerus_hakai,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFF9C27B0,
                secondaryColor = 0xFFFFD600,
                accentColor = 0xFFE040FB,
                quote = "Before creation must come destruction.",
                tags = listOf("Beerus", "Hakai", "GodKi", "FullBody", "Realistic", "4K"),
                isAmoled = true,
                isTrending = false,
                isFeatured = false,
                description = "Full-body photorealistic Beerus standing on ancient celestial ruins channeling an ominous purple Hakai destruction sphere.",
                resolution = "4K Ultra HD",
                downloadsCount = 22500,
                likesCount = 11200,
                createdTimestamp = NOW - 18 * 3600000L
            ),
            Wallpaper(
                id = "goku_ssj3_realistic",
                title = "Goku Super Saiyan 3 Dragon Fist 4K",
                character = "Goku",
                saga = "Buu Saga",
                form = "Super Saiyan 3",
                category = WallpaperCategory.GOKU,
                drawableResId = R.drawable.img_wallpaper_goku_ssj3,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFFFAB00,
                secondaryColor = 0xFF00E5FF,
                accentColor = 0xFFFFEA00,
                quote = "And this... is to go even further BEYOND!",
                tags = listOf("Goku", "SSJ3", "FullBody", "Realistic", "4K"),
                isAmoled = false,
                isTrending = false,
                isFeatured = false,
                description = "Full-body cinematic Super Saiyan 3 Goku surrounded by roaring golden Ki tornado and crackling cyan lightning.",
                resolution = "4K Ultra HD",
                downloadsCount = 21900,
                likesCount = 10900,
                createdTimestamp = NOW - 19 * 3600000L
            ),
            Wallpaper(
                id = "perfect_cell_bio_arena",
                title = "Perfect Cell Bio-Armor 4K",
                character = "Perfect Cell",
                saga = "Cell Games",
                form = "Perfect Form",
                category = WallpaperCategory.VILLAINS,
                drawableResId = R.drawable.img_wallpaper_perfect_cell,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFF00E676,
                secondaryColor = 0xFFFFD600,
                accentColor = 0xFF69F0AE,
                quote = "Perfection cannot be defeated!",
                tags = listOf("Cell", "Perfect Cell", "Villain", "FullBody", "Realistic", "4K"),
                isAmoled = false,
                isTrending = false,
                isFeatured = false,
                description = "Full-body hyper-detailed Perfect Cell standing in the shattered Cell Games arena with glossy emerald exoskeleton and golden bio-aura.",
                resolution = "4K Ultra HD",
                downloadsCount = 20800,
                likesCount = 10200,
                createdTimestamp = NOW - 20 * 3600000L
            ),
            Wallpaper(
                id = "kid_buu_planet_burst",
                title = "Kid Buu Planet Burst 4K",
                character = "Kid Buu",
                saga = "Buu Saga",
                form = "Pure Majin",
                category = WallpaperCategory.VILLAINS,
                drawableResId = R.drawable.img_wallpaper_kid_buu,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFFF4081,
                secondaryColor = 0xFFFF1744,
                accentColor = 0xFFFF80AB,
                quote = "Pure chaos given form!",
                tags = listOf("Kid Buu", "Majin Buu", "Villain", "FullBody", "Realistic", "4K"),
                isAmoled = true,
                isTrending = false,
                isFeatured = false,
                description = "Full-body realistic Kid Buu floating amidst exploding planetary debris while summoning a molten pink Planet Burst.",
                resolution = "4K Ultra HD",
                downloadsCount = 19700,
                likesCount = 9800,
                createdTimestamp = NOW - 21 * 3600000L
            ),
            Wallpaper(
                id = "whis_angel_attendant",
                title = "Whis Cosmic Angel 4K",
                character = "Whis",
                saga = "Universe 7",
                form = "Angel Attendant",
                category = WallpaperCategory.GODS,
                drawableResId = R.drawable.img_wallpaper_whis_angel,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFF40C4FF,
                secondaryColor = 0xFFE0F7FA,
                accentColor = 0xFFFFD740,
                quote = "Allow your body to react on its own.",
                tags = listOf("Whis", "Angel", "GodKi", "FullBody", "Realistic", "4K"),
                isAmoled = false,
                isTrending = false,
                isFeatured = false,
                description = "Graceful full-body realistic render of Whis floating on celestial nebula clouds with his luminous golden scepter.",
                resolution = "4K Ultra HD",
                downloadsCount = 18600,
                likesCount = 9200,
                createdTimestamp = NOW - 22 * 3600000L
            ),
            Wallpaper(
                id = "bardock_rebel_realistic",
                title = "Bardock Rebel of Planet Vegeta 4K",
                character = "Bardock",
                saga = "Episode of Bardock",
                form = "Saiyan Warrior",
                category = WallpaperCategory.Z_WARRIORS,
                drawableResId = R.drawable.img_wallpaper_bardock_saiyan,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFFFF5252,
                secondaryColor = 0xFF29B6F6,
                accentColor = 0xFFFFD600,
                quote = "Everything is about to change! Kakarot will avenge us!",
                tags = listOf("Bardock", "Saiyan", "Z-Warriors", "FullBody", "Realistic", "4K"),
                isAmoled = true,
                isTrending = false,
                isFeatured = false,
                description = "Heroic full-body realistic artwork of Bardock in battle-worn Saiyan armor charging a blazing azure Riot Javelin above Planet Vegeta.",
                resolution = "4K Ultra HD",
                downloadsCount = 18100,
                likesCount = 8900,
                createdTimestamp = NOW - 23 * 3600000L
            ),
            Wallpaper(
                id = "android_17_18_cyborg_duo",
                title = "Android 17 & 18 Twin Cyborgs 4K",
                character = "Android 17 & 18",
                saga = "Android / ToP Saga",
                form = "Infinite Energy Twins",
                category = WallpaperCategory.Z_WARRIORS,
                drawableResId = R.drawable.img_wallpaper_android_17_18,
                styleType = WallpaperStyle.DRAWABLE_ASSET,
                primaryColor = 0xFF00E676,
                secondaryColor = 0xFF00E5FF,
                accentColor = 0xFFFFD600,
                quote = "Infinite energy. Unstoppable synergy.",
                tags = listOf("Android 17", "Android 18", "Z-Warriors", "FullBody", "Realistic", "4K"),
                isAmoled = false,
                isTrending = false,
                isFeatured = false,
                description = "Realistic full-body duo render of Android 17 and Android 18 standing back-to-back on a shattered highway with glowing emerald & cyan energy.",
                resolution = "4K Ultra HD",
                downloadsCount = 17500,
                likesCount = 8600,
                createdTimestamp = NOW - 24 * 3600000L
            )
        )
    }
}
