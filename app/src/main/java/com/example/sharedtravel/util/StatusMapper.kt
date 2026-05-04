package com.example.sharedtravel.util

import com.example.sharedtravel.data.model.BookingStatus
import com.example.sharedtravel.data.model.TripStatus

object StatusMapper {
    fun mapBookingStatusKey(status: BookingStatus): StringKey {
        return when (status) {
            BookingStatus.PENDING -> StringKey.STATUS_PENDING
            BookingStatus.CONFIRMED -> StringKey.STATUS_CONFIRMED
            BookingStatus.REJECTED -> StringKey.STATUS_REJECTED
            BookingStatus.CANCELLED -> StringKey.STATUS_CANCELLED
        }
    }

    fun mapTripStatusKey(status: TripStatus): StringKey {
        return when (status) {
            TripStatus.SCHEDULED -> StringKey.STATUS_SCHEDULED
            TripStatus.COMPLETED -> StringKey.STATUS_COMPLETED
            TripStatus.IN_PROGRESS -> StringKey.STATUS_PENDING // Fallback or add IN_PROGRESS
            TripStatus.CANCELLED -> StringKey.STATUS_CANCELLED
        }
    }
}
