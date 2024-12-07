package com.unchain.ui.settings.viewpager.userprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VpUserProfileViewModel : ViewModel() {
    private val _height = MutableLiveData<String>()
    val height: LiveData<String> = _height

    private val _weight = MutableLiveData<String>()
    val weight: LiveData<String> = _weight

    private val _dob = MutableLiveData<String>()
    val dob: LiveData<String> = _dob

    // Fungsi untuk mengupdate data
    fun updateUserInfo(newHeight: String, newWeight: String, newDOB: String) {
        _height.value = newHeight
        _weight.value = newWeight
        _dob.value = newDOB
    }
}