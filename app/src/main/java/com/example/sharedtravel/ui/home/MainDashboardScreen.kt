package com.example.sharedtravel.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sharedtravel.data.model.Booking
import com.example.sharedtravel.data.model.BookingStatus
import com.example.sharedtravel.data.model.Trip
import com.example.sharedtravel.viewmodel.TravelState
import com.example.sharedtravel.viewmodel.TravelViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object FindRide : Screen("find_ride", "Find", Icons.Default.Search)
    object OfferRide : Screen("offer_ride", "Offer", Icons.Default.Add)
    object Requests : Screen("requests", "Requests", Icons.Default.Notifications)
    object Bookings : Screen("bookings", "Bookings", Icons.Default.List)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@Composable
fun MainDashboardScreen() {
    val navController = rememberNavController()
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
                        label = { Text(screen.label) },
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
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}


@Composable
fun OfferRideScreen(viewModel: TravelViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var startLocation by remember { mutableStateOf("") }
    var endLocation by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (uiState) {
            is TravelState.Success -> {
                Toast.makeText(context, "Ride Published Successfully!", Toast.LENGTH_SHORT).show()
                startLocation = ""
                endLocation = ""
                price = ""
                seats = ""
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
        Text("Offer a New Ride", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = startLocation,
            onValueChange = { startLocation = it },
            label = { Text("Start Location") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = endLocation,
            onValueChange = { endLocation = it },
            label = { Text("End Location") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price per Seat") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            )
            OutlinedTextField(
                value = seats,
                onValueChange = { seats = it },
                label = { Text("Available Seats") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.createTrip(startLocation, endLocation, price, seats) },
            enabled = uiState !is TravelState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is TravelState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Publish Ride")
            }
        }
    }
}

@Composable
fun FindRideScreen(viewModel: TravelViewModel = viewModel()) {
    val trips by viewModel.filteredTrips.collectAsState()
    val origin by viewModel.originQuery.collectAsState()
    val destination by viewModel.destinationQuery.collectAsState()
    
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is TravelState.Success -> {
                if (selectedTrip != null) {
                    Toast.makeText(context, "Seat booked successfully!", Toast.LENGTH_SHORT).show()
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
            title = { Text("Confirm Booking") },
            text = { Text("Are you sure you want to book a seat for the ride from ${selectedTrip!!.startLocation} to ${selectedTrip!!.endLocation}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.bookSeat(selectedTrip!!)
                    showDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Find a Ride",
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
                    label = { Text("From (Origin)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = destination,
                    onValueChange = { viewModel.updateSearch(origin, it) },
                    label = { Text("To (Destination)") },
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
                Text("No rides found for this route.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(trips) { trip ->
                    TripCard(trip = trip, onBookClick = {
                        selectedTrip = trip
                        showDialog = true
                    })
                }
            }
        }
    }
}

@Composable
fun TripCard(trip: Trip, onBookClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${trip.startLocation} → ${trip.endLocation}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Price: $${trip.pricePerSeat}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${trip.availableSeats} seats left",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Book Seat")
            }
        }
    }
}

@Composable
fun RequestsScreen(viewModel: TravelViewModel = viewModel()) {
    val bookings by viewModel.driverBookings.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Booking Requests", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No pending requests")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Passenger ID: ${booking.passengerId.takeLast(6)}")
            Text("Seats requested: ${booking.seatsBooked}")
            Text("Status: ${booking.status}", fontWeight = FontWeight.Bold)
            
            if (booking.status == BookingStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onReject, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Reject")
                    }
                    Button(onClick = onApprove) {
                        Text("Approve")
                    }
                }
            }
        }
    }
}

@Composable
fun BookingsScreen(viewModel: TravelViewModel = viewModel()) {
    val bookings by viewModel.passengerBookings.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Bookings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't booked any rides yet")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bookings) { booking ->
                    MyBookingCard(booking = booking)
                }
            }
        }
    }
}

@Composable
fun MyBookingCard(booking: Booking) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Trip ID: ${booking.tripId.takeLast(6)}")
            Text("Seats booked: ${booking.seatsBooked}")
            val statusColor = when(booking.status) {
                BookingStatus.CONFIRMED -> Color(0xFF4CAF50)
                BookingStatus.REJECTED -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }
            Text(
                text = "Status: ${booking.status}",
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Profile Screen", style = MaterialTheme.typography.headlineMedium)
    }
}
