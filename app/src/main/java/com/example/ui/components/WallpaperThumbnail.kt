package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.model.Wallpaper
import com.example.util.WallpaperRenderer

@Composable
fun WallpaperThumbnail(
    wallpaper: Wallpaper,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null
) {
    Box(modifier = modifier) {
        if (wallpaper.drawableResId != null) {
            Image(
                painter = painterResource(id = wallpaper.drawableResId),
                contentDescription = wallpaper.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = colorFilter
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width.toInt().coerceAtLeast(1)
                val height = size.height.toInt().coerceAtLeast(1)
                drawIntoCanvas { canvas ->
                    WallpaperRenderer.renderToCanvas(canvas.nativeCanvas, wallpaper, width, height)
                }
            }
        }
    }
}
