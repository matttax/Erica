package com.matttax.erica.presentation.viewmodels

import com.matttax.erica.domain.config.WordGroupConfig
import com.matttax.erica.presentation.states.WordsState

interface WordsViewModel: StateViewModel<WordsState?> {
    fun onWordSelected(position: Int)
    fun onWordDeselected(position: Int)
    fun onDeselectAll()
    suspend fun onGetWords(wordGroupConfig: WordGroupConfig)
    suspend fun onMoveSelected(toId: Long)
    suspend fun onDelete(position: Int)
    suspend fun onDeleteSelected()
    suspend fun onGetSets()
}
