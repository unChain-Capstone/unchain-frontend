package com.unchain.ui.behavior

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.unchain.data.model.BehaviorResponse
import com.unchain.network.ApiService
import javax.inject.Inject

class BehaviorRepository @Inject constructor(
    private val apiService: ApiService,
    private val auth: FirebaseAuth
) {
    private val TAG = "BehaviorRepository"

    suspend fun getBehavior(): BehaviorResponse {
        Log.d(TAG, "Making API call to get behavior data")
        val token = auth.currentUser?.getIdToken(false)?.result?.token
            ?: throw Exception("User not authenticated")
        
        Log.d(TAG, "Got auth token, calling API")
        val response = apiService.getBehavior("Bearer $token")
        Log.d(TAG, "Received API response: ${response.code()}")
        
        if (response.isSuccessful) {
            val data = response.body()
            Log.d(TAG, "Received behavior data: $data")
            return data ?: throw Exception("Response body is null")
        } else {
            Log.e(TAG, "Failed to fetch behavior data: ${response.code()}")
            throw Exception("Failed to fetch behavior data: ${response.code()}")
        }
    }
}
