package com.example.sharedtravel.data.model

/**
 * Represents the current state of a scheduled trip.
 */
enum class TripStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

/**
 * Data class representing a scheduled ride/trip in the carpooling system.
 */
data class Trip(
    val id: String = "",
    val driverId: String = "",
    val startLocation: String = "",
    val endLocation: String = "",
    val date: String = "",
    val time: String = "",
    val departureTimestamp: Long = 0L,
    val pricePerSeat: Double = 0.0,
    val totalSeats: Int = 0,
    var availableSeats: Int = 0,
    val status: TripStatus = TripStatus.SCHEDULED
)
