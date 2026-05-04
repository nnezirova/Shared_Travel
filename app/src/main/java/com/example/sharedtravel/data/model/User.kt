package com.example.sharedtravel.data.model

import java.util.Locale

data class User (
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val university: String = "",
    val drivingSinceYear: String = "",
    val averageRating: Double = 5.0,
    val totalReviews: Int = 0

)
