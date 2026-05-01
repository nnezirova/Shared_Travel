package com.example.sharedtravel

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.sharedtravel.ui.auth.LoginScreen
import com.example.sharedtravel.ui.home.HomeActivity
import com.example.sharedtravel.ui.theme.SharedTravelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SharedTravelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        onLoginSuccess = {
                            // Navigate to HomeActivity on successful login
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}
