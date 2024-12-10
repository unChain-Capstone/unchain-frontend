package com.unchain.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfile (
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val age: Int? = null,
    val weight: Int? = null,
    val height: String? = null,
)