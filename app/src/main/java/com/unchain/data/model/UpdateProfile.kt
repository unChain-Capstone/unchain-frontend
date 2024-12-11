package com.unchain.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfile(
    @SerialName("dateOfBirth")
    val dateOfBirth: String,
    @SerialName("isMale")
    val isMale: Boolean,
    @SerialName("weight")
    val weight: Int,
    @SerialName("height")
    val height: Int
)