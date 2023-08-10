package com.matttax.erica.domain.usecases.words.crud

import com.matttax.erica.domain.architecture.UseCase
import com.matttax.erica.domain.config.WordGroupConfig
import com.matttax.erica.domain.model.WordDomainModel
import com.matttax.erica.domain.repositories.WordsRepository

class GetWordsUseCase(
    private val wordsRepository: WordsRepository
): UseCase<WordGroupConfig, List<WordDomainModel>> {
    override suspend fun execute(
        input: WordGroupConfig,
        onResult: (List<WordDomainModel>) -> Unit
    ) {
        val words = wordsRepository.getWords(input)
        onResult(words)
    }
}
