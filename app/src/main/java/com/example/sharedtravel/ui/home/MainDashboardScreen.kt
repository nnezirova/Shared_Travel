package com.example.sharedtravel.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sharedtravel.data.model.*
import com.example.sharedtravel.util.*
import com.example.sharedtravel.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val labelKey: StringKey, val icon: ImageVector) {
    object FindRide : Screen("find_ride", StringKey.NAV_FIND, Icons.Default.Search)
    object OfferRide : Screen("offer_ride", StringKey.NAV_OFFER, Icons.Default.Add)
    object Requests : Screen("requests", StringKey.NAV_REQUESTS, Icons.Default.Notifications)
    object Bookings : Screen("bookings", StringKey.NAV_BOOKINGS, Icons.AutoMirrored.Filled.List)
    object Profile : Screen("profile", StringKey.NAV_PROFILE, Icons.Default.Person)
}

@Composable
fun MainDashboardScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val currentLang = LocalLanguage.current
    val items = listOf(
        Screen.FindRide,
        Screen.OfferRide,
        Screen.Requests,
        Screen.Bookings,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(AppStrings.get(screen.labelKey, currentLang)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.FindRide.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.FindRide.route) { FindRideScreen() }
            composable(Screen.OfferRide.route) { OfferRideScreen() }
            composable(Screen.Requests.route) { RequestsScreen() }
            composable(Screen.Bookings.route) { BookingsScreen() }
            composable(Screen.Profile.route) { ProfileScreen(onLogout = onLogout) }
        }
    }
}


