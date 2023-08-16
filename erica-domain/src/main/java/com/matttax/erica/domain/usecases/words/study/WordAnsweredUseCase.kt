package com.matttax.erica.domain.usecases.words.study

import com.matttax.erica.domain.architecture.UseCase
import com.matttax.erica.domain.model.WordDomainModel
import com.matttax.erica.domain.repositories.SetsRepository
import com.matttax.erica.domain.repositories.WordsRepository

class WordAnsweredUseCase(
    val wordsRepository: WordsRepository,
    val setsRepository: SetsRepository
) : UseCase<Pair<WordDomainModel, String>, Boolean> {

    override suspend fun execute(input: Pair<WordDomainModel, String>, onResult: (Boolean) -> Unit) {
        input.first.id?.let {
            wordsRepository.onWordAnswered(it, input.first.translation == input.second)
            setsRepository.touchSet(input.first.setId)
        }
        onResult(input.first.translation == input.second)
    }

}
