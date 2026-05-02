package com.example.sharedtravel.data.model

/**
 * Represents the status of a passenger's seat reservation.
 */
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    REJECTED
}

/**
 * Data class representing a booking made by a passenger for a specific trip.
 */
data class Booking(
    val id: String = "",
    val tripId: String = "",
    val passengerId: String = "",
    val driverId: String = "",
    val seatsBooked: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: BookingStatus = BookingStatus.PENDING
)
