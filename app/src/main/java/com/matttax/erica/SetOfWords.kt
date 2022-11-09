package com.matttax.erica

import android.content.Context
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.ImageView
import java.util.*
import kotlin.math.min

class SetOfWords(val id: Int, val name: String, val description: String, val wordsCount: Int)

class WordGroup(var words: MutableList<QuizWord>, private val batchSize: Int) {
    var nextBatchStart = 0

    init {
        words.shuffle()
    }

    fun updateBatches(incorrectWords: MutableList<QuizWord>) {
        words = words.drop(nextBatchStart) as MutableList<QuizWord>
        nextBatchStart = min(batchSize, words.size)
        words.addAll(incorrectWords)
        words.shuffle()
    }

    fun completed() = nextBatchStart == words.size

    fun getNextBatch(incorrectWords:MutableList<QuizWord>): List<QuizWord> {
        updateBatches(incorrectWords)
        return words.slice(0 until nextBatchStart)
    }

}

class Word(val term: String?, val definition: String)

class LanguagePair(private val termLanguage: String, private val definitionLanguage: String) {
    fun getTermLocale(): Locale = getLocale(termLanguage)

    fun getDefinitionLocale(): Locale = getLocale(definitionLanguage)

    private fun getLocale(language: String) = when(language) {
        "ru" -> Locale("ru", "RU")
        else -> Locale.US
    }
}

class QuizWord(val id: Int,
               val langPair: LanguagePair,
               val word: Word,
               val timesAsked: Int,
               val timesCorrect: Int,
               val lastAsked: Date,
               val setId: Int) {

    private lateinit var termSpeech: TextToSpeech
    private lateinit var definitionSpeech: TextToSpeech

    fun getCorrectPercentage() = timesCorrect.toDouble() / timesAsked

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