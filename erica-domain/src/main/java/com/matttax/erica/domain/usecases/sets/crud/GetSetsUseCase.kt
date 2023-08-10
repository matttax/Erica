package com.matttax.erica.domain.usecases.sets.crud

import com.matttax.erica.domain.architecture.UseCase
import com.matttax.erica.domain.config.SetGroupConfig
import com.matttax.erica.domain.model.SetDomainModel
import com.matttax.erica.domain.repositories.SetsRepository

class GetSetsUseCase(
    private val setsRepository: SetsRepository
) : UseCase<SetGroupConfig, List<SetDomainModel>> {

    override suspend fun execute(
        input: SetGroupConfig,
        onResult: (List<SetDomainModel>) -> Unit
    ) {
        val sets = setsRepository.getSets(input)
        onResult(sets)
    }
}
