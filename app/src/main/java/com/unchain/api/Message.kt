package com.unchain.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Message(
    val role: String,
    val content: String,
    @Transient
    val isLoading: Boolean = false
)
