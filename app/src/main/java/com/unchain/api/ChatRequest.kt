package com.unchain.api

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<Message>
) {
    // Filter out any messages with isLoading = true when converting to API format
    fun toApiRequest(): Map<String, Any> {
        return mapOf(
            "model" to model,
            "messages" to messages.filterNot { it.isLoading }.map {
                mapOf(
                    "role" to it.role,
                    "content" to it.content
                )
            }
        )
    }
}
