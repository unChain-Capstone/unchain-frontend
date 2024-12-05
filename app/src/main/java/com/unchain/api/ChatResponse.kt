package com.unchain.api

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val choices: List<Choice>
)
