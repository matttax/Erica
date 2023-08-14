package com.matttax.erica.domain.config

data class MoveWordsRequest(
    val idFrom: Int,
    val idTo: Long,
    val words: List<Long>
)
