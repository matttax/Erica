package com.matttax.erica.domain.usecases.words.crud

import com.matttax.erica.domain.architecture.UseCase
import com.matttax.erica.domain.model.WordDomainModel
import com.matttax.erica.domain.repositories.WordsRepository

class AddWordUseCase(
    private val wordsRepository: WordsRepository
) : UseCase<WordDomainModel, Boolean> {

    override suspend fun execute(input: WordDomainModel, onResult: (Boolean) -> Unit) {
        val isSuccessful = wordsRepository.addWord(input)
        onResult(isSuccessful)
    }
}
