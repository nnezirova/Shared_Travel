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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.example.sharedtravel.ui.auth.LoginScreen
import com.example.sharedtravel.ui.home.HomeActivity
import com.example.sharedtravel.ui.theme.SharedTravelTheme
import com.example.sharedtravel.util.LanguageManager
import com.example.sharedtravel.util.LocalLanguage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.init(this)
        enableEdgeToEdge()
        setContent {
            val lang by LanguageManager.currentLanguage.collectAsState()
            
            key(lang) {
                CompositionLocalProvider(LocalLanguage provides lang) {
                    SharedTravelTheme {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLoginSuccess = {
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
    }
}
