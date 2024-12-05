package com.unchain.api

import kotlinx.serialization.Serializable

@Serializable
data class Choice(
    val message: Message
)