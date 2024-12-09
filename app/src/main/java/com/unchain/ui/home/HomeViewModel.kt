package com.unchain.ui.home

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.unchain.data.model.DashboardData
import com.unchain.data.model.HistoryResponse
import com.unchain.data.model.SugarHistory
import com.unchain.data.preferences.model.SugarConsumption
import com.unchain.data.preferences.preferences.SugarPreferencesManager
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.data.ml.RecommendationHelper
import com.unchain.data.ml.RecommendationItem
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
    @RequiresApi(Build.VERSION_CODES.O)
    val sugarPreferences = sugarPreferencesManager.sugarPreferencesFlow.asLiveData()

    private val _historyData = MutableLiveData<List<SugarHistory>>()
    val historyData: LiveData<List<SugarHistory>> = _historyData

    private val _dashboardData = MutableLiveData<DashboardData>()
    val dashboardData: LiveData<DashboardData> = _dashboardData

    private val _recommendations = MutableLiveData<List<RecommendationItem>>()
    val recommendations: LiveData<List<RecommendationItem>> = _recommendations

    private lateinit var recommendationHelper: RecommendationHelper

    private var lastFetchTime: Long = 0
    private val FETCH_INTERVAL = 5 * 60 * 1000 // 5 menit dalam milidetik

    fun loadHistories(forceRefresh: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!forceRefresh && _historyData.value != null && 
            (currentTime - lastFetchTime) < FETCH_INTERVAL) {
            return
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

    fun fetchDashboard(userId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getDashboard(userId)
                if (response.isSuccessful) {
                    response.body()?.let { dashboardResponse -> 
                        Log.d("HomeViewModel", "Dashboard data received: ${dashboardResponse.data}")
                        _dashboardData.value = dashboardResponse.data
                    }
                } else {
                    Log.e("HomeViewModel", "Error fetching dashboard: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception fetching dashboard", e)
            }
        }
    }

    fun addSugarConsumption(foodName: String, sugarAmount: Int) {
        viewModelScope.launch {
            sugarPreferencesManager.addSugarConsumption(foodName, sugarAmount)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDailyHistory(date: String): List<SugarConsumption> {
        return sugarPreferences.value?.dailyHistory?.get(date) ?: emptyList()
    }

    fun initRecommendationSystem(context: Context) {
        recommendationHelper = RecommendationHelper(context)
    }

    private fun getDefaultRecommendation(): List<RecommendationItem> {
        return listOf(
            RecommendationItem(
                id = 1,
                sugarLevel = "Normal",
                score = 1.0f,
                recommendation = "No data available. This is a default recommendation."
            )
        )
    }

    fun updateRecommendations(weeklyIntake: Float) {
        viewModelScope.launch {
            try {
                val output = recommendationHelper.getRecommendations(weeklyIntake)
                val processedRecommendations = RecommendationHelper.processRecommendations(output)
                _recommendations.value = processedRecommendations
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error getting recommendations", e)
                _recommendations.value = getDefaultRecommendation()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::recommendationHelper.isInitialized) {
            recommendationHelper.close()
        }
    }
}