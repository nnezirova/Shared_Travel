package com.example.sharedtravel.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedtravel.R
import com.example.sharedtravel.viewmodel.AuthViewModel
import android.content.Intent
import com.example.sharedtravel.ui.home.HomeActivity
import androidx.lifecycle.lifecycleScope
import com.example.sharedtravel.viewmodel.AuthState
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = AuthViewModel()

        observeViewModel()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)


        val goToRegisterButton = findViewById<Button>(R.id.goToRegisterButton)

        goToRegisterButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            viewModel.loginUser(email, password)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        // Show progress bar if you have one
                    }
                    is AuthState.Success -> {
                        Toast.makeText(this@LoginActivity, "Успешен вход!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is AuthState.Error -> {
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                    }
                    else -> {}
                }
            }
        }
    }
}