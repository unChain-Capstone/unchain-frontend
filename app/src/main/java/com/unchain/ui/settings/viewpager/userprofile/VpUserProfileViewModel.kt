package com.unchain.ui.settings.viewpager.userprofile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unchain.data.model.UpdateProfile
import com.unchain.data.model.UpdateProfileResponse
import com.unchain.data.model.UserProfileResponse
import com.unchain.data.preferences.model.UserPreferences
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.network.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed class UpdateProfileState {
    object Initial : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

class VpUserProfileViewModel(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _height = MutableLiveData<String>()
    val height: LiveData<String> = _height

    private val _weight = MutableLiveData<String>()
    val weight: LiveData<String> = _weight

    private val _dateOfBirth = MutableLiveData<String>()
    val dateOfBirth: LiveData<String> = _dateOfBirth

    private val _gender = MutableLiveData<Boolean>()
    val gender: LiveData<Boolean> = _gender

    private val _updateProfileState = MutableLiveData<UpdateProfileState>()
    val updateProfileState: LiveData<UpdateProfileState> = _updateProfileState

    val userPreferences: Flow<UserPreferences> = userPreferencesManager.userPreferencesFlow

    init {
        loadUserProfile()
    }

    private fun formatDateString(dateString: String): String {
        return dateString.substringBefore("T")
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                ApiClient.apiService.getProfile().enqueue(object : Callback<UserProfileResponse> {
                    override fun onResponse(
                        call: Call<UserProfileResponse>,
                        response: Response<UserProfileResponse>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.data?.let { profile ->
                                _height.value = profile.height.toString()
                                _weight.value = profile.weight.toString()
                                _gender.value = profile.isMale
                                _dateOfBirth.value = formatDateString(profile.dateOfBirth)
                            }
                        }
                    }

                    override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                        Log.e("VpUserProfileViewModel", "Error loading profile", t)
                    }
                })
            } catch (e: Exception) {
                Log.e("VpUserProfileViewModel", "Error loading profile: ${e.message}")
            }
        }
    }

    fun updateUserInfo(newHeight: String, newWeight: String, dateOfBirth: String, isMale: Boolean) {
        viewModelScope.launch {
            _updateProfileState.value = UpdateProfileState.Loading
            try {
                val updateProfile = UpdateProfile(
                    dateOfBirth = "${dateOfBirth}T00:00:00.000Z",  // Add timezone in standard format
                    isMale = isMale,
                    weight = newWeight.toInt(),
                    height = newHeight.toInt()
                )

                ApiClient.apiService.updateProfile(updateProfile).enqueue(object : Callback<UpdateProfileResponse> {
                    override fun onResponse(
                        call: Call<UpdateProfileResponse>,
                        response: Response<UpdateProfileResponse>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.data?.let { profile ->
                                _height.value = profile.height.toString()
                                _weight.value = profile.weight.toString()
                                _gender.value = profile.isMale
                                _dateOfBirth.value = formatDateString(profile.dateOfBirth)
                                _updateProfileState.value = UpdateProfileState.Success
                            }
                        } else {
                            val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                            _updateProfileState.value = UpdateProfileState.Error("Failed to update profile: $errorMessage")
                        }
                    }

                    override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                        _updateProfileState.value = UpdateProfileState.Error("Network error: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                _updateProfileState.value = UpdateProfileState.Error("Error: ${e.message}")
            }
        }
    }
}