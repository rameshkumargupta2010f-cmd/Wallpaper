package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Wallpaper

@Composable
fun WallpaperCard(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    isMultiSelectMode: Boolean = false,
    isSelectedForBatch: Boolean = false,
    onWallpaperClick: (Wallpaper) -> Unit,
    onToggleFavorite: (Wallpaper) -> Unit,
    onQuickDownload: (Wallpaper) -> Unit,
    onToggleMultiSelect: (Wallpaper) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(18.dp))
            .clickable {
                if (isMultiSelectMode) {
                    onToggleMultiSelect(wallpaper)
                } else {
                    onWallpaperClick(wallpaper)
                }
            }
            .then(
                if (isSelectedForBatch) {
                    Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                } else Modifier
            )
            .testTag("wallpaper_card_${wallpaper.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            WallpaperThumbnail(
                wallpaper = wallpaper,
                modifier = Modifier.fillMaxSize()
            )

            // Top overlay: badges & action buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.70f), Color.Transparent)
                        )
                    )
                    .padding(8.dp)
            ) {
                // Character Tag & Resolution badge
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = wallpaper.character,
                            color = Color(wallpaper.accentColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    if (wallpaper.resolution.contains("4K")) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "4K",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (wallpaper.isTrending) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Trending",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Checkbox in MultiSelect Mode OR Favorite Button
                if (isMultiSelectMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isSelectedForBatch) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.5f))
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelectedForBatch) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    val favColor by animateColorAsState(
                        targetValue = if (isFavorite) Color(0xFFFF1744) else Color.White,
                        label = "fav_color"
                    )

                    IconButton(
                        onClick = { onToggleFavorite(wallpaper) },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(34.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                            .testTag("fav_btn_${wallpaper.id}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = favColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Bottom overlay: Title and Quick Download
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                        )
                    )
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = wallpaper.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = wallpaper.form,
                            color = Color(wallpaper.primaryColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!isMultiSelectMode) {
                        IconButton(
                            onClick = { onQuickDownload(wallpaper) },
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .testTag("download_quick_${wallpaper.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Quick download wallpaper",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
