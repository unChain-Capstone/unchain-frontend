package com.unchain.ui.settings.viewpager.userprofile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.unchain.data.model.UserProfileResponse
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.network.ApiClient
import kotlinx.coroutines.launch
import okhttp3.Callback
import retrofit2.*

class VpUserProfileViewModel(
    private val userPreferencesManager: UserPreferencesManager,
): ViewModel() {
    val userPreferences = userPreferencesManager.userPreferencesFlow.asLiveData()

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

    // Fungsi untuk mengupdate data
    fun updateUserInfo(newHeight: String, newWeight: String, newAge: String) {
        _height.value = newHeight
        _weight.value = newWeight
        _age.value = newAge
    }
}