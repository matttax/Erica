package com.matttax.erica.presentation.states

import com.matttax.erica.domain.model.SetDomainModel

data class SetsState(
    val sets: List<SetDomainModel>? = null
)
