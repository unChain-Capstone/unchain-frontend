package com.unchain.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfile (
    val age: Int? = null,
    val weight: Int? = null,
    val height: String? = null,
)