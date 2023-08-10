package com.matttax.erica.domain.model

data class WordDomainModel (
    val id: Long? = null,
    val text: String,
    val translation: String,
    val textLanguage: Language,
    val translationLanguage: Language,
    val setId: Long,
    val askedCount: Int = 0,
    val answeredCount: Int = 0,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis(),
    val lastAskedTimestamp: Long? = null,
)
