package com.example.sharedtravel.ui.auth

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedtravel.R
import com.example.sharedtravel.viewmodel.AuthViewModel
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.example.sharedtravel.viewmodel.AuthState
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        viewModel = AuthViewModel()

        observeViewModel()

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val universityInput = findViewById<EditText>(R.id.universityInput)
        val registerButton = findViewById<Button>(R.id.registerButton)

        val goToLoginButton = findViewById<Button>(R.id.goToLoginButton)

        goToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        registerButton.setOnClickListener {

            val name = nameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val phone = phoneInput.text.toString()
            val university = universityInput.text.toString()

            viewModel.registerUser(email, password)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        // Show progress
                    }
                    is AuthState.Success -> {
                        Toast.makeText(this@RegisterActivity, "Успешна регистрация!", Toast.LENGTH_SHORT).show()
                        finish() // Or go to home
                    }
                    is AuthState.Error -> {
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                    }
                    else -> {}
                }
            }
        }
    }
}