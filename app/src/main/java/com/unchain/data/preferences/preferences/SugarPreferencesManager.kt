package com.unchain.data.preferences.preferences

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.unchain.data.preferences.model.SugarConsumption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate

private val Context.sugarDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugar_preferences")

class SugarPreferencesManager(private val context: Context) {
    private object PreferencesKeys {
        val TOTAL_SUGAR_AMOUNT = intPreferencesKey("total_sugar_amount")
        val CURRENT_DATE = stringPreferencesKey("current_date")
        val DAILY_HISTORY = stringPreferencesKey("daily_history")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val sugarPreferencesFlow: Flow<SugarPreferences> = context.sugarDataStore.data
        .map { preferences ->
            val currentDate = LocalDate.now().toString()
            val savedDate = preferences[PreferencesKeys.CURRENT_DATE] ?: currentDate

            if (savedDate != currentDate) {
                // Ganti hari, kereset, terus update ke history
                resetDailyConsumption()
                SugarPreferences(
                    totalSugarAmount = 0,
                    currentDate = currentDate,
                    dailyHistory = getDailyHistory()
                )
            } else {
                SugarPreferences(
                    totalSugarAmount = preferences[PreferencesKeys.TOTAL_SUGAR_AMOUNT] ?: 0,
                    currentDate = currentDate,
                    dailyHistory = getDailyHistory()
                )
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun resetDailyConsumption() {
        context.sugarDataStore.edit { preferences ->
            val currentDate = LocalDate.now().toString()
            val currentAmount = preferences[PreferencesKeys.TOTAL_SUGAR_AMOUNT] ?: 0
            val currentHistory = getDailyHistory().toMutableMap()


            if (currentAmount > 0) {
                currentHistory[preferences[PreferencesKeys.CURRENT_DATE] ?: currentDate] =
                    sugarConsumptions
            }

            // Reset buat hari baru
            preferences[PreferencesKeys.TOTAL_SUGAR_AMOUNT] = 0
            preferences[PreferencesKeys.CURRENT_DATE] = currentDate
            preferences[PreferencesKeys.DAILY_HISTORY] = Json.encodeToString(currentHistory)
        }
    }

    private suspend fun getDailyHistory(): Map<String, List<SugarConsumption>> {
        return try {
            val preferences = context.sugarDataStore.data.first()
            val historyJson = preferences[PreferencesKeys.DAILY_HISTORY] ?: return emptyMap()
            Json.decodeFromString<Map<String, List<SugarConsumption>>>(historyJson)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    suspend fun addSugarConsumption(foodName: String, sugarAmount: Int) {
        context.sugarDataStore.edit { preferences ->
            val currentAmount = preferences[PreferencesKeys.TOTAL_SUGAR_AMOUNT] ?: 0
            val newAmount = currentAmount + sugarAmount

            preferences[PreferencesKeys.TOTAL_SUGAR_AMOUNT] = newAmount


            if (preferences[PreferencesKeys.CURRENT_DATE] == null) {
                preferences[PreferencesKeys.CURRENT_DATE] = LocalDate.now().toString()
            }

            val newConsumption = SugarConsumption(
                foodName = foodName,
                sugarAmount = sugarAmount
            )
            sugarConsumptions = sugarConsumptions + newConsumption
        }
    }

    suspend fun clearSugarData() {
        context.sugarDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private var sugarConsumptions: List<SugarConsumption> = emptyList()
}