package com.unchain.data.preferences.repository

import android.content.Context
import androidx.datastore.dataStore
import com.unchain.data.preferences.model.SugarConsumption
import com.unchain.data.preferences.preferences.SugarPreferences
import com.unchain.data.preferences.preferences.SugarPreferencesSerializer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.Flow

class SugarRepository(private val context: Context) {
    companion object {
        private val Context.sugarDataStore by dataStore(
            fileName = "sugar_prefs.pb",
            serializer = SugarPreferencesSerializer()
        )
    }

    val sugarPreferences: Flow<SugarPreferences> = context.sugarDataStore.data
        .catch { exception ->
            emit(SugarPreferences())
        }

    suspend fun addSugarConsumption(foodName: String, sugarAmount: Int) {
        context.sugarDataStore.updateData { preferences ->
            val newConsumption = SugarConsumption(
                foodName = foodName,
                sugarAmount = sugarAmount
            )
            preferences.copy(
                totalSugarAmount = preferences.totalSugarAmount + sugarAmount,
                sugarConsumptions = preferences.sugarConsumptions + newConsumption
            )
        }
    }
}