package com.unchain.ui.settings.viewpager.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.unchain.data.preferences.preferences.UserPreferencesManager

class VpUserProfileModelFactory(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VpUserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VpUserProfileViewModel(userPreferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}