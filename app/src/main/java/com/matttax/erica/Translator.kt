package com.matttax.erica

import android.content.Context
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.matttax.erica.adaptors.PartOfSpeechAdaptor
import com.matttax.erica.adaptors.TRANSLATION
import com.matttax.erica.adaptors.TranslationAdaptor
import com.matttax.erica.adaptors.WordAdaptor

class Translator(val context: Context, var word: String, private val languagePair: LanguagePair) {
    var translations: List<String> = emptyList()
    var examples: List<StudyCard> = emptyList()
    var definitions: List<Definitions> = emptyList()

    init {
        translations = loadTranslations()
        definitions = loadDefinitions()
        examples = loadExamples()
    }

    fun getAdaptorAtPosition(currentPosition: Int) = when (currentPosition) {
        1 -> PartOfSpeechAdaptor(context, definitions)
        2 -> WordAdaptor(context, examples, ContextCompat.getColor(context, R.color.blue), Int.MAX_VALUE-1)
        else -> TranslationAdaptor(context, translations, TRANSLATION.WORD)
    }

//    fun getTranslation() = translations
//    fun getExamples() = examples
//    fun getDefinitions() = definitions

    private fun loadTranslations(): List<String> {
        if (!Python.isStarted())
            Python.start(AndroidPlatform(context))
        val p = Python.getInstance()
        val f = p.getModule("Translate")
        return f.callAttr("getTranslations", word, languagePair.termLanguage, languagePair.definitionLanguage).asList()
            .map { it.toString() }
    }

    private fun loadExamples(): List<StudyCard> {
        if (!Python.isStarted())
            Python.start(AndroidPlatform(context))
        val p = Python.getInstance()
        val f = p.getModule("Translate")
        val examplesAsWords = f.callAttr("getExamples", word, languagePair.termLanguage, languagePair.definitionLanguage).asMap()
            .map { StudyItem(it.key.toString(), it.value.toString().trimEnd()) }
        val exampleCards = mutableListOf<StudyCard>()
        for(example in examplesAsWords)
            exampleCards += StudyCard(Int.MAX_VALUE, languagePair, example, Int.MAX_VALUE)
        return exampleCards
    }

    private fun loadDefinitions(): List<Definitions> {
        if (languagePair.termLanguage == "de") {
            if (word.lowercase().matches("(der |die |das ).*".toRegex()))
                word = word.substring(4)
        }
        if (!Python.isStarted())
            Python.start(AndroidPlatform(context))
        val p = Python.getInstance()
        val f = p.getModule("Translate")
        return f.callAttr("getDefinitions", word, languagePair.getTermFullName()).asMap()
                .map { it ->
                Definitions(it.key.toString(), it.value.asList()[0].toString(),
                it.value.asList().subList(1, it.value.asList().size).map { it.toString() }) }
    }
}