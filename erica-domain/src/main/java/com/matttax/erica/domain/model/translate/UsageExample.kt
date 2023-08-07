package com.matttax.erica.domain.model.translate

import com.matttax.erica.domain.model.Language

data class UsageExample(
    val text: String,
    val translation: String,
    val textLanguage: Language,
    val translationLanguage: Language
)