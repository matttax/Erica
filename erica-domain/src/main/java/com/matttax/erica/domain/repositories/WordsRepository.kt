package com.matttax.erica.domain.repositories

import com.matttax.erica.domain.config.WordGroupConfig
import com.matttax.erica.domain.model.WordDomainModel

interface WordsRepository {
    fun getWords(wordGroupConfig: WordGroupConfig): List<WordDomainModel>
    fun addWord(wordDomainModel: WordDomainModel)
    fun removeById(id: Long)
    fun moveToSet(wordId: Long, setId: Long)
}
