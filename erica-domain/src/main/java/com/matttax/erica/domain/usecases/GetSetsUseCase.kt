package com.matttax.erica.domain.usecases

import com.matttax.erica.domain.architecture.UseCase
import com.matttax.erica.domain.model.SetDomainModel
import com.matttax.erica.domain.repositories.SetsRepository

class GetSetsUseCase(
    private val setsRepository: SetsRepository
) : UseCase<Unit, List<SetDomainModel>> {

    override suspend fun execute(input: Unit, onResult: (List<SetDomainModel>) -> Unit) {
        val sets = setsRepository.getSets()
        onResult(sets)
    }
}
