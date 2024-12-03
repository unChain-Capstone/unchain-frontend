package com.unchain.data.model

data class HistoryResponse(
    val status: Boolean,
    val data: List<SugarHistory>,
    val message: String,
    val code: Int
)
