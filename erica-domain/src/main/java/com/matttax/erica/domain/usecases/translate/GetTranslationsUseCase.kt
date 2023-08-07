package com.matttax.erica.domain.usecases.translate

import com.matttax.erica.domain.model.translate.TranslationRequest
import com.matttax.erica.domain.repositories.Translator

class GetTranslationsUseCase(
    private val translator: Translator
) : TranslateUseCase<List<String>>() {

    override fun executeInBackground(input: TranslationRequest): List<String> {
        return translator.getTranslations(input)
    }
}
