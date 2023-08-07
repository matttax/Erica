package com.matttax.erica.domain.usecases.translate

import com.matttax.erica.domain.model.translate.TranslationRequest
import com.matttax.erica.domain.model.translate.UsageExample
import com.matttax.erica.domain.repositories.Translator

class GetExamplesUseCase(
    private val translator: Translator
) : TranslateUseCase<List<UsageExample>>() {

    override fun executeInBackground(input: TranslationRequest): List<UsageExample> {
        return translator.getExamples(input)
    }
}
