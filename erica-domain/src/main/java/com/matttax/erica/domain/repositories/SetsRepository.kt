package com.matttax.erica.domain.repositories

import com.matttax.erica.domain.config.SetGroupConfig
import com.matttax.erica.domain.model.SetDomainModel

interface SetsRepository {
    fun addSet(name: String, description: String): Long
    fun getSets(setGroupConfig: SetGroupConfig): List<SetDomainModel>
    fun removeById(id: Long)
}
