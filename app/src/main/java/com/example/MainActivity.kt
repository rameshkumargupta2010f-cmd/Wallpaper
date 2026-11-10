package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Wallpaper
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.AdminStudioSheet
import com.example.ui.components.AdvancedFilterSheet
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.DownloadedScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.DragonWallpapersTheme
import com.example.util.WallpaperHelper
import kotlinx.coroutines.launch

import androidx.compose.material3.ExperimentalMaterial3Api

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DragonWallpapersTheme {
                DragonWallpapersApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DragonWallpapersApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val onQuickDownload: (Wallpaper) -> Unit = { wallpaper ->
        scope.launch {
            Toast.makeText(context, "Saving '${wallpaper.title}' to Gallery...", Toast.LENGTH_SHORT).show()
            val result = WallpaperHelper.saveWallpaperToGallery(context, wallpaper)
            result.onSuccess {
                viewModel.recordDownload(wallpaper)
                Toast.makeText(
                    context,
                    "Saved to Pictures/DragonWallpapers!",
                    Toast.LENGTH_LONG
                ).show()
            }.onFailure { err ->
                Toast.makeText(context, "Download failed: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Detail Screen (full bleed)
    if (uiState.detailWallpaper != null) {
        val currentWp = uiState.detailWallpaper!!
        DetailScreen(
            wallpaper = currentWp,
            isFavorite = uiState.favoriteIds.contains(currentWp.id),
            onBack = { viewModel.closeDetail() },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onDownloadSuccess = { viewModel.recordDownload(it) }
        )
    } else {
        // Back handler: Return to EXPLORE tab if currently on FAVORITES or DOWNLOADS
        BackHandler(enabled = uiState.currentTab != AppTab.EXPLORE) {
            viewModel.onSelectTab(AppTab.EXPLORE)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    // Explore Tab
                    NavigationBarItem(
                        selected = uiState.currentTab == AppTab.EXPLORE,
                        onClick = { viewModel.onSelectTab(AppTab.EXPLORE) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = "Explore"
                            )
                        },
                        label = {
                            Text(
                                text = "Explore",
                                fontWeight = if (uiState.currentTab == AppTab.EXPLORE) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_explore")
                    )

                    // Favorites Tab
                    NavigationBarItem(
                        selected = uiState.currentTab == AppTab.FAVORITES,
                        onClick = { viewModel.onSelectTab(AppTab.FAVORITES) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (uiState.favoriteIds.isNotEmpty()) {
                                        Badge(
                                            containerColor = Color(0xFFFF1744),
                                            contentColor = Color.White
                                        ) {
                                            Text(text = uiState.favoriteIds.size.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favorites"
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Favorites",
                                fontWeight = if (uiState.currentTab == AppTab.FAVORITES) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_favorites")
                    )

                    // Downloads Tab
                    NavigationBarItem(
                        selected = uiState.currentTab == AppTab.DOWNLOADS,
                        onClick = { viewModel.onSelectTab(AppTab.DOWNLOADS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (uiState.downloadedIds.isNotEmpty()) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = uiState.downloadedIds.size.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DownloadDone,
                                    contentDescription = "Downloads"
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Saved",
                                fontWeight = if (uiState.currentTab == AppTab.DOWNLOADS) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_downloads")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = uiState.currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        AppTab.EXPLORE -> {
                            HomeScreen(
                                wallpapers = uiState.filteredWallpapers,
                                favoriteIds = uiState.favoriteIds,
                                selectedCategory = uiState.selectedCategory,
                                selectedSection = uiState.selectedSection,
                                selectedResolution = uiState.selectedResolution,
                                selectedStyle = uiState.selectedStyle,
                                categoryCounts = uiState.categoryCounts,
                                dailyWallpaper = uiState.dailyWallpaper,
                                isMultiSelectMode = uiState.isMultiSelectMode,
                                selectedMultiIds = uiState.selectedMultiIds,
                                isBatchDownloading = uiState.isBatchDownloading,
                                searchQuery = uiState.searchQuery,
                                sortOrder = uiState.sortOrder,
                                onCategorySelected = { viewModel.onSelectCategory(it) },
                                onSectionSelected = { viewModel.onSelectSection(it) },
                                onSearchQueryChanged = { viewModel.onSearchQueryChange(it) },
                                onSortOrderChanged = { viewModel.onSortOrderChange(it) },
                                onWallpaperClick = { viewModel.openDetail(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onQuickDownload = onQuickDownload,
                                onOpenRandomWallpaper = { viewModel.openRandomWallpaper() },
                                onToggleMultiSelectMode = { viewModel.toggleMultiSelectMode() },
                                onToggleMultiSelectItem = { viewModel.toggleMultiSelectItem(it) },
                                onSelectAllVisible = { viewModel.selectAllVisible(it) },
                                onDownloadBatch = { viewModel.downloadBatchSelected(context) },
                                onOpenAdminStudio = { viewModel.openAdminStudio() },
                                onOpenAdvancedFilters = { viewModel.openAdvancedFilters() }
                            )
                        }

                        AppTab.FAVORITES -> {
                            val favorites = uiState.allWallpapers.filter { uiState.favoriteIds.contains(it.id) }
                            FavoritesScreen(
                                favoriteWallpapers = favorites,
                                onWallpaperClick = { viewModel.openDetail(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onQuickDownload = onQuickDownload
                            )
                        }

                        AppTab.DOWNLOADS -> {
                            val downloaded = uiState.allWallpapers.filter { uiState.downloadedIds.contains(it.id) }
                            DownloadedScreen(
                                downloadedWallpapers = downloaded,
                                favoriteIds = uiState.favoriteIds,
                                onWallpaperClick = { viewModel.openDetail(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onQuickDownload = onQuickDownload
                            )
                        }
                    }
                }
            }
        }

        // Admin Studio CMS Sheet
        if (uiState.showAdminStudio) {
            AdminStudioSheet(
                totalWallpapers = uiState.allWallpapers,
                onDismiss = { viewModel.closeAdminStudio() },
                onAddWallpaper = { viewModel.addCustomWallpaper(it) },
                onDeleteWallpaper = { viewModel.deleteWallpaper(it) },
                onToggleFeatured = { viewModel.toggleFeatured(it) },
                onToggleTrending = { viewModel.toggleTrending(it) }
            )
        }

        // Advanced Filter & Categorization Sheet
        if (uiState.showAdvancedFilters) {
            AdvancedFilterSheet(
                selectedCategory = uiState.selectedCategory,
                selectedSection = uiState.selectedSection,
                selectedResolution = uiState.selectedResolution,
                selectedStyle = uiState.selectedStyle,
                categoryCounts = uiState.categoryCounts,
                onCategorySelected = { viewModel.onSelectCategory(it) },
                onSectionSelected = { viewModel.onSelectSection(it) },
                onResolutionSelected = { viewModel.onSelectResolution(it) },
                onStyleSelected = { viewModel.onSelectStyle(it) },
                onResetFilters = { viewModel.onResetAllFilters() },
                onDismiss = { viewModel.closeAdvancedFilters() }
            )
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = Color.White.copy(alpha = 0.6f),
    unselectedTextColor = Color.White.copy(alpha = 0.6f),
    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
)
