package com.matttax.erica.domain.usecases.words.crud

import com.matttax.erica.domain.architecture.BackgroundUseCase
import com.matttax.erica.domain.config.MoveWordsRequest
import com.matttax.erica.domain.coroutines.CoroutineContextProvider
import com.matttax.erica.domain.repositories.WordsRepository

class MoveWordsUseCase(
    private val wordsRepository: WordsRepository
) : BackgroundUseCase<MoveWordsRequest, Boolean>(CoroutineContextProvider.Default) {

    override fun executeInBackground(input: MoveWordsRequest): Boolean {
        if (input.words.isEmpty())
            return false
        wordsRepository.moveToSet(input.idFrom, input.idTo, *input.words.toLongArray())
        return true
    }

}
