package com.matttax.erica.presentation.states

import com.matttax.erica.domain.model.translate.DictionaryDefinition

sealed class HintState {
    data class Hint(val definitions: List<DictionaryDefinition>): HintState()
    object NotRequested: HintState()
    object NotFound: HintState()
    object Loading: HintState()
}
