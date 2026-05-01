package com.example.sharedtravel.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Modern repository for Firebase Authentication using Coroutines.
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    /**
     * Registers a user with email and password.
     * Returns the [FirebaseUser] if successful.
     */
    suspend fun register(email: String, password: String): FirebaseUser? {
        return auth.createUserWithEmailAndPassword(email, password).await().user
    }

    /**
     * Logs in a user with email and password.
     * Returns the [FirebaseUser] if successful.
     */
    suspend fun login(email: String, password: String): FirebaseUser? {
        return auth.signInWithEmailAndPassword(email, password).await().user
    }
    
    /**
     * Get the current user if already logged in.
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}
