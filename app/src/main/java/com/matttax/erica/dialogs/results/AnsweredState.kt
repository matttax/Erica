package com.matttax.erica.dialogs.results

data class AnsweredState(
    val correctAnswer: String,
    val isCorrect: Boolean,
    val showNotIncorrect: Boolean
)
