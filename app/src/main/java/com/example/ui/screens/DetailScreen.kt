package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RealisticTonePreset
import com.example.model.Wallpaper
import com.example.model.WallpaperScreenTarget
import com.example.ui.components.HomescreenOverlay
import com.example.ui.components.LockscreenOverlay
import com.example.ui.components.WallpaperApplyDialog
import com.example.ui.components.WallpaperThumbnail
import com.example.util.WallpaperHelper
import com.example.util.WallpaperRenderer
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

enum class DetailPreviewMode {
    CLEAN,
    LOCKSCREEN,
    HOMESCREEN
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: (Wallpaper) -> Unit,
    onDownloadSuccess: (Wallpaper) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler {
        onBack()
    }

    // UI state
    var isUiVisible by remember { mutableStateOf(true) }
    var previewMode by remember { mutableStateOf(DetailPreviewMode.CLEAN) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showRealisticStudio by remember { mutableStateOf(true) }
    var selectedTone by remember { mutableStateOf(RealisticTonePreset.STUDIO_RAW) }
    var isDepthParallaxEnabled by remember { mutableStateOf(true) }
    var isDownloading by remember { mutableStateOf(false) }
    var isApplying by remember { mutableStateOf(false) }

    // Zoom, pan, and subtle 3D parallax offset
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Smooth atmospheric animation for 3D depth breathing & embers
    val infiniteTransition = rememberInfiniteTransition(label = "realistic_depth")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val liveColorFilter = remember(selectedTone) {
        if (selectedTone == RealisticTonePreset.STUDIO_RAW) {
            null
        } else {
            ColorFilter.colorMatrix(ColorMatrix(WallpaperRenderer.getColorMatrixArray(selectedTone)))
        }
    }

    val depthScale = if (isDepthParallaxEnabled) 1.035f + 0.015f * sin(animPhase * Math.PI * 2).toFloat() else 1f
    val depthTranslationX = if (isDepthParallaxEnabled) 8f * sin(animPhase * Math.PI * 2).toFloat() else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2.2f
                        offset = Offset.Zero
                    },
                    onTap = {
                        isUiVisible = !isUiVisible
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)
                    if (scale > 1f) {
                        offset += pan
                    } else {
                        offset = Offset.Zero
                    }
                }
            }
    ) {
        // Fullscreen Realistic Wallpaper Content with optional 3D Depth Motion & Live Color Grading
        WallpaperThumbnail(
            wallpaper = wallpaper,
            colorFilter = liveColorFilter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale * depthScale,
                    scaleY = scale * depthScale,
                    translationX = offset.x + depthTranslationX,
                    translationY = offset.y
                )
        )

        // Subtle Atmospheric Depth Rim Vignette & Ki Ember Overlay when 3D Depth is enabled
        if (isDepthParallaxEnabled) {
            RealisticAtmosphereOverlay(
                accentColor = Color(wallpaper.primaryColor),
                phase = animPhase,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Lockscreen Overlay Simulation
        if (previewMode == DetailPreviewMode.LOCKSCREEN) {
            LockscreenOverlay()
        }

        // Homescreen Overlay Simulation
        if (previewMode == DetailPreviewMode.HOMESCREEN) {
            HomescreenOverlay()
        }

        // Top App Bar
        AnimatedVisibility(
            visible = isUiVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Preview Mode Toggles (Clean, Lock, Home)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PreviewModePill(
                            label = "Clean",
                            selected = previewMode == DetailPreviewMode.CLEAN,
                            onClick = { previewMode = DetailPreviewMode.CLEAN }
                        )
                        PreviewModePill(
                            label = "Lock",
                            icon = Icons.Default.Lock,
                            selected = previewMode == DetailPreviewMode.LOCKSCREEN,
                            onClick = { previewMode = DetailPreviewMode.LOCKSCREEN }
                        )
                        PreviewModePill(
                            label = "Home",
                            icon = Icons.Default.Home,
                            selected = previewMode == DetailPreviewMode.HOMESCREEN,
                            onClick = { previewMode = DetailPreviewMode.HOMESCREEN }
                        )
                    }
                }

                // Right Controls: Realistic FX Toggle + Info
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showRealisticStudio = !showRealisticStudio },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (showRealisticStudio) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                else Color.Black.copy(alpha = 0.55f)
                            )
                            .testTag("detail_realistic_studio_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Realistic Studio Controls",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { showInfoSheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .testTag("detail_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Wallpaper Info",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Bottom Realistic Studio & Action Bar
        AnimatedVisibility(
            visible = isUiVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Realistic Studio Grading & 3D Depth Panel
                AnimatedVisibility(visible = showRealisticStudio) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF10131D).copy(alpha = 0.90f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD600),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "REALISTIC STUDIO",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFD600),
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                // 3D Depth / Atmospheric Ki Toggle Pill
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isDepthParallaxEnabled) MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                                    else Color.White.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isDepthParallaxEnabled) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { isDepthParallaxEnabled = !isDepthParallaxEnabled }
                                        .testTag("detail_depth_toggle")
                                ) {
                                    Text(
                                        text = if (isDepthParallaxEnabled) "✨ 3D Depth ON" else "3D Depth OFF",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Tone / Realistic Lighting Presets
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RealisticTonePreset.values().forEach { preset ->
                                    val isSelected = preset == selectedTone
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { selectedTone = preset }
                                            .testTag("tone_preset_${preset.name}")
                                    ) {
                                        Text(
                                            text = preset.label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Main Floating Action Bar Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF141722).copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B3044)),
                    shadowElevation = 8.dp,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Favorite Button
                        IconButton(
                            onClick = { onToggleFavorite(wallpaper) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .testTag("detail_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFFF1744) else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Download Button (Primary)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !isDownloading) {
                                    isDownloading = true
                                    scope.launch {
                                        val result = WallpaperHelper.saveWallpaperToGallery(
                                            context = context,
                                            wallpaper = wallpaper,
                                            tonePreset = selectedTone
                                        )
                                        isDownloading = false
                                        result.onSuccess {
                                            onDownloadSuccess(wallpaper)
                                            Toast.makeText(
                                                context,
                                                "Saved '${wallpaper.title}' (${selectedTone.label}) to Gallery!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }.onFailure { err ->
                                            Toast.makeText(
                                                context,
                                                "Download failed: ${err.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                                .testTag("detail_download_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Saving...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Download",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Apply as Wallpaper Button (Secondary Accent)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showApplyDialog = true }
                                .testTag("detail_apply_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wallpaper,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Apply",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Apply Dialog Sheet
        if (showApplyDialog) {
            WallpaperApplyDialog(
                wallpaper = wallpaper,
                isApplying = isApplying,
                onDismiss = { showApplyDialog = false },
                onConfirmApply = { target ->
                    isApplying = true
                    scope.launch {
                        val result = WallpaperHelper.applyWallpaper(
                            context = context,
                            wallpaper = wallpaper,
                            target = target,
                            tonePreset = selectedTone
                        )
                        isApplying = false
                        showApplyDialog = false
                        result.onSuccess {
                            val targetName = when (target) {
                                WallpaperScreenTarget.HOME_SCREEN -> "Home Screen"
                                WallpaperScreenTarget.LOCK_SCREEN -> "Lock Screen"
                                WallpaperScreenTarget.BOTH_SCREENS -> "Home & Lock Screens"
                            }
                            Toast.makeText(
                                context,
                                "Applied realistic 4K art (${selectedTone.label}) to $targetName!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.onFailure { err ->
                            Toast.makeText(context, "Apply failed: ${err.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        // Metadata Info Sheet
        if (showInfoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showInfoSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp)
                ) {
                    Text(
                        text = wallpaper.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "${wallpaper.character} • ${wallpaper.form}",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = wallpaper.description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )

                    if (wallpaper.quote.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "\"${wallpaper.quote}\"",
                                fontSize = 13.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = Color(wallpaper.accentColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecItem("Saga", wallpaper.saga)
                        SpecItem("Resolution", wallpaper.resolution)
                        SpecItem("Tone", selectedTone.badge)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "TAGS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallpaper.tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = "#$tag",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RealisticAtmosphereOverlay(
    accentColor: Color,
    phase: Float,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        val rand = Random(42)
        List(18) {
            Triple(rand.nextFloat(), rand.nextFloat(), 1.5f + rand.nextFloat() * 3.5f)
        }
    }

    Box(modifier = modifier) {
        // Soft cinematic edge vignette & rim lighting
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            accentColor.copy(alpha = 0.05f),
                            Color.Black.copy(alpha = 0.35f)
                        )
                    )
                )
        )

        // Subtle rising Ki dust embers
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEachIndexed { idx, (px, py, radius) ->
                val yProgress = (py - phase + 1f) % 1f
                val drift = sin((phase * 6.28f + idx).toDouble()).toFloat() * 14f
                val alpha = (0.15f + 0.35f * sin((yProgress * Math.PI)).toFloat()).coerceIn(0f, 0.55f)
                drawCircle(
                    color = accentColor.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(
                        x = (px * size.width + drift).coerceIn(0f, size.width),
                        y = yProgress * size.height
                    )
                )
            }
        }
    }
}

@Composable
private fun PreviewModePill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
