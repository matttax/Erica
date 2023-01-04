package com.matttax.erica

import android.content.Context
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.ImageView
import java.util.*
import kotlin.math.min

class WordSet(val id: Int, val name: String, val description: String, val wordsCount: Int) {
    override fun toString(): String {
        return name
    }
}

class WordGroup(var words: MutableList<StudyCard>, private val batchSize: Int, ask: String) {
    var nextBatchStart = 0

    init {
        words.shuffle()
        val w = mutableListOf<StudyCard>()
        if (ask != "Word")
            words.forEach { w.add(it.getInvert()) }
        if (ask == "Translation")
            words = w
        else if (ask == "Both")
            words.addAll(w)
    }

    fun getNextBatch(incorrectWords:MutableList<StudyCard>): Stack<StudyCard> {
        words = ArrayList(words.drop(nextBatchStart))
        words.addAll(incorrectWords)
        words.shuffle()
        nextBatchStart = min(batchSize, words.size)
        val batch = Stack<StudyCard>()
        for (i in 0 until nextBatchStart) {
            batch.push(words[i])
        }
        return batch
    }

}

class StudyItem(val word: String?, val translation: String)

class LanguagePair(val termLanguage: String, val definitionLanguage: String) {

    fun getTermLocale(): Locale = getLocale(termLanguage)

    fun getDefinitionLocale(): Locale = getLocale(definitionLanguage)

    fun getTermFullName() = getFullName(termLanguage)

    fun getDefinitionFullName() = getFullName(definitionLanguage)

    private fun getFullName(language: String) = when(language) {
        "ru" -> "russian"
        "de" -> "german"
        "pl" -> "polish"
        "tr" -> "turkish"
        else -> "english"
    }

    private fun getLocale(language: String) = when(language) {
        "ru" -> Locale("ru", "RU")
        "de" -> Locale.GERMAN
        "pl" -> Locale("pl", "PL")
        "tr" -> Locale("tr", "TR")
        else -> Locale.US
    }
}

class StudyCard(val id: Int, private val langPair: LanguagePair, val word: StudyItem, val setId: Int) {

    private lateinit var termSpeech: TextToSpeech
    private lateinit var definitionSpeech: TextToSpeech

    fun spell(context: Context, play:ImageView?=null) {
        spellTerm(context, play)
        termSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {}
            override fun onError(utteranceId: String) {}
            override fun onDone(utteranceId: String) { spellDefinition(context, play) }
        })
    }

    override fun toString(): String {
        return "${word.word} ${word.translation}"
    }

    fun spellTerm(context: Context, play:ImageView?=null) {
        termSpeech = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                play?.setColorFilter(Color.argb(255, 255, 165, 0))
                termSpeech.language = langPair.getTermLocale()
                termSpeech.speak(word.word, TextToSpeech.QUEUE_FLUSH, null,"")
            }
        }
    }

    fun spellDefinition(context: Context, play:ImageView?=null) {
        definitionSpeech = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                definitionSpeech.language = langPair.getDefinitionLocale()
                definitionSpeech.speak(word.translation, TextToSpeech.QUEUE_FLUSH, null, "")
                definitionSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(p0: String?) {}
                    override fun onError(p0: String?) {}
                    override fun onDone(p0: String?) {
                        play?.setColorFilter(Color.argb(255, 41, 45, 54))
                    }
                })
            }
        }
    }

    fun getInvert() = StudyCard(id, LanguagePair(langPair.definitionLanguage, langPair.termLanguage),
                                StudyItem(word.translation, word.word?:""), setId)
}

class Definitions(val partOfSpeech: String, val description: String, val definition: List<String>)