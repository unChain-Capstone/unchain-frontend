package com.unchain.ui.settings.viewpager.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.unchain.data.preferences.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VpSettingsViewModel : ViewModel() {
    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Initial)
    val logoutState: StateFlow<LogoutState> = _logoutState

    fun logout(userPreferencesManager: UserPreferencesManager) {
        viewModelScope.launch {
            try {
                _logoutState.value = LogoutState.Loading
                // Clear Firebase auth
                FirebaseAuth.getInstance().signOut()
                // Clear user preferences
                userPreferencesManager.clearUser()
                _logoutState.value = LogoutState.Success
            } catch (e: Exception) {
                _logoutState.value = LogoutState.Error(e.message ?: "Logout failed")
            }
        }
    }
}

sealed class LogoutState {
    object Initial : LogoutState()
    object Loading : LogoutState()
    object Success : LogoutState()
    data class Error(val message: String) : LogoutState()
}