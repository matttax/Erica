package com.matttax.erica.domain.model.translate

data class DictionaryDefinition(
    val partOfSpeech: String,
    val description: String,
    val definitions: List<String>
)
