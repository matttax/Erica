package com.matttax.erica.domain.config

data class WordGroupConfig(
    val setId: SetId = SetId.None,
    val sorting: WordsSorting = WordsSorting.LAST_ADDED_FIRST,
    val limit: Int? = null,
)