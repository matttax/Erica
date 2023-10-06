package com.matttax.erica.presentation.states

import com.matttax.erica.domain.model.WordDomainModel

data class WordsState(
    val words: List<WordDomainModel>? = null,
)
