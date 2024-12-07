package com.unchain.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponse(
    val status: String,
    val data: DashboardData,
    val message: String,
    val code: Int
)

@Serializable
data class DashboardData(
    val name: String,
    @SerialName("daily_sugar")
    val dailySugar: Int,
    @SerialName("weekly_sugar")
    val weeklySugar: Int,
    @SerialName("monthly_sugar")
    val monthlySugar: Int,
    @SerialName("daily_consume")
    val dailyConsume: List<Consumption>
)

@Serializable
data class Consumption(
    val title: String,
    val weight: Int,
    val isBeverage: Boolean = true
)
