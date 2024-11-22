package com.unchain.data.preferences.preferences

import kotlinx.serialization.Serializable
import com.unchain.data.preferences.model.SugarConsumption

@Serializable
data class SugarPreferences(
    val totalSugarAmount: Int = 0,
    val sugarConsumptions: List<SugarConsumption> = emptyList()
)