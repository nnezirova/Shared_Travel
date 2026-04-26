package com.example.sharedtravel.ui.auth

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedtravel.R
import com.example.sharedtravel.viewmodel.AuthViewModel
import android.content.Intent

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        viewModel = AuthViewModel()

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

            viewModel.register(email, password) { success, message ->

                if (success) {
                    Toast.makeText(this, "Успешна регистрация!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, message ?: "Грешка", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}