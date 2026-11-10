package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WallpaperRepository
import com.example.model.Wallpaper
import com.example.model.WallpaperCategory
import com.example.ui.screens.WallpaperSortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    EXPLORE("Explore"),
    FAVORITES("Favorites"),
    DOWNLOADS("Downloads")
}

data class MainUiState(
    val allWallpapers: List<Wallpaper> = emptyList(),
    val filteredWallpapers: List<Wallpaper> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val downloadedIds: Set<String> = emptySet(),
    val currentTab: AppTab = AppTab.EXPLORE,
    val selectedCategory: WallpaperCategory = WallpaperCategory.ALL,
    val searchQuery: String = "",
    val sortOrder: WallpaperSortOrder = WallpaperSortOrder.POPULAR,
    val detailWallpaper: Wallpaper? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WallpaperRepository(application)
    private val allWallpapersList: List<Wallpaper> = repository.getAllWallpapers()

    private val _currentTab = MutableStateFlow(AppTab.EXPLORE)
    private val _selectedCategory = MutableStateFlow(WallpaperCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(WallpaperSortOrder.POPULAR)
    private val _detailWallpaper = MutableStateFlow<Wallpaper?>(null)

    // Filter flow combines category, search query, and sort order
    private val filteredWallpapersFlow: StateFlow<List<Wallpaper>> = combine(
        _selectedCategory,
        _searchQuery,
        _sortOrder
    ) { cat, query, sort ->
        val filtered = allWallpapersList.filter { wp ->
            val matchesCategory = when (cat) {
                WallpaperCategory.ALL -> true
                WallpaperCategory.AMOLED -> wp.isAmoled
                else -> wp.category == cat
            }

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                wp.title.lowercase().contains(q) ||
                        wp.character.lowercase().contains(q) ||
                        wp.saga.lowercase().contains(q) ||
                        wp.form.lowercase().contains(q) ||
                        wp.tags.any { it.lowercase().contains(q) }
            }

            matchesCategory && matchesQuery
        }

        when (sort) {
            WallpaperSortOrder.POPULAR -> filtered.sortedByDescending { it.downloadsCount }
            WallpaperSortOrder.NEWEST -> filtered.reversed()
            WallpaperSortOrder.CHARACTER -> filtered.sortedBy { it.character }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allWallpapersList)

    // Final UI State combines filtered wallpapers, favorites, downloads, current tab, and detail
    val uiState: StateFlow<MainUiState> = combine(
        filteredWallpapersFlow,
        repository.favoriteIdsFlow,
        repository.downloadedIdsFlow,
        _currentTab,
        _detailWallpaper
    ) { filtered, favIds, downIds, tab, detailWp ->
        MainUiState(
            allWallpapers = allWallpapersList,
            filteredWallpapers = filtered,
            favoriteIds = favIds,
            downloadedIds = downIds,
            currentTab = tab,
            selectedCategory = _selectedCategory.value,
            searchQuery = _searchQuery.value,
            sortOrder = _sortOrder.value,
            detailWallpaper = detailWp
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MainUiState(allWallpapers = allWallpapersList, filteredWallpapers = allWallpapersList)
    )

    fun onSelectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun onSelectCategory(category: WallpaperCategory) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChange(order: WallpaperSortOrder) {
        _sortOrder.value = order
    }

    fun openDetail(wallpaper: Wallpaper) {
        _detailWallpaper.value = wallpaper
    }

    fun closeDetail() {
        _detailWallpaper.value = null
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        repository.toggleFavorite(wallpaper.id)
    }

    fun recordDownload(wallpaper: Wallpaper) {
        repository.recordDownload(wallpaper.id)
    }
}
