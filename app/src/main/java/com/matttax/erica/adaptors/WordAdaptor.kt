package com.matttax.erica.adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.matttax.erica.QuizWord
import com.matttax.erica.R
import com.matttax.erica.dialogs.DeleteWordDialog

class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var term: TextView
    var definition: TextView
    var play: ImageView
    var delete: ImageView
    var edit: ImageView
    var item: MaterialCardView

    init {
        item = itemView.findViewById(R.id.item)
        term = itemView.findViewById(R.id.wordTerm)
        delete = itemView.findViewById(R.id.deleteWord)
        edit = itemView.findViewById(R.id.editWord)
        definition = itemView.findViewById(R.id.wordDef)
        play = itemView.findViewById(R.id.playSound)
    }

}

class WordAdaptor(var context: Context, var words: List<QuizWord>, var color: Int, var lastIncorrect:Int=Int.MAX_VALUE) :
    RecyclerView.Adapter<WordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val inflator = LayoutInflater.from(context)
        val view = inflator.inflate(R.layout.term_item, parent, false)
        return WordViewHolder(view)
    }

    override fun getItemCount(): Int = words.size

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.term.text = words[position].word.term
        holder.definition.text = words[position].word.definition
        holder.delete.setOnClickListener {
            DeleteWordDialog(context, R.layout.delete_word,
                words[position].setId, words[position].id).showDialog()
        }
        holder.play.setOnClickListener {
            words[position].spell(context, holder.play)
        }
        holder.item.strokeColor = if (position < lastIncorrect) color else ContextCompat.getColor(context, R.color.green)
        if (position != Int.MAX_VALUE) {
            holder.edit.isInvisible = true
            holder.delete.isInvisible = true
        }
    }
}