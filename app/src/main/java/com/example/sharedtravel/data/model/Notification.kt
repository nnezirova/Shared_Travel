package com.example.sharedtravel.data.model

data class Notification(
    val id: String = "",
    val tripId: String = "",
    val passengerId: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
