package com.matttax.erica.domain.model.translate

import com.matttax.erica.domain.model.Language

data class TranslationRequest(
    val text: String,
    val fromLanguage: Language,
    val toLanguage: Language
)
