package com.example.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WallpaperRepository
import com.example.model.HomeSection
import com.example.model.ResolutionFilter
import com.example.model.StyleFilter
import com.example.model.Wallpaper
import com.example.model.WallpaperCategory
import com.example.model.WallpaperStyle
import com.example.ui.screens.WallpaperSortOrder
import com.example.util.WallpaperHelper
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
    val selectedSection: HomeSection = HomeSection.ALL,
    val selectedResolution: ResolutionFilter = ResolutionFilter.ALL,
    val selectedStyle: StyleFilter = StyleFilter.ALL,
    val searchQuery: String = "",
    val sortOrder: WallpaperSortOrder = WallpaperSortOrder.POPULAR,
    val detailWallpaper: Wallpaper? = null,
    val dailyWallpaper: Wallpaper? = null,
    val categoryCounts: Map<WallpaperCategory, Int> = emptyMap(),
    val isMultiSelectMode: Boolean = false,
    val selectedMultiIds: Set<String> = emptySet(),
    val isBatchDownloading: Boolean = false,
    val showAdminStudio: Boolean = false,
    val showAdvancedFilters: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WallpaperRepository(application)

    private val _currentTab = MutableStateFlow(AppTab.EXPLORE)
    private val _selectedCategory = MutableStateFlow(WallpaperCategory.ALL)
    private val _selectedSection = MutableStateFlow(HomeSection.ALL)
    private val _selectedResolution = MutableStateFlow(ResolutionFilter.ALL)
    private val _selectedStyle = MutableStateFlow(StyleFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(WallpaperSortOrder.POPULAR)
    private val _detailWallpaper = MutableStateFlow<Wallpaper?>(null)

    // Multi-select batch download mode
    private val _isMultiSelectMode = MutableStateFlow(false)
    private val _selectedMultiIds = MutableStateFlow<Set<String>>(emptySet())
    private val _isBatchDownloading = MutableStateFlow(false)

    // Dialog sheets
    private val _showAdminStudio = MutableStateFlow(false)
    private val _showAdvancedFilters = MutableStateFlow(false)

    private data class FilterConfig(
        val category: WallpaperCategory,
        val section: HomeSection,
        val resolution: ResolutionFilter,
        val style: StyleFilter
    )

    private data class SearchSortConfig(
        val query: String,
        val sort: WallpaperSortOrder
    )

    private data class InteractionConfig(
        val tab: AppTab,
        val detail: Wallpaper?,
        val isMultiSelect: Boolean,
        val selectedMultiIds: Set<String>,
        val isBatchDownloading: Boolean
    )

    private data class SheetConfig(
        val showAdmin: Boolean,
        val showFilters: Boolean
    )

    private val filterConfigFlow = combine(
        _selectedCategory,
        _selectedSection,
        _selectedResolution,
        _selectedStyle
    ) { cat, sec, res, sty ->
        FilterConfig(cat, sec, res, sty)
    }

    private val searchSortConfigFlow = combine(
        _searchQuery,
        _sortOrder
    ) { query, sort ->
        SearchSortConfig(query, sort)
    }

    // Filter flow combines catalog filters
    private val filteredWallpapersFlow: StateFlow<List<Wallpaper>> = combine(
        repository.wallpapersFlow,
        filterConfigFlow,
        searchSortConfigFlow
    ) { allWps, filters, searchSort ->
        val cat = filters.category
        val section = filters.section
        val res = filters.resolution
        val style = filters.style
        val query = searchSort.query
        val sort = searchSort.sort

        val filtered = allWps.filter { wp ->
            // Category filter
            val matchesCategory = when (cat) {
                WallpaperCategory.ALL -> true
                WallpaperCategory.AMOLED -> wp.isAmoled
                else -> wp.category == cat
            }

            // Section filter (Trending, Most Downloaded, Recent)
            val matchesSection = when (section) {
                HomeSection.ALL -> true
                HomeSection.TRENDING -> wp.isTrending || wp.downloadsCount > 5000
                HomeSection.MOST_DOWNLOADED -> wp.downloadsCount > 4000
                HomeSection.RECENT -> wp.createdTimestamp > (System.currentTimeMillis() - 7 * 86400000L)
                else -> true
            }

            // Resolution filter
            val matchesResolution = when (res) {
                ResolutionFilter.ALL -> true
                ResolutionFilter.RES_4K -> wp.resolution.contains("4K")
                ResolutionFilter.RES_2K -> wp.resolution.contains("2K")
                ResolutionFilter.RES_FHD -> wp.resolution.contains("1080p") || wp.resolution.contains("Full HD")
                else -> true
            }

            // Style filter
            val matchesStyle = when (style) {
                StyleFilter.ALL -> true
                StyleFilter.FACE_AND_BODY -> wp.styleType == WallpaperStyle.CANVAS_FACE_AND_BODY
                StyleFilter.FULL_BODY -> wp.styleType == WallpaperStyle.CANVAS_FULL_BODY_HERO
                StyleFilter.FACE_PORTRAIT -> wp.styleType == WallpaperStyle.CANVAS_CINEMATIC_FACE
                StyleFilter.AURA -> wp.styleType == WallpaperStyle.CANVAS_AURA
                StyleFilter.AMOLED -> wp.isAmoled
                StyleFilter.BEAM -> wp.styleType == WallpaperStyle.CANVAS_BEAM
                StyleFilter.KI_SPHERE -> wp.styleType == WallpaperStyle.CANVAS_KI_SPHERE
                StyleFilter.MANGA -> wp.styleType == WallpaperStyle.CANVAS_MANGA_SKETCH
                StyleFilter.CYBER -> wp.styleType == WallpaperStyle.CANVAS_CYBER_TECH
                StyleFilter.LIGHTNING -> wp.styleType == WallpaperStyle.CANVAS_LIGHTNING_STORM
                StyleFilter.COSMIC -> wp.styleType == WallpaperStyle.CANVAS_COSMIC_VOID
                else -> true
            }

            // Search query filter
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

            matchesCategory && matchesSection && matchesResolution && matchesStyle && matchesQuery
        }

        when (sort) {
            WallpaperSortOrder.POPULAR -> filtered.sortedByDescending { it.downloadsCount }
            WallpaperSortOrder.NEWEST -> filtered.sortedByDescending { it.createdTimestamp }
            WallpaperSortOrder.CHARACTER -> filtered.sortedBy { it.character }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getAllWallpapers())

    private val interactionConfigFlow = combine(
        _currentTab,
        _detailWallpaper,
        _isMultiSelectMode,
        _selectedMultiIds,
        _isBatchDownloading
    ) { tab, detail, multi, ids, downloading ->
        InteractionConfig(tab, detail, multi, ids, downloading)
    }

    private val sheetConfigFlow = combine(
        _showAdminStudio,
        _showAdvancedFilters
    ) { admin, filters ->
        SheetConfig(admin, filters)
    }

    private val idsConfigFlow = combine(
        repository.favoriteIdsFlow,
        repository.downloadedIdsFlow
    ) { favs, downloads ->
        Pair(favs, downloads)
    }

    // Final UI State
    val uiState: StateFlow<MainUiState> = combine(
        filteredWallpapersFlow,
        repository.wallpapersFlow,
        idsConfigFlow,
        interactionConfigFlow,
        sheetConfigFlow
    ) { filtered, allWps, ids, interaction, sheets ->
        MainUiState(
            allWallpapers = allWps,
            filteredWallpapers = filtered,
            favoriteIds = ids.first,
            downloadedIds = ids.second,
            currentTab = interaction.tab,
            selectedCategory = _selectedCategory.value,
            selectedSection = _selectedSection.value,
            selectedResolution = _selectedResolution.value,
            selectedStyle = _selectedStyle.value,
            searchQuery = _searchQuery.value,
            sortOrder = _sortOrder.value,
            detailWallpaper = interaction.detail,
            dailyWallpaper = repository.getDailyWallpaper(),
            categoryCounts = repository.getCategoryCounts(),
            isMultiSelectMode = interaction.isMultiSelect,
            selectedMultiIds = interaction.selectedMultiIds,
            isBatchDownloading = interaction.isBatchDownloading,
            showAdminStudio = sheets.showAdmin,
            showAdvancedFilters = sheets.showFilters
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MainUiState(
            allWallpapers = repository.getAllWallpapers(),
            filteredWallpapers = repository.getAllWallpapers(),
            dailyWallpaper = repository.getDailyWallpaper(),
            categoryCounts = repository.getCategoryCounts()
        )
    )

    fun onSelectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun onSelectCategory(category: WallpaperCategory) {
        _selectedCategory.value = category
    }

    fun onSelectSection(section: HomeSection) {
        _selectedSection.value = section
    }

    fun onSelectResolution(res: ResolutionFilter) {
        _selectedResolution.value = res
    }

    fun onSelectStyle(style: StyleFilter) {
        _selectedStyle.value = style
    }

    fun onResetAllFilters() {
        _selectedCategory.value = WallpaperCategory.ALL
        _selectedSection.value = HomeSection.ALL
        _selectedResolution.value = ResolutionFilter.ALL
        _selectedStyle.value = StyleFilter.ALL
        _searchQuery.value = ""
        _sortOrder.value = WallpaperSortOrder.POPULAR
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

    fun openRandomWallpaper() {
        val randomWp = repository.getRandomWallpaper()
        openDetail(randomWp)
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        repository.toggleFavorite(wallpaper.id)
    }

    fun recordDownload(wallpaper: Wallpaper) {
        repository.recordDownload(wallpaper.id)
    }

    // Multi-select batch download actions
    fun toggleMultiSelectMode() {
        _isMultiSelectMode.value = !_isMultiSelectMode.value
        if (!_isMultiSelectMode.value) {
            _selectedMultiIds.value = emptySet()
        }
    }

    fun toggleMultiSelectItem(id: String) {
        val current = _selectedMultiIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedMultiIds.value = current
    }

    fun selectAllVisible(wallpapers: List<Wallpaper>) {
        _selectedMultiIds.value = wallpapers.map { it.id }.toSet()
    }

    fun clearMultiSelect() {
        _selectedMultiIds.value = emptySet()
        _isMultiSelectMode.value = false
    }

    fun downloadBatchSelected(context: Context) {
        val selectedIds = _selectedMultiIds.value
        if (selectedIds.isEmpty()) return

        val all = repository.getAllWallpapers()
        val targets = all.filter { selectedIds.contains(it.id) }

        _isBatchDownloading.value = true
        viewModelScope.launch {
            var successCount = 0
            targets.forEach { wp ->
                val res = WallpaperHelper.saveWallpaperToGallery(context, wp)
                if (res.isSuccess) {
                    successCount++
                    repository.recordDownload(wp.id)
                }
            }
            _isBatchDownloading.value = false
            clearMultiSelect()
            Toast.makeText(
                context,
                "Batch Downloaded $successCount / ${targets.size} wallpapers to Pictures/DragonWallpapers!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // CMS Actions
    fun openAdminStudio() {
        _showAdminStudio.value = true
    }

    fun closeAdminStudio() {
        _showAdminStudio.value = false
    }

    fun openAdvancedFilters() {
        _showAdvancedFilters.value = true
    }

    fun closeAdvancedFilters() {
        _showAdvancedFilters.value = false
    }

    fun addCustomWallpaper(wallpaper: Wallpaper) {
        repository.addCustomWallpaper(wallpaper)
    }

    fun deleteWallpaper(wallpaperId: String) {
        repository.deleteWallpaper(wallpaperId)
    }

    fun toggleFeatured(wallpaperId: String) {
        repository.toggleFeatured(wallpaperId)
    }

    fun toggleTrending(wallpaperId: String) {
        repository.toggleTrending(wallpaperId)
    }
}
