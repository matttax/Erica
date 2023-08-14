package com.matttax.erica.domain.usecases.sets.crud

import com.matttax.erica.domain.architecture.UseCase
import com.matttax.erica.domain.model.SetDomainModel
import com.matttax.erica.domain.repositories.SetsRepository

class UpdateSetUseCase(
    private val setsRepository: SetsRepository
) : UseCase<SetDomainModel, Unit> {

    override suspend fun execute(input: SetDomainModel, onResult: (Unit) -> Unit) {
        setsRepository.updateSet(input.id, input.name, input.description)
        onResult(Unit)
    }

}