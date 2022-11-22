package com.matttax.erica

import android.content.Context
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.ImageView
import java.util.*
import kotlin.math.min

class WordSet(val id: Int, val name: String, val description: String, val wordsCount: Int)

class WordGroup(var words: MutableList<QuizWord>, private val batchSize: Int) {
    var nextBatchStart = 0

    init {
        words.shuffle()
    }

    fun getNextBatch(incorrectWords:MutableList<QuizWord>): Stack<QuizWord> {
        words = ArrayList(words.drop(nextBatchStart))
        words.addAll(incorrectWords)
        words.shuffle()
        nextBatchStart = min(batchSize, words.size)
        val batch = Stack<QuizWord>()
        for (i in 0 until nextBatchStart) {
            batch.push(words[i])
        }
        return batch
    }

}

class Word(val term: String?, val definition: String)

class LanguagePair(val termLanguage: String, val definitionLanguage: String) {

    fun getTermLocale(): Locale = getLocale(termLanguage)

    fun getDefinitionLocale(): Locale = getLocale(definitionLanguage)

    fun getTermFullName() = getFullName(termLanguage)

    fun getDefinitionFullName() = getFullName(definitionLanguage)

    private fun getFullName(language: String) = when(language) {
        "ru" -> "russian"
        else -> "english"
    }

    private fun getLocale(language: String) = when(language) {
        "ru" -> Locale("ru", "RU")
        else -> Locale.US
    }
}

class QuizWord(val id: Int,
               val langPair: LanguagePair,
               val word: Word,
               val setId: Int) {

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
        return "${word.term} ${word.definition}"
    }

    fun spellTerm(context: Context, play:ImageView?=null) {
        termSpeech = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                play?.setColorFilter(Color.argb(255, 255, 165, 0))
                termSpeech.language = langPair.getTermLocale()
                termSpeech.speak(word.term, TextToSpeech.QUEUE_FLUSH, null,"")
            }
        }
    }

    fun spellDefinition(context: Context, play:ImageView?=null) {
        definitionSpeech = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                definitionSpeech.language = langPair.getDefinitionLocale()
                definitionSpeech.speak(word.definition, TextToSpeech.QUEUE_FLUSH, null, "")
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
}