@Composable
fun OfferRideScreen(viewModel: TravelViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentLang = LocalLanguage.current

    var startLocation by remember { mutableStateOf("") }
    var endLocation by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (uiState) {
            is TravelState.Success -> {
                Toast.makeText(context, AppStrings.get(StringKey.RIDE_PUBLISHED_SUCCESS, currentLang), Toast.LENGTH_SHORT).show()
                startLocation = ""; endLocation = ""; date = ""; time = ""; price = ""; seats = ""
                viewModel.resetState()
            }
            is TravelState.Error -> {
                Toast.makeText(context, (uiState as TravelState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = AppStrings.get(StringKey.OFFER_RIDE_TITLE, currentLang),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = startLocation,
                    onValueChange = { startLocation = it },
                    label = { Text(AppStrings.get(StringKey.FROM_ORIGIN_LABEL, currentLang)) },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = endLocation,
                    onValueChange = { endLocation = it },
                    label = { Text(AppStrings.get(StringKey.TO_DESTINATION_LABEL, currentLang)) },
                    leadingIcon = { Icon(Icons.Default.Place, null) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text(AppStrings.get(StringKey.DATE_LABEL, currentLang)) },
                        placeholder = { Text("DD/MM") },
                        leadingIcon = { Icon(Icons.Default.DateRange, null) },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text(AppStrings.get(StringKey.TIME_LABEL, currentLang)) },
                        placeholder = { Text("HH:mm") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text(AppStrings.get(StringKey.PRICE_LABEL, currentLang)) },
                        leadingIcon = { Text("$", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = seats,
                        onValueChange = { seats = it },
                        label = { Text(AppStrings.get(StringKey.SEATS_LABEL, currentLang)) },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (startLocation.isBlank() || endLocation.isBlank() || price.isBlank() || seats.isBlank() || date.isBlank() || time.isBlank()) {
                            Toast.makeText(context, AppStrings.get(StringKey.FILL_ALL_FIELDS_ERROR, currentLang), Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.createTrip(startLocation, endLocation, date, time, price, seats)
                        }
                    },
                    enabled = uiState !is TravelState.Loading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState is TravelState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(AppStrings.get(StringKey.PUBLISH_RIDE_BUTTON, currentLang), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun FindRideScreen(viewModel: TravelViewModel = viewModel()) {
    val trips by viewModel.filteredTrips.collectAsState()
    val origin by viewModel.originQuery.collectAsState()
    val destination by viewModel.destinationQuery.collectAsState()
    val currentLang = LocalLanguage.current
    
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is TravelState.Success -> {
                if (selectedTrip != null) {
                    Toast.makeText(context, AppStrings.get(StringKey.BOOKING_SUCCESS, currentLang), Toast.LENGTH_SHORT).show()
                    selectedTrip = null
                    viewModel.resetState()
                }
            }
            is TravelState.Error -> {
                Toast.makeText(context, (uiState as TravelState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    if (showDialog && selectedTrip != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(AppStrings.get(StringKey.CONFIRM_BOOKING_TITLE, currentLang)) },
            text = { Text(AppStrings.get(StringKey.CONFIRM_BOOKING_TEXT, currentLang).format(selectedTrip!!.startLocation, selectedTrip!!.endLocation)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.bookSeat(selectedTrip!!)
                    showDialog = false
                }) {
                    Text(AppStrings.get(StringKey.CONFIRM_BUTTON, currentLang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(AppStrings.get(StringKey.CANCEL_BUTTON, currentLang))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = AppStrings.get(StringKey.FIND_RIDE_TITLE, currentLang),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- SEARCH HEADER ---
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = origin,
                    onValueChange = { viewModel.updateSearch(it, destination) },
                    label = { Text(AppStrings.get(StringKey.FROM_ORIGIN_LABEL, currentLang)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = destination,
                    onValueChange = { viewModel.updateSearch(origin, it) },
                    label = { Text(AppStrings.get(StringKey.TO_DESTINATION_LABEL, currentLang)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Place, null) },
                    singleLine = true
                )
            }
        }

        if (uiState is TravelState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (trips.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(AppStrings.get(StringKey.NO_RIDES_FOUND, currentLang), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(trips) { tripWithDriver ->
                    TripCard(tripWithDriver = tripWithDriver, onBookClick = {
                        selectedTrip = tripWithDriver.trip
                        showDialog = true
                    })
                }
            }
        }
    }
}

@Composable
fun TripCard(tripWithDriver: TripWithDriver, onBookClick: () -> Unit) {
    val trip = tripWithDriver.trip
    val driver = tripWithDriver.driver
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isOwnTrip = currentUserId == trip.driverId
    val currentLang = LocalLanguage.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Driver Info Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = driver?.firstName?.take(1) ?: "?",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    val driverName = if (driver != null) {
                        "${driver.firstName} ${driver.lastName.take(1)}."
                    } else {
                        "Unknown Driver"
                    }
                    Text(
                        text = driverName, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (driver != null) {
                        Text(
                            text = AppStrings.get(StringKey.DRIVING_SINCE_FORMAT, currentLang).format(driver.drivingSinceYear),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                if (driver != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star, 
                            contentDescription = null, 
                            tint = Color(0xFFFFB300), 
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = driver.averageRating.toString(), 
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Date & Time Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Text(text = trip.date, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Text(text = trip.time, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }

            // Trip Details Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = trip.startLocation, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = trip.endLocation, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${trip.pricePerSeat}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = AppStrings.get(StringKey.PER_SEAT_LABEL, currentLang),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.get(StringKey.SEATS_LEFT_FORMAT, currentLang).format(trip.availableSeats),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (!isOwnTrip) {
                    Button(
                        onClick = onBookClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(AppStrings.get(StringKey.BOOK_NOW_BUTTON, currentLang))
                    }
                } else {
                    AssistChip(
                        onClick = {},
                        label = { Text(AppStrings.get(StringKey.YOUR_TRIP_LABEL, currentLang)) },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestsScreen(viewModel: TravelViewModel = viewModel()) {
    val trips by viewModel.driverTrips.collectAsState()
    val bookings by viewModel.driverBookings.collectAsState()
    val currentLang = LocalLanguage.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(AppStrings.get(StringKey.DRIVER_DASHBOARD_TITLE, currentLang), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text(AppStrings.get(StringKey.ACTIVE_TRIPS_LABEL, currentLang), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        if (trips.isEmpty()) {
            Text(AppStrings.get(StringKey.NO_TRIPS_CREATED, currentLang), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(trips) { trip ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (trip.status == TripStatus.COMPLETED) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${trip.startLocation} -> ${trip.endLocation}", fontWeight = FontWeight.Bold)
                                Text(
                                    text = AppStrings.get(StringKey.STATUS_LABEL, currentLang).format(AppStrings.get(StatusMapper.mapTripStatusKey(trip.status), currentLang)), 
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (trip.status == TripStatus.SCHEDULED) {
                                Button(
                                    onClick = { viewModel.completeTrip(trip.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Text(AppStrings.get(StringKey.FINISH_TRIP_BUTTON, currentLang))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(AppStrings.get(StringKey.BOOKING_REQUESTS_LABEL, currentLang), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        if (bookings.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(AppStrings.get(StringKey.NO_PENDING_REQUESTS, currentLang))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(bookings) { booking ->
                    BookingRequestCard(
                        booking = booking,
                        onApprove = { viewModel.updateBookingStatus(booking, BookingStatus.CONFIRMED) },
                        onReject = { viewModel.updateBookingStatus(booking, BookingStatus.REJECTED) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingRequestCard(
    booking: Booking,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val currentLang = LocalLanguage.current
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(AppStrings.get(StringKey.PASSENGER_ID_LABEL, currentLang).format(booking.passengerId.takeLast(6)))
            Text(AppStrings.get(StringKey.SEATS_REQUESTED_LABEL, currentLang).format(booking.seatsBooked))
            Text(
                text = AppStrings.get(StringKey.STATUS_LABEL, currentLang).format(AppStrings.get(StatusMapper.mapBookingStatusKey(booking.status), currentLang)), 
                fontWeight = FontWeight.Bold
            )
            
            if (booking.status == BookingStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onReject, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text(AppStrings.get(StringKey.REJECT_BUTTON, currentLang))
                    }
                    Button(onClick = onApprove) {
                        Text(AppStrings.get(StringKey.APPROVE_BUTTON, currentLang))
                    }
                }
            }
        }
    }
}

@Composable
fun BookingsScreen(viewModel: TravelViewModel = viewModel()) {
    val bookings by viewModel.passengerBookings.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentLang = LocalLanguage.current

    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is TravelState.Success && showRatingDialog) {
            Toast.makeText(context, AppStrings.get(StringKey.RATING_SUCCESS, currentLang), Toast.LENGTH_SHORT).show()
            showRatingDialog = false
            selectedBooking = null
            viewModel.resetState()
        }
    }

    if (showRatingDialog && selectedBooking != null) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating: Double ->
                viewModel.submitReview(selectedBooking!!, rating)
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(AppStrings.get(StringKey.MY_BOOKINGS_TITLE, currentLang), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        // Notifications Banner
        if (notifications.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            notifications.forEach { note ->
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { viewModel.markNotificationAsRead(note.id) }
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(note.message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(AppStrings.get(StringKey.NO_BOOKINGS_YET, currentLang))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bookings) { booking ->
                    MyBookingCard(
                        booking = booking,
                        viewModel = viewModel,
                        onRateClick = {
                            selectedBooking = booking
                            showRatingDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RatingDialog(onDismiss: () -> Unit, onSubmit: (Double) -> Unit) {
    var rating by remember { mutableFloatStateOf(5f) }
    val currentLang = LocalLanguage.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.get(StringKey.RATING_DIALOG_TITLE, currentLang)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(AppStrings.get(StringKey.RATING_DIALOG_TEXT, currentLang))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "⭐ ${rating.toInt()}", style = MaterialTheme.typography.headlineMedium)
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating.toDouble()) }) {
                Text(AppStrings.get(StringKey.SUBMIT_BUTTON, currentLang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.get(StringKey.CANCEL_BUTTON, currentLang))
            }
        }
    )
}

@Composable
fun MyBookingCard(booking: Booking, viewModel: TravelViewModel, onRateClick: () -> Unit) {
    val tripFlow = remember(booking.tripId) { viewModel.getTripFlow(booking.tripId) }
    val trip by tripFlow.collectAsState(initial = null)
    val currentLang = LocalLanguage.current

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Trip: ${trip?.startLocation ?: "..." } -> ${trip?.endLocation ?: "..."}", fontWeight = FontWeight.Bold)
            Text(AppStrings.get(StringKey.SEATS_REQUESTED_LABEL, currentLang).format(booking.seatsBooked))
            
            val statusColor = when(booking.status) {
                BookingStatus.CONFIRMED -> Color(0xFF4CAF50)
                BookingStatus.REJECTED -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = AppStrings.get(StringKey.STATUS_LABEL, currentLang).format(AppStrings.get(StatusMapper.mapBookingStatusKey(booking.status), currentLang)), 
                    color = statusColor, 
                    fontWeight = FontWeight.Bold
                )
                if (trip != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "| Trip: ${AppStrings.get(StatusMapper.mapTripStatusKey(trip!!.status), currentLang)}", 
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            if (booking.status == BookingStatus.CONFIRMED && !booking.isRated && trip?.status == TripStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = onRateClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(AppStrings.get(StringKey.RATE_DRIVER_BUTTON, currentLang))
                }
            } else if (booking.isRated) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = AppStrings.get(StringKey.ALREADY_RATED_LABEL, currentLang),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val profileState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    val currentLang = LocalLanguage.current

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var drivingSince by remember { mutableStateOf("") }

    // Update local state when userProfile loads
    LaunchedEffect(userProfile) {
        userProfile?.let {
            firstName = it.firstName
            lastName = it.lastName
            drivingSince = it.drivingSinceYear
        }
    }

    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileState.Success -> {
                Toast.makeText(context, AppStrings.get(StringKey.PROFILE_SAVED_SUCCESS, currentLang), Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is ProfileState.Error -> {
                Toast.makeText(context, (profileState as ProfileState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Person, 
                    contentDescription = null, 
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (firstName.isNotEmpty()) "$firstName $lastName" else AppStrings.get(StringKey.MY_PROFILE_TITLE, currentLang),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (userProfile != null) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = userProfile!!.averageRating.toString(), 
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = AppStrings.get(StringKey.REVIEWS_COUNT_FORMAT, currentLang).format(userProfile!!.totalReviews), 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text(AppStrings.get(StringKey.FIRST_NAME_LABEL, currentLang)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text(AppStrings.get(StringKey.LAST_NAME_LABEL, currentLang)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = drivingSince,
            onValueChange = { drivingSince = it },
            label = { Text(AppStrings.get(StringKey.DRIVING_SINCE_YEAR_LABEL, currentLang)) },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            singleLine = true
        )

        Button(
            onClick = {
                if (firstName.isBlank() || lastName.isBlank()) {
                    Toast.makeText(context, AppStrings.get(StringKey.NAMES_BLANK_ERROR, currentLang), Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.saveProfile(firstName, lastName, drivingSince)
                }
            },
            enabled = profileState !is ProfileState.Loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            if (profileState is ProfileState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(AppStrings.get(StringKey.SAVE_PROFILE_BUTTON, currentLang), style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(AppStrings.get(StringKey.LOGOUT_BUTTON, currentLang), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}
