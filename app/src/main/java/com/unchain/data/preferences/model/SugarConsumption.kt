package com.unchain.data.preferences.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SugarConsumption(
    val id: String = UUID.randomUUID().toString(),
    val foodName: String,
    val sugarAmount: Int,
    val timestamp: Long = System.currentTimeMillis()
)