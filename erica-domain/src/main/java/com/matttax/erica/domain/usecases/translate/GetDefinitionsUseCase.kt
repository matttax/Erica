package com.matttax.erica.domain.usecases.translate

import com.matttax.erica.domain.model.translate.DictionaryDefinition
import com.matttax.erica.domain.model.translate.TranslationRequest
import com.matttax.erica.domain.repositories.Translator

class GetDefinitionsUseCase(
    private val translator: Translator
) : TranslateUseCase<List<DictionaryDefinition>>() {

    override fun executeInBackground(input: TranslationRequest): List<DictionaryDefinition> {
        return translator.getDefinitions(input)
    }
}
