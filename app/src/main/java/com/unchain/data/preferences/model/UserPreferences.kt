package com.unchain.data.preferences.model

data class UserPreferences(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = ""
)