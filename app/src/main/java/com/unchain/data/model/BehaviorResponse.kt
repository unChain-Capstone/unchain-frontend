package com.unchain.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BehaviorResponse(
    val status: Boolean,
    val data: BehaviorData,
    val message: String,
    val code: Int
)

@Serializable
data class BehaviorData(
    val behaviourStatus: String,
    val message: String
)
