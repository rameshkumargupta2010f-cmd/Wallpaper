package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Wallpaper
import com.example.model.WallpaperCategory
import com.example.model.WallpaperStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudioSheet(
    totalWallpapers: List<Wallpaper>,
    onDismiss: () -> Unit,
    onAddWallpaper: (Wallpaper) -> Unit,
    onDeleteWallpaper: (String) -> Unit,
    onToggleFeatured: (String) -> Unit,
    onToggleTrending: (String) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Upload, 1: Manage & Analytics

    // New Wallpaper Form State
    var title by remember { mutableStateOf("") }
    var character by remember { mutableStateOf("Goku") }
    var form by remember { mutableStateOf("Super Saiyan Blue") }
    var saga by remember { mutableStateOf("Tournament of Power") }
    var selectedCategory by remember { mutableStateOf(WallpaperCategory.GOKU) }
    var selectedStyle by remember { mutableStateOf(WallpaperStyle.CANVAS_AURA) }
    var resolution by remember { mutableStateOf("4K Ultra HD") }
    var quote by remember { mutableStateOf("") }
    var kanji by remember { mutableStateOf("氣") }
    var isAmoled by remember { mutableStateOf(false) }
    var isTrending by remember { mutableStateOf(true) }
    var isFeatured by remember { mutableStateOf(true) }
    var publishSuccessMsg by remember { mutableStateOf<String?>(null) }

    // Search query in manage tab
    var manageSearchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F111A),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CREATOR & CMS DASHBOARD",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Live Content Management • ${totalWallpapers.size} Total Wallpapers",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF141722),
                contentColor = Color.White,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload / Add New", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manage & Analytics", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Upload / Add New Wallpaper Form
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        if (publishSuccessMsg != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF00C853).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00C853)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E676))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = publishSuccessMsg ?: "",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        CmsTextField(
                            label = "Wallpaper Title",
                            value = title,
                            placeholder = "e.g. Goku Ultra Instinct Omen Nova",
                            onValueChange = { title = it }
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                CmsTextField(
                                    label = "Character",
                                    value = character,
                                    placeholder = "e.g. Goku, Vegeta, Broly",
                                    onValueChange = { character = it }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CmsTextField(
                                    label = "Transformation / Form",
                                    value = form,
                                    placeholder = "e.g. Beast, Ultra Ego, SSJ3",
                                    onValueChange = { form = it }
                                )
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                CmsTextField(
                                    label = "Saga / Arc",
                                    value = saga,
                                    placeholder = "e.g. Granolah Arc, Cell Games",
                                    onValueChange = { saga = it }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CmsTextField(
                                    label = "Kanji / Symbol",
                                    value = kanji,
                                    placeholder = "e.g. 悟, 魔, M, 破",
                                    onValueChange = { kanji = it }
                                )
                            }
                        }
                    }

                    item {
                        CmsTextField(
                            label = "Warrior Quote (Optional)",
                            value = quote,
                            placeholder = "e.g. A Saiyan has no limits!",
                            onValueChange = { quote = it }
                        )
                    }

                    // Category Selector
                    item {
                        Text("Category", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WallpaperCategory.values().filterNot { it == WallpaperCategory.ALL }.forEach { cat ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedCategory == cat) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedCategory = cat }
                                ) {
                                    Text(
                                        text = cat.displayName,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Art Style Selector
                    item {
                        Text("Art Style Engine", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WallpaperStyle.values().filterNot { it == WallpaperStyle.DRAWABLE_ASSET }.forEach { style ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedStyle == style) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedStyle = style }
                                ) {
                                    Text(
                                        text = style.label,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Resolution Selector
                    item {
                        Text("Resolution", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("4K Ultra HD", "2K Quad HD", "Full HD 1080p").forEach { res ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (resolution == res) Color(0xFFFF9100) else Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { resolution = res }
                                ) {
                                    Text(
                                        text = res,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Toggles (AMOLED, Trending, Featured)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterCheckboxPill(
                                label = "AMOLED Black",
                                checked = isAmoled,
                                onToggle = { isAmoled = !isAmoled }
                            )
                            FilterCheckboxPill(
                                label = "Mark Trending",
                                checked = isTrending,
                                onToggle = { isTrending = !isTrending }
                            )
                            FilterCheckboxPill(
                                label = "Feature in Banner",
                                checked = isFeatured,
                                onToggle = { isFeatured = !isFeatured }
                            )
                        }
                    }

                    // Publish Button
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (title.isBlank()) title = "$character $form Ultra"
                                val newWp = Wallpaper(
                                    id = "custom_${System.currentTimeMillis()}",
                                    title = title,
                                    character = character.trim().ifEmpty { "Goku" },
                                    saga = saga.trim().ifEmpty { "Custom Arc" },
                                    form = form.trim().ifEmpty { "Ascended" },
                                    category = selectedCategory,
                                    styleType = selectedStyle,
                                    primaryColor = 0xFFFF6D00,
                                    secondaryColor = 0xFF00B0FF,
                                    accentColor = 0xFFFFD600,
                                    kanjiSymbol = kanji.ifEmpty { "氣" },
                                    quote = quote,
                                    tags = listOf(character, form, saga, "Custom", "4K"),
                                    isAmoled = isAmoled,
                                    isTrending = isTrending,
                                    isFeatured = isFeatured,
                                    description = "Custom uploaded wallpaper for $character in $form form.",
                                    resolution = resolution
                                )
                                onAddWallpaper(newWp)
                                publishSuccessMsg = "Successfully added '$title' to active library!"
                                title = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("cms_publish_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish Wallpaper to Library", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Manage & Analytics Tab
                Column(modifier = Modifier.fillMaxSize()) {
                    // Analytics Stats Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard("Total Library", "${totalWallpapers.size}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                        StatCard("Trending Items", "${totalWallpapers.count { it.isTrending }}", Color(0xFFFF5252), Modifier.weight(1f))
                        StatCard("Featured", "${totalWallpapers.count { it.isFeatured }}", Color(0xFFFFD600), Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search in Manage
                    OutlinedTextField(
                        value = manageSearchQuery,
                        onValueChange = { manageSearchQuery = it },
                        placeholder = { Text("Search catalog to delete or feature...", fontSize = 12.sp, color = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF141722),
                            unfocusedContainerColor = Color(0xFF141722),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredManageList = remember(totalWallpapers, manageSearchQuery) {
                        if (manageSearchQuery.isBlank()) {
                            totalWallpapers.take(40)
                        } else {
                            val q = manageSearchQuery.trim().lowercase()
                            totalWallpapers.filter { it.title.lowercase().contains(q) || it.character.lowercase().contains(q) }.take(40)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredManageList, key = { it.id }) { wp ->
                            ManageWallpaperRow(
                                wallpaper = wp,
                                onToggleFeatured = { onToggleFeatured(wp.id) },
                                onToggleTrending = { onToggleTrending(wp.id) },
                                onDelete = { onDeleteWallpaper(wp.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CmsTextField(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFF555B6E)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF141722),
                unfocusedContainerColor = Color(0xFF141722),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFF2B3044),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FilterCheckboxPill(label: String, checked: Boolean, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (checked) MaterialTheme.colorScheme.primary else Color(0xFF2B3044)),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (checked) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (checked) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141722)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, color = accent, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ManageWallpaperRow(
    wallpaper: Wallpaper,
    onToggleFeatured: () -> Unit,
    onToggleTrending: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF141722),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wallpaper.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${wallpaper.character} • ${wallpaper.resolution}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleTrending, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Trending",
                        tint = if (wallpaper.isTrending) Color(0xFFFF5252) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onToggleFeatured, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Featured",
                        tint = if (wallpaper.isFeatured) Color(0xFFFFD600) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF1744).copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
