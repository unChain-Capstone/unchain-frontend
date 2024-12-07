package com.unchain.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val status: Boolean,
    val data: UserData,
    val message: String,
    val code: Int
)

@Serializable
data class UserData(
    val id: String,
    val name: String,
    val age: Int,
    val weight: Int,
    val height: Int,
    val email: String,
    val photoUrl: String
)
