package com.example.sharedtravel.util

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "settings")

val LocalLanguage = staticCompositionLocalOf { "en" }

object LanguageManager {
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    
    val currentLanguage = MutableStateFlow("en")

    /**
     * To be called in MainActivity onCreate to ensure the saved language is applied.
     */
    fun init(context: Context) {
        val lang = runBlocking { 
            context.dataStore.data.map { it[LANGUAGE_KEY] ?: "en" }.first() 
        }
        currentLanguage.value = lang
    }

    suspend fun setLanguage(context: Context, languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
        currentLanguage.value = languageCode
    }
}
