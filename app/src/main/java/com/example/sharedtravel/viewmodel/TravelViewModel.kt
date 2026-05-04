package com.example.sharedtravel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedtravel.data.model.Booking
import com.example.sharedtravel.data.model.BookingStatus
import com.example.sharedtravel.data.model.Notification
import com.example.sharedtravel.data.model.Trip
import com.example.sharedtravel.data.model.User
import com.example.sharedtravel.data.repository.TravelRepository
import com.example.sharedtravel.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class TravelState {
    object Idle : TravelState()
    object Loading : TravelState()
    object Success : TravelState()
    data class Error(val message: String) : TravelState()
}

data class TripWithDriver(
    val trip: Trip,
    val driver: User?
)

class TravelViewModel : ViewModel() {
    private val repository = TravelRepository()
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _originQuery = MutableStateFlow("")
    val originQuery = _originQuery.asStateFlow()

    private val _destinationQuery = MutableStateFlow("")
    val destinationQuery = _destinationQuery.asStateFlow()

    // Real-time stream of all trips from repository
    private val allTrips: Flow<List<Trip>> = repository.getTripsFlow()

    // Filtered stream: Updates whenever the database OR the search queries change
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val filteredTrips: StateFlow<List<TripWithDriver>> = combine(
        allTrips,
        _originQuery,
        _destinationQuery
    ) { trips, origin, destination ->
        trips.filter { trip ->
            trip.startLocation.contains(origin, ignoreCase = true) &&
            trip.endLocation.contains(destination, ignoreCase = true)
        }
    }.flatMapLatest { trips ->
        flow {
            val joined = trips.map { trip ->
                val driver = userRepository.getUserProfile(trip.driverId)
                TripWithDriver(trip, driver)
            }
            emit(joined)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Real-time stream of bookings for current user as a Driver
    val driverBookings: StateFlow<List<Booking>> = auth.currentUser?.uid?.let { uid ->
        repository.getDriverBookingsFlow(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } ?: MutableStateFlow(emptyList())

    // Real-time stream of trips for current user as a Driver
    val driverTrips: StateFlow<List<Trip>> = auth.currentUser?.uid?.let { uid ->
        repository.getDriverTripsFlow(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } ?: MutableStateFlow(emptyList())

    // Real-time stream of bookings for current user as a Passenger
    val passengerBookings: StateFlow<List<Booking>> = auth.currentUser?.uid?.let { uid ->
        repository.getPassengerBookingsFlow(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } ?: MutableStateFlow(emptyList())

    // Real-time stream of notifications
    val notifications: StateFlow<List<Notification>> = auth.currentUser?.uid?.let { uid ->
        repository.getNotificationsFlow(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } ?: MutableStateFlow(emptyList())

    private val _uiState = MutableStateFlow<TravelState>(TravelState.Idle)
    val uiState: StateFlow<TravelState> = _uiState.asStateFlow()

    fun updateSearch(origin: String, destination: String) {
        _originQuery.value = origin
        _destinationQuery.value = destination
    }

    fun createTrip(
        startLocation: String,
        endLocation: String,
        date: String,
        time: String,
        price: String,
        seats: String
    ) {
        val driverId = auth.currentUser?.uid ?: return
        
        // Basic validation
        if (startLocation.isBlank() || endLocation.isBlank() || price.isBlank() || seats.isBlank() || date.isBlank() || time.isBlank()) {
            _uiState.value = TravelState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = TravelState.Loading
            try {
                repository.createTrip(
                    driverId = driverId,
                    startLocation = startLocation,
                    endLocation = endLocation,
                    date = date,
                    time = time,
                    departureTimestamp = System.currentTimeMillis(), // Fallback
                    pricePerSeat = price.toDoubleOrNull() ?: 0.0,
                    totalSeats = seats.toIntOrNull() ?: 0
                )
                _uiState.value = TravelState.Success
            } catch (e: Exception) {
                _uiState.value = TravelState.Error(e.localizedMessage ?: "Failed to create trip")
            }
        }
    }

    fun completeTrip(tripId: String) {
        viewModelScope.launch {
            _uiState.value = TravelState.Loading
            try {
                repository.completeTrip(tripId)
                _uiState.value = TravelState.Success
            } catch (e: Exception) {
                _uiState.value = TravelState.Error(e.localizedMessage ?: "Failed to complete trip")
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    /**
     * Get a real-time flow for a single trip status.
     */
    fun getTripFlow(tripId: String): Flow<Trip?> {
        return repository.getTripFlow(tripId)
    }

    /**
     * Books a seat for the current user.
     */
    fun bookSeat(trip: Trip, seatsToBook: Int = 1) {
        val passengerId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.value = TravelState.Loading
            try {
                repository.bookSeat(trip, passengerId, seatsToBook)
                _uiState.value = TravelState.Success
            } catch (e: Exception) {
                _uiState.value = TravelState.Error(e.localizedMessage ?: "Booking failed")
            }
        }
    }

    /**
     * Updates booking status (APPROVE/REJECT)
     */
    fun updateBookingStatus(booking: Booking, status: BookingStatus) {
        viewModelScope.launch {
            _uiState.value = TravelState.Loading
            try {
                repository.updateBookingStatus(booking.id, booking.tripId, status)
                _uiState.value = TravelState.Success
            } catch (e: Exception) {
                _uiState.value = TravelState.Error(e.localizedMessage ?: "Update failed")
            }
        }
    }

    /**
     * Submits a rating for the driver.
     */
    fun submitReview(booking: Booking, rating: Double) {
        viewModelScope.launch {
            _uiState.value = TravelState.Loading
            try {
                // Perform both updates
                userRepository.rateDriver(booking.driverId, rating)
                repository.markBookingAsRated(booking.id)
                _uiState.value = TravelState.Success
            } catch (e: Exception) {
                _uiState.value = TravelState.Error(e.localizedMessage ?: "Failed to submit rating")
            }
        }
    }

    fun resetState() {
        _uiState.value = TravelState.Idle
    }
}
