package com.unchain.network

import com.unchain.data.model.SugarHistory
import com.unchain.data.model.HistoryResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("api/v1/histories")
    fun addHistory(@Body history: SugarHistory): Call<SugarHistory>

    @GET("api/v1/histories")
    fun getHistories(): Call<HistoryResponse>

    @DELETE("api/v1/histories/{id}")
    fun deleteHistory(@Path("id") id: Int): Call<Unit>
}
