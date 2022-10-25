package com.matttax.erica

import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class WordAdaptor(var context: Context, var words: List<QuizWord>) :
    RecyclerView.Adapter<WordAdaptor.WordViewHolder>() {

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var term: TextView
        var definition: TextView
        var play: ImageView
        lateinit var tts: TextToSpeech
        lateinit var ttst: TextToSpeech

        init {
            term = itemView.findViewById(R.id.wordTerm)
            definition = itemView.findViewById(R.id.wordDef)
            play = itemView.findViewById(R.id.playSound)
            play.setOnClickListener {
                tts = TextToSpeech(context) {
                    if (it == TextToSpeech.SUCCESS) {
                        play.setColorFilter(Color.argb(255, 255, 165, 0))
                        tts.language = Locale.US
                        tts.speak(term.text, TextToSpeech.QUEUE_FLUSH, null,"")
                    }
                }
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String) {}

                    override fun onDone(utteranceId: String) {
                        ttst = TextToSpeech(context) {
                            if (it == TextToSpeech.SUCCESS) {
                                ttst.language = Locale("ru", "RU")
                                ttst.speak(definition.text, TextToSpeech.QUEUE_FLUSH, null, "")
                            }
                        }
                        ttst.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(p0: String?) {}

                            override fun onDone(p0: String?) {
                                play.setColorFilter(Color.argb(255, 41, 45, 54))
                            }

                            override fun onError(p0: String?) {}

                        })
                    }

                    override fun onError(utteranceId: String) {}
                })

                // word method
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val inflator = LayoutInflater.from(context)
        val view = inflator.inflate(R.layout.term_item, parent, false)
        return WordViewHolder(view)
    }

    override fun getItemCount(): Int = words.size

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.term.text = words[position].word.term
        holder.definition.text = words[position].word.definition
    }
}