package com.matttax.erica.domain.repositories

import com.matttax.erica.domain.model.translate.DictionaryDefinition
import com.matttax.erica.domain.model.translate.TranslationRequest
import com.matttax.erica.domain.model.translate.UsageExample

interface Translator {
    fun getTranslations(request: TranslationRequest): List<String>
    fun getDefinitions(request: TranslationRequest): List<DictionaryDefinition>
    fun getExamples(request: TranslationRequest): List<UsageExample>
}
