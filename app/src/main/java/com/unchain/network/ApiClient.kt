package com.unchain.network

import com.unchain.BuildConfig
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
    private const val BASE_URL = BuildConfig.BASE_URL
    
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
                e.printStackTrace()
            }
        }
        
        val request = requestBuilder.build()
        chain.proceed(request)
    }


    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
