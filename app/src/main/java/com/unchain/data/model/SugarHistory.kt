package com.unchain.data.model

import kotlinx.serialization.Serializable


@Serializable
data class SugarHistory(
    val id: Int? = null,
    val title: String,
    val weight: Int,
    val isBeverage: Boolean,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
