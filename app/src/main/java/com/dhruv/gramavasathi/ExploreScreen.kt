package com.dhruv.gramavasathi

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dhruv.gramavasathi.ui.theme.Terracotta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(navController: NavController, farmStays: List<FarmStay>, isLoading: Boolean) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedActivities = remember { mutableStateListOf<String>() }
    val activities = listOf(
        "Farm Activities",
        "Animal Interaction",
        "Local Cooking",
        "Nature Walk",
        "Birdwatching",
        "Fishing",
        "Village Walk",
        "Yoga & Meditation",
        "Plantation Walk",
        "Organic Farming",
        "Garden Walk",
        "Heritage Living",
        "Rural Craft",
        "Orchard Walk"
    )

    // Dynamic counts for activities
    val activityCounts = remember(farmStays) {
        activities.associateWith { activity ->
            farmStays.count { it.activities.contains(activity) }
        }
    }

    val filteredStays = farmStays.filter { stay ->
        val matchesSearch = stay.name.contains(searchQuery, ignoreCase = true) ||
                stay.location.contains(searchQuery, ignoreCase = true)
        val matchesActivities = if (selectedActivities.isEmpty()) {
            true
        } else {
            stay.activities.any { it in selectedActivities }
        }
        matchesSearch && matchesActivities
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Filter by Activities",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
                ) {
                    items(activities) { activity ->
                        val isSelected = selectedActivities.contains(activity)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedActivities.remove(activity)
                                } else {
                                    selectedActivities.add(activity)
                                }
                            },
                            label = { 
                                val count = activityCounts[activity] ?: 0
                                Text(
                                    text = if (isSelected) "$activity ($count)" else activity,
                                    style = MaterialTheme.typography.bodySmall
                                ) 
                            },
                            leadingIcon = {
                                val icon = when (activity) {
                                    "Organic Farming" -> Icons.Default.Eco
                                    "Garden Walk" -> Icons.Default.Yard
                                    "Heritage Living" -> Icons.Default.Gite
                                    "Birdwatching" -> Icons.Default.Visibility
                                    "Village Walk" -> Icons.AutoMirrored.Filled.DirectionsWalk
                                    "Rural Craft" -> Icons.Default.Brush
                                    "Orchard Walk" -> Icons.Default.Nature
                                    "Farm Activities" -> Icons.Default.Agriculture
                                    "Animal Interaction" -> Icons.Default.Pets
                                    "Local Cooking" -> Icons.Default.Restaurant
                                    "Nature Walk" -> Icons.Default.Forest
                                    "Fishing" -> Icons.Default.Phishing
                                    "Yoga & Meditation" -> Icons.Default.SelfImprovement
                                    "Plantation Walk" -> Icons.Default.Park
                                    else -> null
                                }
                                if (icon != null) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Terracotta,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                OutlinedButton(
                    onClick = { selectedActivities.clear() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Clear Filters",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F3EF))
            .padding(horizontal = 16.dp)
    ) {
        // Reduced top spacer as MainActivity NavHost applies system bar insets
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar with Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Subtler elevation
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search farm stays...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Terracotta,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
                )
            }

            IconButton(
                onClick = { showFilterSheet = true },
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White,
                    contentColor = Terracotta
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Adaptive spacing

        Text(
            text = "Explore Farm Stays",
            style = MaterialTheme.typography.titleLarge, // Slightly smaller header for better fit
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Experience the authentic rural lifestyle",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terracotta)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp), // Reduced gap for compactness
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(
                    items = filteredStays
                ) { index, stay ->
                    val originalIndex = farmStays.indexOf(stay)
                    FarmStayCard(stay) {
                        navController.navigate("detail_screen/$originalIndex")
                    }
                }
            }
        }
    }
}


@Composable
fun FarmStayCard(stay: FarmStay, onDetailsClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Image Section - Using aspectRatio for natural scaling across various screen widths
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.4f) 
                    .background(Color.LightGray)
            ) {
                if (stay.coverImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = stay.coverImageUrl,
                        contentDescription = "Cover Image for ${stay.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "No Image",
                        modifier = Modifier.size(40.dp).align(Alignment.Center),
                        tint = Color.Gray
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stay.name.ifEmpty { "NO NAME FOUND" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = stay.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB400),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stay.rating.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${stay.price} / night",
                        style = MaterialTheme.typography.titleSmall,
                        color = Terracotta,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Button(
                        onClick = onDetailsClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Details", 
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
