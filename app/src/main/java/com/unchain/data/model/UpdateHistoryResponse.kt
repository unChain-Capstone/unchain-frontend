package com.unchain.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateHistoryResponse (
    val status: Boolean,
    val data: SugarHistory,
    val message: String,
    val code: Int
)