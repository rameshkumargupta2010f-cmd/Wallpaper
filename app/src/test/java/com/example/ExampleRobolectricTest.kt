package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.WallpaperRepository
import com.example.model.RealisticTonePreset
import com.example.model.WallpaperCategory
import com.example.model.WallpaperStyle
import com.example.util.WallpaperRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Dragon Wallpapers", appName)
    }

    @Test
    fun `verify every single wallpaper uses a real realistic artwork drawable`() {
        val wallpapers = WallpaperRepository.ALL_WALLPAPERS
        assertTrue("Curated realistic collection should have 24 artworks", wallpapers.size == 24)

        // Ensure 100% of wallpapers are backed by real image drawables (zero cartoon procedural items)
        wallpapers.forEach { wp ->
            assertNotNull("Wallpaper ${wp.id} must have a realistic drawableResId", wp.drawableResId)
            assertEquals("Wallpaper ${wp.id} must be DRAWABLE_ASSET", WallpaperStyle.DRAWABLE_ASSET, wp.styleType)
        }

        // Verify iconic characters exist
        val characters = wallpapers.map { it.character }.toSet()
        assertTrue(characters.contains("Goku"))
        assertTrue(characters.contains("Vegeta"))
        assertTrue(characters.contains("Gohan"))
        assertTrue(characters.contains("Future Trunks"))
        assertTrue(characters.contains("Piccolo"))
        assertTrue(characters.contains("Vegito"))
        assertTrue(characters.contains("Gogeta"))
        assertTrue(characters.contains("Broly"))
        assertTrue(characters.contains("Frieza"))
        assertTrue(characters.contains("Perfect Cell"))
        assertTrue(characters.contains("Kid Buu"))
        assertTrue(characters.contains("Beerus"))
        assertTrue(characters.contains("Whis"))
        assertTrue(characters.contains("Jiren"))
        assertTrue(characters.contains("Goku Black"))
        assertTrue(characters.contains("Bardock"))
        assertTrue(characters.contains("Shenron"))
    }

    @Test
    fun `verify repository categories and utility features`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = WallpaperRepository(context)

        val randomWp = repo.getRandomWallpaper()
        assertNotNull(randomWp)

        val dailyWp = repo.getDailyWallpaper()
        assertNotNull(dailyWp)

        // Verify every single category has at least 1 realistic wallpaper
        val counts = repo.getCategoryCounts()
        WallpaperCategory.values().forEach { cat ->
            val count = counts[cat] ?: 0
            assertTrue("Category ${cat.name} must have at least 1 realistic wallpaper", count >= 1)
        }
    }

    @Test
    fun `verify full body realistic key visuals load cleanly and support realistic tone grading`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val targets = listOf(
            "goku_ui_full_body_cinematic",
            "trunks_full_body_sword_cinematic",
            "vegito_blue_sword_realistic",
            "piccolo_orange_awakened",
            "frieza_black_emperor",
            "beerus_hakai_destroyer",
            "vegeta_ultra_ego_realistic",
            "jiren_full_power_crimson"
        )

        targets.forEach { id ->
            val wp = WallpaperRepository.ALL_WALLPAPERS.find { it.id == id }
            assertNotNull("Wallpaper $id must exist", wp)
            val thumb = WallpaperRenderer.getThumbnailBitmap(context, wp!!)
            assertNotNull(thumb)
            assertEquals(270, thumb.width)
            assertEquals(480, thumb.height)
        }

        val sample = WallpaperRepository.ALL_WALLPAPERS.first()
        RealisticTonePreset.values().forEach { preset ->
            val graded = WallpaperRenderer.createFullResolutionBitmap(
                context = context,
                wallpaper = sample,
                width = 540,
                height = 960,
                tonePreset = preset
            )
            assertNotNull(graded)
            assertEquals(540, graded.width)
            assertEquals(960, graded.height)
        }
    }
}
