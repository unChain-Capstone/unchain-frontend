package com.unchain.ui.settings.viewpager.userprofile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.unchain.data.model.UpdateHistoryResponse
import com.unchain.data.model.UpdateProfile
import com.unchain.data.model.UserProfileResponse
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.*

sealed class UpdateProfileState {
    object Initial : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

class VpUserProfileViewModel(
    private val userPreferencesManager: UserPreferencesManager,
): ViewModel() {
    val userPreferences = userPreferencesManager.userPreferencesFlow.asLiveData()

    private val _updateProfileState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Initial)
    val updateProfileState: StateFlow<UpdateProfileState> = _updateProfileState

    private val _tvFullName = MutableLiveData<String>()
    val tvFullName: LiveData<String> = _tvFullName

    private val _tvEmail = MutableLiveData<String>()
    val tvEmail: LiveData<String> = _tvEmail

    private val _height = MutableLiveData<String>()
    val height: LiveData<String> = _height

    private val _weight = MutableLiveData<String>()
    val weight: LiveData<String> = _weight

    private val _age = MutableLiveData<String>()
    val age: LiveData<String> = _age

    fun updateUserInfo(newHeight: String, newWeight: String, newAge: String) {
        viewModelScope.launch {
            try {
                _updateProfileState.value = UpdateProfileState.Loading
                
                val userPrefs = userPreferencesManager.userPreferencesFlow.first()
                val userId = userPrefs.userId
                
                val updateProfile = UpdateProfile(
                    height = newHeight,
                    weight = newWeight.toIntOrNull(),
                    age = newAge.toIntOrNull()
                )
                
                Log.d("ProfileUpdate", "Sending update request: $updateProfile")
                
                ApiClient.apiService.updateProfile(userId, updateProfile).enqueue(object : Callback<UpdateHistoryResponse> {
                    override fun onResponse(call: Call<UpdateHistoryResponse>, response: Response<UpdateHistoryResponse>) {
                        if (response.isSuccessful) {
                            _height.value = newHeight
                            _weight.value = newWeight
                            _age.value = newAge
                            _updateProfileState.value = UpdateProfileState.Success
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                            Log.e("ProfileUpdate", "Update failed: $errorBody")
                            _updateProfileState.value = UpdateProfileState.Error("Failed to update profile: $errorBody")
                        }
                    }

                    override fun onFailure(call: Call<UpdateHistoryResponse>, t: Throwable) {
                        Log.e("ProfileUpdate", "Network error", t)
                        _updateProfileState.value = UpdateProfileState.Error("Network error: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("ProfileUpdate", "Exception during update", e)
                _updateProfileState.value = UpdateProfileState.Error("Error: ${e.message}")
            }
        }
    }
}