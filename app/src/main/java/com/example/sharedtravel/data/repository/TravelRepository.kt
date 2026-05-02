package com.example.sharedtravel.data.repository

import com.example.sharedtravel.data.model.Booking
import com.example.sharedtravel.data.model.BookingStatus
import com.example.sharedtravel.data.model.Trip
import com.example.sharedtravel.data.model.TripStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository handling business logic for Trips and Bookings using Firestore.
 */
class TravelRepository {

    private val db = FirebaseFirestore.getInstance()
    private val tripsCollection = db.collection("trips")

    /**
     * Real-time stream of all available trips from Firestore.
     */
    fun getTripsFlow(): Flow<List<Trip>> = callbackFlow {
        val subscription = tripsCollection
            .whereEqualTo("status", TripStatus.SCHEDULED.name)
            .whereGreaterThan("availableSeats", 0) // Only show trips with seats
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val trips = snapshot.toObjects(Trip::class.java)
                    trySend(trips)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Creates a new Trip and saves it to Firestore.
     */
    suspend fun createTrip(
        driverId: String,
        startLocation: String,
        endLocation: String,
        departureTimestamp: Long,
        pricePerSeat: Double,
        totalSeats: Int
    ): Trip = withContext(Dispatchers.IO) {
        
        val tripId = tripsCollection.document().id // Generate Firestore ID

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

        tripsCollection.document(tripId).set(newTrip).await()
        newTrip
    }

    /**
     * Logic for a user booking a seat on a trip.
     * Returns a new [Booking] object if successful.
     */
    suspend fun bookSeat(
        trip: Trip,
        passengerId: String,
        seatsToBook: Int
    ): Booking = withContext(Dispatchers.IO) {
        
        if (trip.availableSeats < seatsToBook) {
            throw Exception("Not enough seats available.")
        }

        val bookingId = db.collection("bookings").document().id

        val newBooking = Booking(
            id = bookingId,
            tripId = trip.id,
            passengerId = passengerId,
            driverId = trip.driverId, // Added driverId here
            seatsBooked = seatsToBook,
            timestamp = System.currentTimeMillis(),
            status = BookingStatus.PENDING
        )

        // Perform atomical update (simplified here, should ideally use transactions)
        val tripRef = tripsCollection.document(trip.id)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(tripRef)
            val currentAvailable = snapshot.getLong("availableSeats") ?: 0L
            if (currentAvailable < seatsToBook) {
                throw Exception("Not enough seats available")
            }
            transaction.update(tripRef, "availableSeats", currentAvailable - seatsToBook)
            transaction.set(db.collection("bookings").document(bookingId), newBooking)
        }.await()
        
        newBooking
    }

    /**
     * Real-time stream of bookings for a specific driver.
     */
    fun getDriverBookingsFlow(driverId: String): Flow<List<Booking>> = callbackFlow {
        val subscription = db.collection("bookings")
            .whereEqualTo("driverId", driverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.toObjects(Booking::class.java)
                    trySend(bookings)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Real-time stream of bookings for a specific passenger.
     */
    fun getPassengerBookingsFlow(passengerId: String): Flow<List<Booking>> = callbackFlow {
        val subscription = db.collection("bookings")
            .whereEqualTo("passengerId", passengerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.toObjects(Booking::class.java)
                    trySend(bookings)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Updates the status of a booking.
     * If rejected, increments the available seats back.
     */
    suspend fun updateBookingStatus(
        bookingId: String,
        tripId: String,
        status: BookingStatus
    ) = withContext(Dispatchers.IO) {
        val bookingRef = db.collection("bookings").document(bookingId)
        val tripRef = db.collection("trips").document(tripId)

        db.runTransaction { transaction ->
            val bookingSnapshot = transaction.get(bookingRef)
            val seatsBooked = bookingSnapshot.getLong("seatsBooked") ?: 0L

            // Update booking status
            transaction.update(bookingRef, "status", status)

            // If REJECTED or CANCELLED, return seats to the trip
            if (status == BookingStatus.REJECTED || status == BookingStatus.CANCELLED) {
                val tripSnapshot = transaction.get(tripRef)
                val currentAvailable = tripSnapshot.getLong("availableSeats") ?: 0L
                transaction.update(tripRef, "availableSeats", currentAvailable + seatsBooked)
            }
        }.await()
    }
    
}

