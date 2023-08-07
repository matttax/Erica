package com.matttax.erica.domain.repositories

import com.matttax.erica.domain.model.SetDomainModel

interface SetsRepository {
    fun getSets(): List<SetDomainModel>
    fun removeById(id: Long)
}
