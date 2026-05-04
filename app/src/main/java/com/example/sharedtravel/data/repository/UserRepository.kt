package com.example.sharedtravel.data.repository

import com.example.sharedtravel.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    /**
     * Saves or updates the user profile in Firestore.
     */
    suspend fun saveUserProfile(user: User) = withContext(Dispatchers.IO) {
        usersCollection.document(user.uid).set(user).await()
    }

    /**
     * Fetches a single user profile from Firestore once.
     */
    suspend fun getUserProfile(uid: String): User? = withContext(Dispatchers.IO) {
        try {
            usersCollection.document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Updates the driver's rating using a transaction.
     */
    suspend fun rateDriver(driverId: String, newRating: Double) = withContext(Dispatchers.IO) {
        val userRef = usersCollection.document(driverId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val oldAverageRating = snapshot.getDouble("averageRating") ?: 5.0
            val oldTotalReviews = snapshot.getLong("totalReviews") ?: 0L
            
            val newTotalReviews = oldTotalReviews + 1
            val newAverageRating = ((oldAverageRating * oldTotalReviews) + newRating) / newTotalReviews
            
            transaction.update(userRef, "averageRating", newAverageRating)
            transaction.update(userRef, "totalReviews", newTotalReviews)
        }.await()
    }

    /**
     * Streams the user profile for a specific UID in real-time.
     */
    fun getUserProfileFlow(uid: String): Flow<User?> = callbackFlow {
        val subscription = usersCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    trySend(user)
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }
}
