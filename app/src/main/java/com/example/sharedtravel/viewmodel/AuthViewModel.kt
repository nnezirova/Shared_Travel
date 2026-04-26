package com.example.sharedtravel.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sharedtravel.data.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    fun register(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        repository.register(email, password, callback)
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        repository.login(email, password, callback)
    }
}