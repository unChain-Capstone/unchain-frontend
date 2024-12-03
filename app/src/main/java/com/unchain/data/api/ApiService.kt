package com.unchain.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/v1/users")
    suspend fun registerUser(
        @Header("Authorization") token: String,
        @Body user: UserRegistrationRequest
    ): Response<UserRegistrationResponse>
}

data class UserRegistrationRequest(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String
)

data class UserRegistrationResponse(
    val status: Boolean,
    val message: String,
    val code: Int
)
