package com.matttax.erica.presentation.model.translate

import com.matttax.erica.presentation.states.DataState

data class TranslateState(
    val translations: DataState? = null,
    val definitions: DataState? = null,
    val examples: DataState? = null,
)
