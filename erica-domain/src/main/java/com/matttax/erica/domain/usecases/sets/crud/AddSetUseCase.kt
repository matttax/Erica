package com.matttax.erica.domain.usecases.sets.crud

import com.matttax.erica.domain.architecture.UseCase
import com.matttax.erica.domain.repositories.SetsRepository

class AddSetUseCase(
    private val setsRepository: SetsRepository
) : UseCase<Pair<String, String>, Long> {

    override suspend fun execute(input: Pair<String, String>, onResult: (Long) -> Unit) {
        val isSuccessful = setsRepository.addSet(input.first, input.second)
        onResult(isSuccessful)
    }
}
