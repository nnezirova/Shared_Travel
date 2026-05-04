package com.example.sharedtravel.data.repository

import com.example.sharedtravel.data.model.Booking
import com.example.sharedtravel.data.model.BookingStatus
import com.example.sharedtravel.data.model.Notification
import com.example.sharedtravel.data.model.Trip
import com.example.sharedtravel.data.model.TripStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository handling business logic for Trips and Bookings using Firestore.
 */
class TravelRepository {

    private val db = FirebaseFirestore.getInstance()
    private val tripsCollection = db.collection("trips")
    private val bookingsCollection = db.collection("bookings")
    private val notificationsCollection = db.collection("notifications")

    /**
     * Real-time stream of all available trips from Firestore.
     * Modified to show only SCHEDULED or IN_PROGRESS trips.
     */
    fun getTripsFlow(): Flow<List<Trip>> = callbackFlow {
        val subscription = tripsCollection
            .whereIn("status", listOf(TripStatus.SCHEDULED.name, TripStatus.IN_PROGRESS.name))
            .whereGreaterThan("availableSeats", 0)
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
     * Real-time stream of a single trip's status.
     */
    fun getTripFlow(tripId: String): Flow<Trip?> = callbackFlow {
        val subscription = tripsCollection.document(tripId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(Trip::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Real-time stream of trips for a specific driver.
     */
    fun getDriverTripsFlow(driverId: String): Flow<List<Trip>> = callbackFlow {
        val subscription = tripsCollection
            .whereEqualTo("driverId", driverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Trip::class.java))
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
        date: String,
        time: String,
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
            date = date,
            time = time,
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
     * Completes a trip and notifies confirmed passengers.
     */
    suspend fun completeTrip(tripId: String) = withContext(Dispatchers.IO) {
        // 1. Update trip status
        tripsCollection.document(tripId).update("status", TripStatus.COMPLETED.name).await()

        // 2. Fetch confirmed bookings to notify passengers
        val confirmedBookings = bookingsCollection
            .whereEqualTo("tripId", tripId)
            .whereEqualTo("status", BookingStatus.CONFIRMED.name)
            .get()
            .await()
            .toObjects(Booking::class.java)

        // 3. Create notifications
        confirmedBookings.forEach { booking ->
            val notificationId = notificationsCollection.document().id
            val notification = Notification(
                id = notificationId,
                tripId = tripId,
                passengerId = booking.passengerId,
                message = "Your trip has been completed. Please rate your driver.",
                isRead = false
            )
            notificationsCollection.document(notificationId).set(notification).await()
        }
    }

    /**
     * Real-time stream of notifications for a specific passenger.
     */
    fun getNotificationsFlow(passengerId: String): Flow<List<Notification>> = callbackFlow {
        val subscription = notificationsCollection
            .whereEqualTo("passengerId", passengerId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Notification::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun markNotificationAsRead(notificationId: String): Unit = withContext(Dispatchers.IO) {
        notificationsCollection.document(notificationId).update("isRead", true).await()
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
     * Cancels a trip and notifies all booked passengers.
     */
    suspend fun cancelTrip(tripId: String) = withContext(Dispatchers.IO) {
        // 1. Update trip status
        tripsCollection.document(tripId).update("status", TripStatus.CANCELLED.name).await()

        // 2. Fetch all bookings for this trip to notify passengers
        val bookings = bookingsCollection
            .whereEqualTo("tripId", tripId)
            .get()
            .await()
            .toObjects(Booking::class.java)

        // 3. Update bookings to CANCELLED and notify
        bookings.forEach { booking ->
            bookingsCollection.document(booking.id).update("status", BookingStatus.CANCELLED.name).await()
            
            sendNotification(
                passengerId = booking.passengerId,
                tripId = tripId,
                message = "The driver has cancelled the trip from ${booking.tripId}. Sorry for the inconvenience."
            )
        }
    }

    /**
     * Logic for sending an in-app notification (stored in Firestore).
     */
    suspend fun sendNotification(passengerId: String, tripId: String, message: String) = withContext(Dispatchers.IO) {
        val notificationId = notificationsCollection.document().id
        val notification = Notification(
            id = notificationId,
            tripId = tripId,
            passengerId = passengerId,
            message = message,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        notificationsCollection.document(notificationId).set(notification).await()
    }

    /**
     * Automatically completes trips that have passed their departure time.
     */
    suspend fun autoCompleteExpiredTrips() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val expiredTrips = tripsCollection
            .whereEqualTo("status", TripStatus.SCHEDULED.name)
            .whereLessThan("departureTimestamp", now)
            .get()
            .await()
            .toObjects(Trip::class.java)

        expiredTrips.forEach { trip ->
            completeTrip(trip.id)
        }
    }

    /**
     * Updates the status of a booking (CONFIRMED, REJECTED, CANCELLED).
     * If rejected or cancelled, return the seats to the trip's available count.
     */
    suspend fun updateBookingStatus(
        bookingId: String,
        tripId: String,
        status: BookingStatus
    ) = withContext(Dispatchers.IO) {
        val bookingRef = bookingsCollection.document(bookingId)
        val tripRef = tripsCollection.document(tripId)

        db.runTransaction { transaction ->
            // 1. READ: Fetch the Booking document
            val bookingSnapshot = transaction.get(bookingRef)
            if (!bookingSnapshot.exists()) return@runTransaction
            val seatsBooked = bookingSnapshot.getLong("seatsBooked") ?: 0L

            // 2. READ: Fetch the Trip document (Must be before any WRITE)
            val tripSnapshot = transaction.get(tripRef)

            // 3. WRITE: Update booking status
            transaction.update(bookingRef, "status", status.name)

            // 4. WRITE: If REJECTED or CANCELLED, return seats to the trip available count
            if (status == BookingStatus.REJECTED || status == BookingStatus.CANCELLED) {
                if (tripSnapshot.exists()) {
                    val currentAvailable = tripSnapshot.getLong("availableSeats") ?: 0L
                    transaction.update(tripRef, "availableSeats", currentAvailable + seatsBooked)
                }
            }
        }.await()
    }

    /**
     * Submits a rating for the driver and marks the booking as rated atomically.
     */
    suspend fun submitReview(
        bookingId: String,
        driverId: String,
        rating: Double
    ) = withContext(Dispatchers.IO) {
        val bookingRef = bookingsCollection.document(bookingId)
        val userRef = db.collection("users").document(driverId)

        db.runTransaction { transaction ->
            // 1. Update Driver Profile Rating
            val userSnapshot = transaction.get(userRef)
            if (userSnapshot.exists()) {
                val oldAverageRating = userSnapshot.getDouble("averageRating") ?: 5.0
                val oldTotalReviews = userSnapshot.getLong("totalReviews") ?: 0L
                
                val newTotalReviews = oldTotalReviews + 1
                val newAverageRating = ((oldAverageRating * oldTotalReviews) + rating) / newTotalReviews
                
                transaction.update(userRef, "averageRating", newAverageRating)
                transaction.update(userRef, "totalReviews", newTotalReviews)
            }

            // 2. Mark the specific booking as rated so they can't rate again
            transaction.update(bookingRef, "isRated", true)
        }.await()
    }
    
}

