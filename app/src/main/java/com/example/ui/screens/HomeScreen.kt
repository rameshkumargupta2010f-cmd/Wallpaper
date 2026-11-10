package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HomeSection
import com.example.model.ResolutionFilter
import com.example.model.StyleFilter
import com.example.model.Wallpaper
import com.example.model.WallpaperCategory
import com.example.ui.components.WallpaperCard
import com.example.ui.components.WallpaperThumbnail

enum class WallpaperSortOrder(val title: String) {
    POPULAR("Most Popular"),
    NEWEST("Newest Added"),
    CHARACTER("Character A-Z")
}

@Composable
fun HomeScreen(
    wallpapers: List<Wallpaper>,
    favoriteIds: Set<String>,
    selectedCategory: WallpaperCategory,
    selectedSection: HomeSection,
    selectedResolution: ResolutionFilter,
    selectedStyle: StyleFilter,
    categoryCounts: Map<WallpaperCategory, Int>,
    dailyWallpaper: Wallpaper?,
    isMultiSelectMode: Boolean,
    selectedMultiIds: Set<String>,
    isBatchDownloading: Boolean,
    searchQuery: String,
    sortOrder: WallpaperSortOrder,
    onCategorySelected: (WallpaperCategory) -> Unit,
    onSectionSelected: (HomeSection) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSortOrderChanged: (WallpaperSortOrder) -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit,
    onToggleFavorite: (Wallpaper) -> Unit,
    onQuickDownload: (Wallpaper) -> Unit,
    onOpenRandomWallpaper: () -> Unit,
    onToggleMultiSelectMode: () -> Unit,
    onToggleMultiSelectItem: (String) -> Unit,
    onSelectAllVisible: (List<Wallpaper>) -> Unit,
    onDownloadBatch: () -> Unit,
    onOpenAdminStudio: () -> Unit,
    onOpenAdvancedFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val featuredWallpapers = remember(wallpapers) {
        wallpapers.filter { it.isFeatured || it.drawableResId != null }.take(8)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 4-star mini dragon ball icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD54F), Color(0xFFFF6D00), Color(0xFFD50000))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFD50000),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "DRAGON WALLPAPERS",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "1,100+ Offline Ultra HD Library",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Action Icons Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Random Wallpaper "Surprise Ki"
                IconButton(
                    onClick = onOpenRandomWallpaper,
                    modifier = Modifier.testTag("random_wallpaper_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Random Wallpaper",
                        tint = Color(0xFFFFD600)
                    )
                }

                // Advanced Filter Button
                IconButton(
                    onClick = onOpenAdvancedFilters,
                    modifier = Modifier.testTag("advanced_filters_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (selectedResolution != ResolutionFilter.ALL || selectedStyle != StyleFilter.ALL) MaterialTheme.colorScheme.primary else Color.White
                    )
                }

                // Search toggle
                IconButton(
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) onSearchQueryChanged("")
                    },
                    modifier = Modifier.testTag("toggle_search_button")
                ) {
                    Icon(
                        imageVector = if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isSearchExpanded) MaterialTheme.colorScheme.primary else Color.White
                    )
                }

                // Creator Studio / CMS
                IconButton(
                    onClick = onOpenAdminStudio,
                    modifier = Modifier.testTag("admin_studio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Creator CMS Studio",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Multi-Select Batch Actions Bar
        if (isMultiSelectMode) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedMultiIds.size} Selected",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onSelectAllVisible(wallpapers) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Select All", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = onDownloadBatch,
                            enabled = selectedMultiIds.isNotEmpty() && !isBatchDownloading,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            if (isBatchDownloading) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                            } else {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download (${selectedMultiIds.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        IconButton(
                            onClick = onToggleMultiSelectMode,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Cancel", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Search Input Bar (Expandable)
        AnimatedVisibility(
            visible = isSearchExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Search 1,100+ Goku, Vegeta, Beast, Broly, Sagas...", color = Color.Gray, fontSize = 13.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF2B3044),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_field")
                )
            }
        }

        // Main Grid with Featured Carousel Header
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Daily Spotlight Card (shown on explore feed)
            if (searchQuery.isEmpty() && dailyWallpaper != null && selectedCategory == WallpaperCategory.ALL && selectedSection == HomeSection.ALL) {
                item(span = { GridItemSpan(2) }) {
                    DailyWallpaperCard(
                        dailyWallpaper = dailyWallpaper,
                        onWallpaperClick = onWallpaperClick
                    )
                }
            }

            // Featured Carousel Header (shown when not searching)
            if (searchQuery.isEmpty() && featuredWallpapers.isNotEmpty() && selectedSection == HomeSection.ALL) {
                item(span = { GridItemSpan(2) }) {
                    FeaturedHeroCarousel(
                        featuredList = featuredWallpapers,
                        onWallpaperClick = onWallpaperClick
                    )
                }
            }

            // Collection Feed Row (All, Trending, Most Downloaded, Recent)
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeSection.values().forEach { sec ->
                        val isSelected = sec == selectedSection
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFFFF9100) else Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSectionSelected(sec) }
                        ) {
                            Text(
                                text = sec.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Category Chips Row with exact Live Counts
            item(span = { GridItemSpan(2) }) {
                CategoryChipsRow(
                    selectedCategory = selectedCategory,
                    categoryCounts = categoryCounts,
                    onCategorySelected = onCategorySelected
                )
            }

            // Results count, Multi-select trigger, and Sort
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${wallpapers.size} Wallpapers",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Multi-select toggle button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isMultiSelectMode) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onToggleMultiSelectMode() }
                        ) {
                            Text(
                                text = if (isMultiSelectMode) "Done Selecting" else "Batch Download",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                WallpaperSortOrder.values().forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = order.title,
                                                color = if (order == sortOrder) MaterialTheme.colorScheme.primary else Color.White,
                                                fontWeight = if (order == sortOrder) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            onSortOrderChanged(order)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Empty State
            if (wallpapers.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No wallpapers found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Try clearing filters or search query",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(wallpapers, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        isFavorite = favoriteIds.contains(wallpaper.id),
                        isMultiSelectMode = isMultiSelectMode,
                        isSelectedForBatch = selectedMultiIds.contains(wallpaper.id),
                        onWallpaperClick = onWallpaperClick,
                        onToggleFavorite = onToggleFavorite,
                        onQuickDownload = onQuickDownload,
                        onToggleMultiSelect = { onToggleMultiSelectItem(wallpaper.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyWallpaperCard(
    dailyWallpaper: Wallpaper,
    onWallpaperClick: (Wallpaper) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF141722),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD600)),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onWallpaperClick(dailyWallpaper) }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Thumbnail
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .fillMaxSize()
            ) {
                WallpaperThumbnail(
                    wallpaper = dailyWallpaper,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Right Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        tint = Color(0xFFFFD600),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DAILY SPOTLIGHT WALLPAPER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD600),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dailyWallpaper.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "${dailyWallpaper.character} • ${dailyWallpaper.resolution} • Tap to view",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun FeaturedHeroCarousel(
    featuredList: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { featuredList.size })

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "FEATURED SAGA LEGENDS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(end = 28.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { page ->
            val wp = featuredList[page]
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onWallpaperClick(wp) }
                    .border(1.dp, Color(0xFF2B3044), RoundedCornerShape(20.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    WallpaperThumbnail(
                        wallpaper = wp,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.align(Alignment.BottomStart)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = wp.resolution,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = wp.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${wp.character} • ${wp.saga}",
                                fontSize = 11.sp,
                                color = Color(wp.primaryColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(
    selectedCategory: WallpaperCategory,
    categoryCounts: Map<WallpaperCategory, Int>,
    onCategorySelected: (WallpaperCategory) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WallpaperCategory.values().forEach { category ->
            val isSelected = category == selectedCategory
            val count = categoryCounts[category] ?: 0
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B3044)),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onCategorySelected(category) }
                    .testTag("category_chip_${category.name}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$count",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
