package com.example.sharedtravel.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedtravel.R
import com.example.sharedtravel.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = AuthViewModel()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {

            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            viewModel.login(email, password) { success, message ->

                if (success) {
                    Toast.makeText(this, "Успешен вход!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, message ?: "Грешка", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}