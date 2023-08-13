package com.matttax.erica

import android.content.Context
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.ImageView
import java.util.*
import kotlin.math.min

data class WordSet(
    val id: Int,
    val name: String,
    val description: String,
    val wordsCount: Int,
) {
    override fun toString(): String {
        return name
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
        "fr" -> "french"
        "es" -> "spanish"
        "it" -> "italian"
        else -> "english"
    }

    private fun getLocale(language: String) = when(language) {
        "ru" -> Locale("ru", "")
        "de" -> Locale.GERMAN
        "fr" -> Locale.FRENCH
        "es" -> Locale("es", "")
        "it" -> Locale.ITALIAN
        else -> Locale.US
    }
}

class StudyCard(val id: Int, val langPair: LanguagePair, val word: StudyItem, val setId: Int) {

    private lateinit var termSpeech: TextToSpeech
    private lateinit var definitionSpeech: TextToSpeech

    override fun toString(): String {
        return "${word.word} ${word.translation}"
    }

    fun spell(context: Context, play:ImageView?=null) {
        spellTerm(context, play)
        termSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {}
            override fun onError(utteranceId: String) {}
            override fun onDone(utteranceId: String) { spellDefinition(context, play) }
        })
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
}
