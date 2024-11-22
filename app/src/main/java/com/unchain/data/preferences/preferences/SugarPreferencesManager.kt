package com.unchain.data.preferences.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.unchain.data.preferences.model.SugarConsumption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.sugarDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugar_preferences")

class SugarPreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val TOTAL_SUGAR_AMOUNT = intPreferencesKey("total_sugar_amount")
        val SUGAR_CONSUMPTIONS = stringPreferencesKey("sugar_consumptions")
    }

    val sugarPreferencesFlow: Flow<SugarPreferences> = context.sugarDataStore.data
        .map { preferences ->
            try {
                val amount = preferences[PreferencesKeys.TOTAL_SUGAR_AMOUNT] ?: 0
                SugarPreferences(
                    totalSugarAmount = amount,
                    sugarConsumptions = emptyList()
                )
            } catch (e: ClassCastException) {

                clearSugarData()
                SugarPreferences(
                    totalSugarAmount = 0,
                    sugarConsumptions = emptyList()
                )
            }
        }

    suspend fun addSugarConsumption(foodName: String, sugarAmount: Int) {
        context.sugarDataStore.edit { preferences ->
            val currentAmount = preferences[PreferencesKeys.TOTAL_SUGAR_AMOUNT] ?: 0
            preferences[PreferencesKeys.TOTAL_SUGAR_AMOUNT] = currentAmount + sugarAmount
        }
    }

    suspend fun clearSugarData() {
        context.sugarDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}