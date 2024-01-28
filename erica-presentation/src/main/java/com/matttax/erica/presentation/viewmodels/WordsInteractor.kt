package com.matttax.erica.presentation.viewmodels

import com.matttax.erica.domain.config.WordGroupConfig
import com.matttax.erica.presentation.model.translate.TranslatedText

interface WordsInteractor {
    fun onWordSelected(position: Int)
    fun onWordDeselected(position: Int)
    fun onDeselectAll()
    fun filterWordsByQuery(query: String)
    suspend fun onGetWords(wordGroupConfig: WordGroupConfig)
    suspend fun onMoveSelectedWords(toId: Long)
    suspend fun onDeleteWordAt(position: Int)
    suspend fun onDeleteSelected()
    suspend fun onGetSets()
    suspend fun onAddWord(translatedText: TranslatedText)
}
