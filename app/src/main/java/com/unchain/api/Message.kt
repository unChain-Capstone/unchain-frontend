package com.unchain.api

data class Message(
    val role: String,
    val content: String,
    val isLoading: Boolean = false  // Add this parameter
)
