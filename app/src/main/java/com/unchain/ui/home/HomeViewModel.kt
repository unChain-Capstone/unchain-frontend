package com.unchain.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.unchain.data.preferences.model.UserPreferences
import com.unchain.data.preferences.preferences.UserPreferencesManager

class HomeViewModel(private val userPreferencesManager: UserPreferencesManager) : ViewModel() {
    val userPreferences: LiveData<UserPreferences> = userPreferencesManager.userPreferencesFlow.asLiveData()
}