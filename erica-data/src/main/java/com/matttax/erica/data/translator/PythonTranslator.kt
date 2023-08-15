package com.matttax.erica.data.translator

import android.content.Context
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.matttax.erica.domain.model.Language
import com.matttax.erica.domain.model.translate.DictionaryDefinition
import com.matttax.erica.domain.model.translate.TranslationRequest
import com.matttax.erica.domain.model.translate.UsageExample
import com.matttax.erica.domain.repositories.Translator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PythonTranslator @Inject constructor(
    @ApplicationContext context: Context
): Translator {

    private val python: Python
    private val translateModule: PyObject

    init {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
        python = Python.getInstance()
        translateModule = python.getModule(TRANSLATE_MODULE_NAME)
    }

    override fun getTranslations(request: TranslationRequest): List<String> {
        return translateModule.callAttr(
            GET_TRANSLATIONS_FUNCTION_NAME,
            request.text,
            request.fromLanguage.code,
            request.toLanguage.code
        ).asList().map { it.toString() }
    }

    override fun getDefinitions(request: TranslationRequest): List<DictionaryDefinition> {
        val preparedText =
            if (request.fromLanguage == Language.GERMAN &&
                request.text.lowercase().matches("(der |die |das ).*".toRegex())
            ) {
                request.text.substring(4)
            } else request.text

        return translateModule.callAttr(
            GET_DEFINITIONS_FUNCTION_NAME,
            preparedText,
            request.fromLanguage.fullName
        ).asMap().map { definition ->
            DictionaryDefinition(
                partOfSpeech = definition.key.toString(),
                description = definition.value.asList()[0].toString(),
                definitions = definition.value.asList().drop(1).map { it.toString() })
        }
    }

    override fun getExamples(request: TranslationRequest): List<UsageExample> {
        return translateModule.callAttr(
            GET_EXAMPLES_FUNCTION_NAME,
            request.text,
            request.fromLanguage.code,
            request.toLanguage.code
        ).asMap().map {
            UsageExample(
                it.key.toString(),
                it.value.toString().trimEnd(),
                request.fromLanguage,
                request.toLanguage
            )
        }
    }

    companion object {
        const val TRANSLATE_MODULE_NAME = "Translate"
        const val GET_TRANSLATIONS_FUNCTION_NAME = "getTranslations"
        const val GET_EXAMPLES_FUNCTION_NAME = "getExamples"
        const val GET_DEFINITIONS_FUNCTION_NAME = "getDefinitions"
    }
}