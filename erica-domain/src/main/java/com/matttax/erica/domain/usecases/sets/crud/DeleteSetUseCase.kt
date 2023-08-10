package com.matttax.erica.domain.usecases.sets.crud

import com.matttax.erica.domain.architecture.UseCase
import com.matttax.erica.domain.repositories.SetsRepository

class DeleteSetUseCase(
    private val setsRepository: SetsRepository
) : UseCase<Long, Unit> {

    override suspend fun execute(input: Long, onResult: (Unit) -> Unit) {
        setsRepository.removeById(input)
        onResult(Unit)
    }

}