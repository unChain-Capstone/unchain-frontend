package com.unchain.ui.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.unchain.data.preferences.model.SugarConsumption
import com.unchain.data.preferences.preferences.SugarPreferencesManager
import com.unchain.data.preferences.preferences.UserPreferencesManager
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userPreferencesManager: UserPreferencesManager,
    private val sugarPreferencesManager: SugarPreferencesManager
) : ViewModel() {
    val userPreferences = userPreferencesManager.userPreferencesFlow.asLiveData()
    val sugarPreferences = sugarPreferencesManager.sugarPreferencesFlow.asLiveData()

    fun addSugarConsumption(foodName: String, sugarAmount: Int) {
        viewModelScope.launch {
            sugarPreferencesManager.addSugarConsumption(foodName, sugarAmount)
        }
    }

    fun getDailyHistory(date: String): List<SugarConsumption> {
        return sugarPreferences.value?.dailyHistory?.get(date) ?: emptyList()
    }
}