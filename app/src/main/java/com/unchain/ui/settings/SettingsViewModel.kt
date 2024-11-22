package com.unchain.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.unchain.data.preferences.preferences.UserPreferencesManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferencesManager = UserPreferencesManager(application)

    val userPreferences = userPreferencesManager.userPreferencesFlow.asLiveData()
}