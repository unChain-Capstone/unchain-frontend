package com.unchain.data.model

import kotlinx.serialization.Serializable


@Serializable
data class HistoryResponse(
    val status: Boolean,
    val data: List<SugarHistory>,
    val message: String,
    val code: Int
)
