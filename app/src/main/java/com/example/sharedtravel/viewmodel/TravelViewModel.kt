package com.example.sharedtravel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedtravel.data.model.Booking
import com.example.sharedtravel.data.model.BookingStatus
import com.example.sharedtravel.data.model.Trip
import com.example.sharedtravel.data.repository.TravelRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class TravelState {
    object Idle : TravelState()
    object Loading : TravelState()
    object Success : TravelState()
    data class Error(val message: String) : TravelState()
}

class TravelViewModel : ViewModel() {
    private val repository = TravelRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _originQuery = MutableStateFlow("")
    val originQuery = _originQuery.asStateFlow()

    private val _destinationQuery = MutableStateFlow("")
    val destinationQuery = _destinationQuery.asStateFlow()

    // Real-time stream of all trips from repository
    private val allTrips: Flow<List<Trip>> = repository.getTripsFlow()

    // Filtered stream: Updates whenever the database OR the search queries change
    val filteredTrips: StateFlow<List<Trip>> = combine(
        allTrips,
        _originQuery,
        _destinationQuery
    ) { trips, origin, destination ->
        trips.filter { trip ->
            trip.startLocation.contains(origin, ignoreCase = true) &&
            trip.endLocation.contains(destination, ignoreCase = true)
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

    // Real-time stream of bookings for current user as a Passenger
    val passengerBookings: StateFlow<List<Booking>> = auth.currentUser?.uid?.let { uid ->
        repository.getPassengerBookingsFlow(uid)
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
        price: String,
        seats: String
    ) {
        val driverId = auth.currentUser?.uid ?: return
        
        // Basic validation
        if (startLocation.isBlank() || endLocation.isBlank() || price.isBlank() || seats.isBlank()) {
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
                    departureTimestamp = System.currentTimeMillis(), // For now
                    pricePerSeat = price.toDoubleOrNull() ?: 0.0,
                    totalSeats = seats.toIntOrNull() ?: 0
                )
                _uiState.value = TravelState.Success
            } catch (e: Exception) {
                _uiState.value = TravelState.Error(e.localizedMessage ?: "Failed to create trip")
            }
        }
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

    fun resetState() {
        _uiState.value = TravelState.Idle
    }
}
