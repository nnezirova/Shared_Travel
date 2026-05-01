package com.example.sharedtravel.data.repository

import com.example.sharedtravel.data.model.Booking
import com.example.sharedtravel.data.model.BookingStatus
import com.example.sharedtravel.data.model.Trip
import com.example.sharedtravel.data.model.TripStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository handling business logic for Trips and Bookings.
 */
class TravelRepository {

    /**
     * Creates a new Trip and returns the Trip object.
     * In a real app, this would also save the trip to Firestore/Database.
     */
    suspend fun createTrip(
        driverId: String,
        startLocation: String,
        endLocation: String,
        departureTimestamp: Long,
        pricePerSeat: Double,
        totalSeats: Int
    ): Trip = withContext(Dispatchers.IO) {
        // Generate a unique ID for the trip
        val tripId = UUID.randomUUID().toString()

        val newTrip = Trip(
            id = tripId,
            driverId = driverId,
            startLocation = startLocation,
            endLocation = endLocation,
            departureTimestamp = departureTimestamp,
            pricePerSeat = pricePerSeat,
            totalSeats = totalSeats,
            availableSeats = totalSeats,
            status = TripStatus.SCHEDULED
        )

        // TODO: Save newTrip to database
        newTrip
    }

    /**
     * Logic for a user booking a seat on a trip.
     * Returns a new [Booking] object if successful, or throws an exception if seats are unavailable.
     */
    suspend fun bookSeat(
        trip: Trip,
        passengerId: String,
        seatsToBook: Int
    ): Booking = withContext(Dispatchers.IO) {
        
        // Validation: Check if there are enough available seats
        if (trip.availableSeats < seatsToBook) {
            throw Exception("Not enough seats available. Requested: $seatsToBook, Available: ${trip.availableSeats}")
        }

        // Generate booking ID
        val bookingId = UUID.randomUUID().toString()

        // Create the booking object
        val newBooking = Booking(
            id = bookingId,
            tripId = trip.id,
            passengerId = passengerId,
            seatsBooked = seatsToBook,
            timestamp = System.currentTimeMillis(),
            status = BookingStatus.PENDING
        )

        // Update the trip's available seats
        // Note: In a production app, this update should be part of a database transaction
        trip.availableSeats -= seatsToBook

        // TODO: Update Trip in database and save the new Booking
        
        newBooking
    }
}
