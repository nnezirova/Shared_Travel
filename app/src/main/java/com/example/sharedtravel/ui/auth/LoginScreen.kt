package com.example.sharedtravel.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharedtravel.util.AppStrings
import com.example.sharedtravel.util.LanguageManager
import com.example.sharedtravel.util.LocalLanguage
import com.example.sharedtravel.util.StringKey
import com.example.sharedtravel.viewmodel.AuthState
import com.example.sharedtravel.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val currentLang = LocalLanguage.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        val currentState = authState
        when (currentState) {
            is AuthState.Success -> {
                Toast.makeText(context, AppStrings.get(StringKey.LOGIN_SUCCESS, currentLang), Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }
            is AuthState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Language Switcher at the Top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = currentLang == "en",
                onClick = { coroutineScope.launch { LanguageManager.setLanguage(context, "en") } },
                label = { Text("🇬🇧 English") },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            FilterChip(
                selected = currentLang == "bg",
                onClick = { coroutineScope.launch { LanguageManager.setLanguage(context, "bg") } },
                label = { Text("🇧🇬 Български") },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = AppStrings.get(StringKey.LOGIN_TITLE, currentLang),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(AppStrings.get(StringKey.EMAIL_LABEL, currentLang)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(AppStrings.get(StringKey.PASSWORD_LABEL, currentLang)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.loginUser(email, password) },
            enabled = authState !is AuthState.Loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text(AppStrings.get(StringKey.LOGIN_BUTTON, currentLang))
            }
        }

        TextButton(onClick = { viewModel.registerUser(email, password) }) {
            Text(AppStrings.get(StringKey.NO_ACCOUNT_TEXT, currentLang))
        }
        
        Spacer(modifier = Modifier.weight(1.5f))
    }
}
