package com.matttax.erica.domain.usecases.words.crud

import com.matttax.erica.domain.architecture.UseCase
import com.matttax.erica.domain.repositories.WordsRepository

class DeleteWordsUseCase(
    private val wordsRepository: WordsRepository
) : UseCase<Pair<Long, Long>, Boolean> {

    override suspend fun execute(input: Pair<Long, Long>, onResult: (Boolean) -> Unit) {
        wordsRepository.remove(input.first, input.second)
        onResult(true)
    }

}
