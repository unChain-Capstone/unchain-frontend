package com.unchain.network

import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Tasks
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

object ApiClient {
    private const val BASE_URL = "https://dev-unchain-742693144827.us-central1.run.app/"

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val user = FirebaseAuth.getInstance().currentUser
        
        val requestBuilder = original.newBuilder()
            .header("Content-Type", "application/json")
        
        if (user != null) {
            try {
                val tokenResult = Tasks.await(user.getIdToken(false))
                tokenResult.token?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }
            } catch (e: Exception) {
                // Handle token retrieval failure
                e.printStackTrace()
            }
        }
        
        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val contentType = "application/json".toMediaType()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
