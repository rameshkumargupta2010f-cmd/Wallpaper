package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.WallpaperRepository
import com.example.model.WallpaperCategory
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
  fun `verify wallpapers catalog has wide variety across all categories`() {
    val wallpapers = WallpaperRepository.ALL_WALLPAPERS
    assertTrue("Should have more than 1000 wallpapers", wallpapers.size >= 1000)

    // Verify key characters exist
    val characters = wallpapers.map { it.character }.toSet()
    assertTrue(characters.contains("Goku"))
    assertTrue(characters.contains("Vegeta"))
    assertTrue(characters.contains("Gohan"))
    assertTrue(characters.contains("Trunks") || characters.any { it.contains("Trunks") })
    assertTrue(characters.contains("Piccolo"))
    assertTrue(characters.contains("Shenron"))

    // Verify Ultra Instinct exists
    val gokuUi = wallpapers.find { it.id == "goku_ultra_instinct" }
    assertNotNull(gokuUi)
    assertEquals(WallpaperCategory.GOKU, gokuUi?.category)
  }

  @Test
  fun `verify repository dynamic CMS and utility features`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = WallpaperRepository(context)

    // Test random wallpaper
    val randomWp = repo.getRandomWallpaper()
    assertNotNull(randomWp)

    // Test daily wallpaper
    val dailyWp = repo.getDailyWallpaper()
    assertNotNull(dailyWp)

    // Test category counts
    val counts = repo.getCategoryCounts()
    assertTrue(counts.isNotEmpty())
    assertTrue(counts[WallpaperCategory.ALL] ?: 0 >= 1000)
    assertTrue(counts[WallpaperCategory.GOKU] ?: 0 >= 50)
    assertTrue(counts[WallpaperCategory.VEGETA] ?: 0 >= 50)
  }
}
