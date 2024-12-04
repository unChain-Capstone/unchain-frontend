package com.unchain.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.unchain.data.model.HistoryResponse
import com.unchain.data.model.SugarHistory
import com.unchain.data.preferences.model.SugarConsumption
import com.unchain.data.preferences.preferences.SugarPreferencesManager
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.network.ApiClient
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel(
    private val userPreferencesManager: UserPreferencesManager,
    private val sugarPreferencesManager: SugarPreferencesManager
) : ViewModel() {
    val userPreferences = userPreferencesManager.userPreferencesFlow.asLiveData()
    val sugarPreferences = sugarPreferencesManager.sugarPreferencesFlow.asLiveData()

    private val _historyData = MutableLiveData<List<SugarHistory>>()
    val historyData: LiveData<List<SugarHistory>> = _historyData

    private var lastFetchTime: Long = 0
    private val FETCH_INTERVAL = 5 * 60 * 1000 // 5 menit dalam milidetik

    fun loadHistories(forceRefresh: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!forceRefresh && _historyData.value != null && 
            (currentTime - lastFetchTime) < FETCH_INTERVAL) {
            return // Gunakan data yang sudah ada
        }

        viewModelScope.launch {
            ApiClient.apiService.getHistories().enqueue(object : Callback<HistoryResponse> {
                override fun onResponse(
                    call: Call<HistoryResponse>,
                    response: Response<HistoryResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { historyResponse ->
                            if (historyResponse.status) {
                                _historyData.value = historyResponse.data
                                lastFetchTime = currentTime
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                    // Handle error
                }
            })
        }
    }

    fun addSugarConsumption(foodName: String, sugarAmount: Int) {
        viewModelScope.launch {
            sugarPreferencesManager.addSugarConsumption(foodName, sugarAmount)
        }
    }

    fun getDailyHistory(date: String): List<SugarConsumption> {
        return sugarPreferences.value?.dailyHistory?.get(date) ?: emptyList()
    }
}