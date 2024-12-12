package com.unchain.data.manager

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    var userId: String?
        get() = sharedPreferences.getString(KEY_USER_ID, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_USER_ID, value).apply()
        }

    var authToken: String?
        get() = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_AUTH_TOKEN, value).apply()
        }

    fun isLoggedIn(): Boolean {
        return !userId.isNullOrEmpty() && !authToken.isNullOrEmpty()
    }

    fun clearUserData() {
        sharedPreferences.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_AUTH_TOKEN)
            apply()
        }
    }
}
