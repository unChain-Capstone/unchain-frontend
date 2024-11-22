package com.unchain.data.preferences.preferences

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import com.unchain.data.preferences.model.SugarConsumption
import java.time.LocalDate

@Serializable
data class SugarPreferences @RequiresApi(Build.VERSION_CODES.O) constructor(
    val totalSugarAmount: Int = 0,
    val currentDate: String = LocalDate.now().toString(),
    val dailyHistory: Map<String, List<SugarConsumption>> = emptyMap(),
    val sugarConsumptions: List<SugarConsumption> = emptyList()
)