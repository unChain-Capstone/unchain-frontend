package com.unchain.ui.behavior

import com.google.firebase.auth.FirebaseAuth
import com.unchain.data.model.BehaviorResponse
import com.unchain.network.ApiService
import javax.inject.Inject

class BehaviorRepository @Inject constructor(
    private val apiService: ApiService,
    private val auth: FirebaseAuth
) {
    suspend fun getBehavior(): BehaviorResponse {
        val token = auth.currentUser?.getIdToken(false)?.result?.token
            ?: throw Exception("User not authenticated")
        
        val response = apiService.getBehavior("Bearer $token")
        
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Response body is null")
        } else {
            throw Exception("Failed to fetch behavior data: ${response.code()}")
        }
    }
}
