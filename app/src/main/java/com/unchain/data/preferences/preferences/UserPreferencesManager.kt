package com.unchain.data.preferences.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.unchain.data.preferences.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val USER_ID = stringPreferencesKey("user_id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL = stringPreferencesKey("email")
        val PHOTO_URL = stringPreferencesKey("photo_url")
    }

    suspend fun saveUser(user: UserPreferences) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = user.userId
            preferences[PreferencesKeys.DISPLAY_NAME] = user.displayName
            preferences[PreferencesKeys.EMAIL] = user.email
            preferences[PreferencesKeys.PHOTO_URL] = user.photoUrl
        }
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                userId = preferences[PreferencesKeys.USER_ID] ?: "",
                displayName = preferences[PreferencesKeys.DISPLAY_NAME] ?: "",
                email = preferences[PreferencesKeys.EMAIL] ?: "",
                photoUrl = preferences[PreferencesKeys.PHOTO_URL] ?: ""
            )
        }

    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}