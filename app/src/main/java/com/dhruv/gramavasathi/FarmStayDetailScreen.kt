package com.dhruv.gramavasathi

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dhruv.gramavasathi.ui.theme.Terracotta
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class BookingStatus { IDLE, CHECKING, AVAILABLE, CONFIRMED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmStayDetailScreen(farmStay: FarmStay) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showBookingSheet by remember { mutableStateOf(false) }
    var showSuccessSheet by remember { mutableStateOf(false) }
    
    // Gallery State
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }

    // Set skipPartiallyExpanded = true to open the sheet fully automatically
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val successSheetState = rememberModalBottomSheetState()

    // Booking State
    var adults by remember { mutableIntStateOf(1) }
    var children by remember { mutableIntStateOf(0) }
    var infants by remember { mutableIntStateOf(0) }
    
    var checkInDate by remember { mutableStateOf<Long?>(null) }
    var checkOutDate by remember { mutableStateOf<Long?>(null) }
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }
    var bookingStatus by remember { mutableStateOf(BookingStatus.IDLE) }
    val isProcessing = bookingStatus != BookingStatus.IDLE

    val totalNights = remember(checkInDate, checkOutDate) {
        if (checkInDate != null && checkOutDate != null) {
            val diff = checkOutDate!! - checkInDate!!
            (diff / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
        } else 0
    }

    // Today in UTC for min date selection
    val todayUtc = remember {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    if (showCheckInPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = checkInDate ?: todayUtc,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= todayUtc
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showCheckInPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    checkInDate = datePickerState.selectedDateMillis
                    showCheckInPicker = false
                    // Reset check-out if it's now before or on check-in
                    if (checkOutDate != null && checkInDate != null && checkOutDate!! <= checkInDate!!) {
                        checkOutDate = null
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCheckInPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCheckOutPicker) {
        val minCheckOut = checkInDate ?: todayUtc
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = checkOutDate ?: (minCheckOut + 24 * 60 * 60 * 1000),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis > minCheckOut
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showCheckOutPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    checkOutDate = datePickerState.selectedDateMillis
                    showCheckOutPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCheckOutPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            // Zeroing out contentWindowInsets to prevent duplicate padding
            // since MainActivity's NavHost already applies system bar insets.
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            if (showBookingSheet) {
                ModalBottomSheet(
                    onDismissRequest = { if (!isProcessing) showBookingSheet = false },
                    sheetState = sheetState,
                    containerColor = Color(0xFFF7F3EF)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Reserve Your Stay",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        // Date Fields row optimized for smaller screens
                        Row(
                            modifier = Modifier.fillMaxWidth(), 
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = checkInDate?.formatDate() ?: "",
                                    onValueChange = {},
                                    label = { 
                                        Text(
                                            "Check-in", 
                                            maxLines = 1, 
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall,
                                            softWrap = false
                                        ) 
                                    },
                                    placeholder = { 
                                        Text(
                                            "Select Date", 
                                            maxLines = 1, 
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall,
                                            softWrap = false
                                        ) 
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Terracotta,
                                        focusedLabelColor = Terracotta
                                    ),
                                    trailingIcon = { 
                                        Icon(
                                            Icons.Default.DateRange, 
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        ) 
                                    },
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                )
                                Box(modifier = Modifier.matchParentSize().clickable(enabled = !isProcessing) { showCheckInPicker = true })
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = checkOutDate?.formatDate() ?: "",
                                    onValueChange = {},
                                    label = { 
                                        Text(
                                            "Check-out", 
                                            maxLines = 1, 
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall,
                                            softWrap = false
                                        ) 
                                    },
                                    placeholder = { 
                                        Text(
                                            "Select Date", 
                                            maxLines = 1, 
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall,
                                            softWrap = false
                                        ) 
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Terracotta,
                                        focusedLabelColor = Terracotta
                                    ),
                                    trailingIcon = { 
                                        Icon(
                                            Icons.Default.DateRange, 
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        ) 
                                    },
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                )
                                Box(modifier = Modifier.matchParentSize().clickable(enabled = !isProcessing) { showCheckOutPicker = true })
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Guest Selector
                        Text(text = "Guests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        GuestCounter("Adults", adults, onDecrement = { if (!isProcessing && adults > 1) adults-- }, onIncrement = { if (!isProcessing) adults++ })
                        GuestCounter("Children", children, onDecrement = { if (!isProcessing && children > 0) children-- }, onIncrement = { if (!isProcessing) children++ })
                        GuestCounter("Infants", infants, onDecrement = { if (!isProcessing && infants > 0) infants-- }, onIncrement = { if (!isProcessing) infants++ })

                        // Dynamic Guest Summary Logic - Always Descriptive
                        val guestSummary = remember(adults, children, infants) {
                            val list = mutableListOf<String>()
                            list.add("$adults Adult${if (adults > 1) "s" else ""}")
                            if (children > 0) list.add("$children Child${if (children > 1) "ren" else ""}")
                            if (infants > 0) list.add("$infants Infant${if (infants > 1) "s" else ""}")
                            list.joinToString(" • ")
                        }
                        
                        Text(
                            text = guestSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Pricing Section
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            // Improved price breakdown format
                            Text(text = "₹${farmStay.price}/night • $totalNights nights", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "₹${farmStay.price * totalNights}", style = MaterialTheme.typography.bodyLarge)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = "₹${farmStay.price * totalNights}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Terracotta)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        val isButtonEnabled = checkInDate != null && checkOutDate != null && totalNights > 0 && !isProcessing

                        Button(
                            onClick = {
                                scope.launch {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    bookingStatus = BookingStatus.CHECKING
                                    delay(2000)
                                    
                                    // Transition to Success Sheet directly after checking
                                    showBookingSheet = false
                                    showSuccessSheet = true
                                    bookingStatus = BookingStatus.IDLE
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = isButtonEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Terracotta,
                                disabledContainerColor = if (isProcessing) Terracotta else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            AnimatedContent(
                                targetState = bookingStatus,
                                transitionSpec = {
                                    (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                                },
                                label = "BookingStatus"
                            ) { status ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    when (status) {
                                        BookingStatus.CHECKING -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Checking availability...", color = Color.White)
                                        }
                                        else -> {
                                            Text("Confirm Dates & Guests", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showSuccessSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSuccessSheet = false },
                    sheetState = successSheetState,
                    containerColor = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Booking Confirmed",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        Text(
                            text = "Your farm stay has been reserved successfully.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Summary Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SuccessSummaryRow("Dates", "${checkInDate?.formatDate()} - ${checkOutDate?.formatDate()}")
                                val fullGuestText = remember(adults, children, infants) {
                                    val total = adults + children + infants
                                    val details = mutableListOf<String>()
                                    details.add("$adults Adult${if (adults > 1) "s" else ""}")
                                    if (children > 0) details.add("$children Child${if (children > 1) "ren" else ""}")
                                    if (infants > 0) details.add("$infants Infant${if (infants > 1) "s" else ""}")
                                    "$total Guests (${details.joinToString(", ")})"
                                }
                                SuccessSummaryRow("Guests", fullGuestText)
                                SuccessSummaryRow("Total Paid", "₹${farmStay.price * totalNights}")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = { showSuccessSheet = false },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF7F3EF))
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Image with Back Button Overlay to reduce top whitespace
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    AsyncImage(
                        model = farmStay.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    IconButton(
                        onClick = { (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed() },
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .background(Color.White.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = farmStay.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Text(text = farmStay.location, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        @Suppress("DEPRECATION")
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(16.dp))
                        Text(text = "${farmStay.rating}", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "₹${farmStay.price} / night",
                        style = MaterialTheme.typography.titleLarge,
                        color = Terracotta,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Property Description",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = farmStay.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "About the Host",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Text(
                                text = farmStay.hostName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = farmStay.hostDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(text = "Gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp)
                    ) {
                        itemsIndexed(farmStay.galleryImages) { index, imageUrl ->
                            Card(
                                modifier = Modifier
                                    .fillParentMaxWidth(0.8f)
                                    .height(180.dp)
                                    .clickable { selectedImageIndex = index },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Gallery Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "Activities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        farmStay.activities.forEach { activity ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon = when (activity) {
                                    "Farm Activities" -> Icons.Default.Agriculture
                                    "Plantation Walk" -> Icons.Default.Park
                                    "Nature Walk" -> Icons.Default.Forest
                                    "Animal Interaction" -> Icons.Default.Pets
                                    "Local Cooking" -> Icons.Default.Restaurant
                                    "Yoga & Meditation" -> Icons.Default.SelfImprovement
                                    "Organic Farming" -> Icons.Default.Eco
                                    "Fishing" -> Icons.Default.Phishing
                                    "Garden Walk" -> Icons.Default.Yard
                                    "Heritage Living" -> Icons.Default.Gite
                                    "Birdwatching" -> Icons.Default.Visibility
                                    "Village Walk" -> Icons.AutoMirrored.Filled.DirectionsWalk
                                    "Rural Craft" -> Icons.Default.Brush
                                    "Orchard Walk" -> Icons.Default.Nature
                                    else -> Icons.Default.CheckCircle
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Terracotta,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activity,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp)) // Reduced from 32dp

                    Button(
                        onClick = {
                            showBookingSheet = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reserve Stay", color = Color.White, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }

        // Fullscreen Gallery Overlay
        AnimatedVisibility(
            visible = selectedImageIndex != null,
            enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center)
        ) {
            selectedImageIndex?.let { index ->
                FullscreenGallery(
                    images = farmStay.galleryImages,
                    initialPage = index,
                    onClose = { selectedImageIndex = null }
                )
            }
        }
    }
}

@Composable
fun SuccessSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(
            text = value, 
            style = MaterialTheme.typography.bodySmall, 
            fontWeight = FontWeight.SemiBold, 
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
    }
}

@Composable
fun FullscreenGallery(
    images: List<String>,
    initialPage: Int,
    onClose: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { images.size })
    
    // Handle back press to close gallery
    BackHandler {
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 16.dp,
            userScrollEnabled = true
        ) { page ->
            AsyncImage(
                model = images[page],
                contentDescription = "Gallery Image $page",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Close Button - removed statusBarsPadding() to avoid duplicate spacing
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Gallery",
                tint = Color.White
            )
        }

        // Page Indicator - removed statusBarsPadding()
        Text(
            text = "${pagerState.currentPage + 1} / ${images.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.TopCenter)
        )
    }
}

@Composable
fun GuestCounter(
    label: String,
    count: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove, 
                    contentDescription = "Decrement", 
                    tint = if ((label == "Adults" && count > 1) || (label != "Adults" && count > 0)) Terracotta else Color.Gray
                )
            }
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increment", tint = Terracotta)
            }
        }
    }
}

private fun Long.formatDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(this))
}
