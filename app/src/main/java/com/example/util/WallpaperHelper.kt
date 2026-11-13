package com.example.util

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.model.Wallpaper
import com.example.model.WallpaperScreenTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object WallpaperHelper {

    private const val TAG = "WallpaperHelper"

    /**
     * Downloads and saves the wallpaper to the device's public Pictures/DragonWallpapers folder.
     * Complies with modern Android scoped storage policies (zero permissions needed on Android 10+).
     */
    suspend fun saveWallpaperToGallery(
        context: Context,
        wallpaper: Wallpaper,
        tonePreset: com.example.model.RealisticTonePreset = com.example.model.RealisticTonePreset.STUDIO_RAW
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val bitmap = WallpaperRenderer.createFullResolutionBitmap(
                context = context,
                wallpaper = wallpaper,
                width = 1440,
                height = 2560,
                tonePreset = tonePreset
            )
            val fileName = "DragonBall_${sanitize(wallpaper.character)}_${sanitize(wallpaper.title)}_${System.currentTimeMillis()}.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/DragonWallpapers")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(Exception("Could not create MediaStore record"))

                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                } ?: return@withContext Result.failure(Exception("Could not open output stream"))

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Result.success(uri)
            } else {
                // Fallback for pre-Android 10
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val folder = File(picturesDir, "DragonWallpapers")
                if (!folder.exists()) folder.mkdirs()
                val file = File(folder, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                val uri = Uri.fromFile(file)
                val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                    data = uri
                }
                context.sendBroadcast(scanIntent)
                Result.success(uri)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save wallpaper: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Applies the wallpaper directly to Home Screen, Lock Screen, or Both.
     */
    suspend fun applyWallpaper(
        context: Context,
        wallpaper: Wallpaper,
        target: WallpaperScreenTarget,
        tonePreset: com.example.model.RealisticTonePreset = com.example.model.RealisticTonePreset.STUDIO_RAW
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bitmap = WallpaperRenderer.createFullResolutionBitmap(
                context = context,
                wallpaper = wallpaper,
                width = 1440,
                height = 2560,
                tonePreset = tonePreset
            )
            val wallpaperManager = WallpaperManager.getInstance(context)

            val flag = when (target) {
                WallpaperScreenTarget.HOME_SCREEN -> WallpaperManager.FLAG_SYSTEM
                WallpaperScreenTarget.LOCK_SCREEN -> WallpaperManager.FLAG_LOCK
                WallpaperScreenTarget.BOTH_SCREENS -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            wallpaperManager.setBitmap(bitmap, null, true, flag)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply wallpaper: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun sanitize(str: String): String {
        return str.replace("[^a-zA-Z0-9]".toRegex(), "_").take(24)
    }
}
