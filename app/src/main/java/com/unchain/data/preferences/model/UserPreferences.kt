package com.unchain.data.preferences.model

data class UserPreferences(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val height: Int = 0,
    val dateOfBirth: String = "",
    val isMale: Boolean = true,
    val weight: Int = 0,
)