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
    assertTrue("Should have extensive collection of wallpapers", wallpapers.size >= 25)

    // Verify key characters exist
    val characters = wallpapers.map { it.character }.toSet()
    assertTrue(characters.contains("Goku"))
    assertTrue(characters.contains("Vegeta"))
    assertTrue(characters.contains("Gohan"))
    assertTrue(characters.contains("Trunks"))
    assertTrue(characters.contains("Piccolo"))
    assertTrue(characters.contains("Shenron"))

    // Verify Ultra Instinct exists
    val gokuUi = wallpapers.find { it.id == "goku_ultra_instinct" }
    assertNotNull(gokuUi)
    assertEquals(WallpaperCategory.GOKU, gokuUi?.category)
  }
}
