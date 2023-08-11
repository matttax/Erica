package com.matttax.erica.domain.config

data class SetGroupConfig(
    val sorting: SetSorting,
    val limit: Int = Int.MAX_VALUE
)
