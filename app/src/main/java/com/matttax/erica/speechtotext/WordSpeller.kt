package com.matttax.erica.speechtotext

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.matttax.erica.presentation.model.translate.TranslatedText
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

class WordSpeller @Inject constructor(
    @ApplicationContext val context: Context
) {

    private lateinit var termSpeech: TextToSpeech
    private lateinit var definitionSpeech: TextToSpeech

    fun spell(translatedText: TranslatedText, onDone: () -> Unit = {}) {
        termSpeech = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                termSpeech.language = translatedText.textLanguage.locale
                termSpeech.speak(translatedText.text, TextToSpeech.QUEUE_FLUSH, null,"")
            }
        }
        termSpeech.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) = Unit
                override fun onError(utteranceId: String) = Unit
                override fun onDone(utteranceId: String) = spellText(
                    text = translatedText.translation,
                    locale = translatedText.translationLanguage.locale,
                    onDone = onDone
                )
            }
        )
    }

    fun spellText(text: String, locale: Locale, onDone: () -> Unit = {}) {
        definitionSpeech = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                definitionSpeech.language = locale
                definitionSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
                definitionSpeech.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(p0: String?) {}
                        override fun onError(p0: String?) {}
                        override fun onDone(p0: String?) = onDone()
                    }
                )
            }
        }
    }
}
