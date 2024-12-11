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
        val HEIGHT = intPreferencesKey("height")
        val DATE_OF_BIRTH = stringPreferencesKey("date_of_birth")
        val IS_MALE = booleanPreferencesKey("is_male")
        val WEIGHT = intPreferencesKey("weight")
    }

    suspend fun saveUser(user: UserPreferences) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = user.userId
            preferences[PreferencesKeys.DISPLAY_NAME] = user.displayName
            preferences[PreferencesKeys.EMAIL] = user.email
            preferences[PreferencesKeys.PHOTO_URL] = user.photoUrl
            preferences[PreferencesKeys.HEIGHT] = user.height
            preferences[PreferencesKeys.DATE_OF_BIRTH] = user.dateOfBirth
            preferences[PreferencesKeys.IS_MALE] = user.isMale
            preferences[PreferencesKeys.WEIGHT] = user.weight
        }
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                userId = preferences[PreferencesKeys.USER_ID] ?: "",
                displayName = preferences[PreferencesKeys.DISPLAY_NAME] ?: "",
                email = preferences[PreferencesKeys.EMAIL] ?: "",
                photoUrl = preferences[PreferencesKeys.PHOTO_URL] ?: "",
                height = preferences[PreferencesKeys.HEIGHT] ?: 0,
                dateOfBirth = preferences[PreferencesKeys.DATE_OF_BIRTH] ?: "",
                isMale = preferences[PreferencesKeys.IS_MALE] ?: true,
                weight = preferences[PreferencesKeys.WEIGHT] ?: 0
            )
        }

    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}