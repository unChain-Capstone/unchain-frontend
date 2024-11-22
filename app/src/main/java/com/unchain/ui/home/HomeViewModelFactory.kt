package com.unchain.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.unchain.data.preferences.preferences.UserPreferencesManager

class HomeViewModelFactory(private val userPreferencesManager: UserPreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(userPreferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}