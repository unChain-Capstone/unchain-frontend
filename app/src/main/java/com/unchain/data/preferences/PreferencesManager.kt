package com.unchain.data.preferences

import android.content.Context

class PreferencesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("UnchainPrefs", Context.MODE_PRIVATE)

    fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean("isFirstTime", true)
    }

    fun setFirstTimeDone() {
        sharedPreferences.edit().putBoolean("isFirstTime", false).apply()
    }
}