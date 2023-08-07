package com.matttax.erica.presentation.model.translate

import com.matttax.erica.domain.model.Language

data class TranslatedText(
    val text: String,
    val translation: String,
    val textLanguage: Language,
    val translationLanguage: Language
)