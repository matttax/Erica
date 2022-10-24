package com.matttax.erica

import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class WordAdaptor(var context: Context, var words: List<QuizWord>) :
    RecyclerView.Adapter<WordAdaptor.WordViewHolder>() {

    class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var term: TextView
        var definition: TextView
        var play: ImageView

        init {
            term = itemView.findViewById(R.id.wordTerm)
            definition = itemView.findViewById(R.id.wordDef)
            play = itemView.findViewById(R.id.playSound)
            play.setOnClickListener {
                play.setColorFilter(Color.argb(255, 255, 165, 0))
                val timer = object: CountDownTimer(1000, 1000) {
                    override fun onTick(p0: Long) {}

                    override fun onFinish() {
                        play.setColorFilter(Color.argb(255, 41, 45, 54))
                    }
                }
                timer.start()
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