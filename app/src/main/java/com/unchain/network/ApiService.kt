package com.unchain.network

import com.unchain.data.model.AddHistoryResponse
import com.unchain.data.model.BehaviorResponse
import com.unchain.data.model.HistoryResponse
import com.unchain.data.model.DashboardResponse
import com.unchain.data.model.SugarHistory
import com.unchain.data.model.UpdateHistoryResponse
import com.unchain.data.model.UpdateProfile
import com.unchain.data.model.UpdateProfileResponse
import com.unchain.data.model.UserProfileResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/v1/histories")
    fun addHistory(@Body history: SugarHistory): Call<AddHistoryResponse>

    @GET("api/v1/users/profile")
    fun getProfile(): Call<UserProfileResponse>

    @PUT("api/v1/users/profile")
    fun updateProfile(
        @Body userInfo: UpdateProfile
    ): Call<UpdateProfileResponse>

    @POST("api/v1/users/profile")
    fun insertUser(@Body userInfo: UpdateProfile): Call<UpdateHistoryResponse>

    @GET("api/v1/histories")
    fun getHistories(): Call<HistoryResponse>

    @DELETE("api/v1/histories/{id}")
    fun deleteHistory(@Path("id") id: Int): Call<Unit>

    @GET("api/v1/dashboard")
    suspend fun getDashboard(@Query("userId") userId: String): Response<DashboardResponse>

    @GET("api/v1/users/behaviour")
    suspend fun getBehavior(@Header("Authorization") token: String): Response<BehaviorResponse>
}
