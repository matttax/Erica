package com.matttax.erica.domain.repositories

import com.matttax.erica.domain.config.WordGroupConfig
import com.matttax.erica.domain.model.WordDomainModel

interface WordsRepository {
    fun getWords(wordGroupConfig: WordGroupConfig): List<WordDomainModel>
    fun addWord(wordDomainModel: WordDomainModel): Boolean
    fun remove(wordId: Long, setId: Long)
    fun moveToSet(fromSetId: Long, toSetId: Long, vararg wordIds: Long)
    fun onWordAnswered(wordId: Long, isCorrect: Boolean)
}
