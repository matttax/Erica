package com.matttax.erica.domain.config

data class MoveWordsRequest(
    val idFrom: Long,
    val idTo: Long,
    val words: List<Long>
)
