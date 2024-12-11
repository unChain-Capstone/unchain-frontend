package com.unchain.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileResponse(
    val status: Boolean,
    val data: UserProfile,
    val message: String,
    val code: Int
)

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val isMale: Boolean,
    val dateOfBirth: String,
    val weight: Int,
    val height: Int,
    val email: String,
    val photoUrl: String,
    val createdAt: String,
    val updatedAt: String
)
