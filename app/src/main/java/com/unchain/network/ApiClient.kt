package com.unchain.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import com.google.firebase.auth.FirebaseAuth

object ApiClient {
    private const val BASE_URL = "https://dev-unchain-742693144827.us-central1.run.app/"

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.result?.token
        
        val requestBuilder = original.newBuilder()
            .header("Content-Type", "application/json")
        
        token?.let {
            requestBuilder.header("Authorization", "Bearer $it")
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

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
