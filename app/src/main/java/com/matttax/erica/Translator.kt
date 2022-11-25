package com.matttax.erica

import android.content.Context
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.matttax.erica.adaptors.PartOfSpeechAdaptor
import com.matttax.erica.adaptors.TranslationAdaptor
import com.matttax.erica.adaptors.WordAdaptor

class Translator(val context: Context, val word: String, private val languagePair: LanguagePair, private val position: Int) {
    private var translations: List<String> = emptyList()
    private var examples: List<QuizWord> = emptyList()
    private var definitions: List<Definitions> = emptyList()

    init {
        translations = loadTranslations()
        definitions = loadDefinitions()
        examples = loadExamples()
    }

    fun getAdaptorAtPosition(currentPosition: Int) = when (currentPosition) {
        1 -> PartOfSpeechAdaptor(context, definitions)
        2 -> WordAdaptor(context, examples, ContextCompat.getColor(context, R.color.blue), Int.MAX_VALUE-1)
        else -> TranslationAdaptor(context, translations, "en")
    }

    fun getLoadedAdaptor() = getAdaptorAtPosition(position)

    private fun loadTranslations(): List<String> {
        if (!Python.isStarted())
            Python.start(AndroidPlatform(context))
        val p = Python.getInstance()
        val f = p.getModule("Translate")
        return f.callAttr("getTranslations", word, languagePair.termLanguage, languagePair.definitionLanguage).asList()
            .map { it.toString() }
    }

    private fun loadExamples(): List<QuizWord> {
        if (!Python.isStarted())
            Python.start(AndroidPlatform(context))
        val p = Python.getInstance()
        val f = p.getModule("Translate")
        val examplesAsWords = f.callAttr("getExamples", word, languagePair.termLanguage, languagePair.definitionLanguage).asMap()
            .map { Word(it.key.toString(), it.value.toString().trimEnd()) }
        val exampleCards = mutableListOf<QuizWord>()
        for(example in examplesAsWords)
            exampleCards += QuizWord(Int.MAX_VALUE, languagePair, example, Int.MAX_VALUE)
        return exampleCards
    }

    private fun loadDefinitions(): List<Definitions> {
        if (!Python.isStarted())
            Python.start(AndroidPlatform(context))
        val p = Python.getInstance()
        val f = p.getModule("Translate")
        return f.callAttr("getDefinitions", word, languagePair.getTermFullName()).asMap()
                .map { it ->
                Definitions(it.key.toString(), it.value.asList()[0].toString(),
                it.value.asList().subList(1, it.value.asList().size-1).map { it.toString() }) }
    }
}