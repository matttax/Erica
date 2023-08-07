package com.matttax.erica.presentation.states

import com.matttax.erica.domain.model.Language

data class TranslateState(
    val languageIn: Language? = null,
    val languageOut: Language? = null,
    val textIn: String? = null,
    val textOut: String? = null,
    val translations: DataState? = null,
    val definitions: DataState? = null,
    val examples: DataState? = null
)
