package com.matttax.erica.adaptors

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.matttax.erica.QuizWord
import com.matttax.erica.R
import com.matttax.erica.activities.WordsActivity
import com.matttax.erica.dialogs.DeleteWordDialog

class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val term: TextView = itemView.findViewById(R.id.wordTerm)
    val definition: TextView = itemView.findViewById(R.id.wordDef)
    val play: ImageView = itemView.findViewById(R.id.playSound)
    val delete: ImageView = itemView.findViewById(R.id.deleteWord)
    val edit: ImageView = itemView.findViewById(R.id.editWord)
    val item: MaterialCardView = itemView.findViewById(R.id.item)
    val cardBackground: LinearLayout = itemView.findViewById(R.id.cardBackground)
}


class WordAdaptor(
    var context: Context, var words: List<QuizWord>, var color: Int,
    var lastIncorrect:Int=Int.MAX_VALUE, val whenClick: (() -> Unit)? = null) :
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
            DeleteWordDialog(context, R.layout.delete_word, words[position]).showDialog()
        }
        holder.play.setOnClickListener {
            words[position].spell(context, holder.play)
        }
        holder.item.strokeColor = if (position < lastIncorrect) color else ContextCompat.getColor(context, R.color.green)
        if (lastIncorrect != Int.MAX_VALUE) {
            holder.cardBackground.removeView(holder.edit)
            holder.cardBackground.removeView(holder.delete)
        } else {
            val currentWord = words[holder.adapterPosition]
            holder.itemView.setOnClickListener {
                if (words[position] in (context as WordsActivity).selected) {
                    (context as WordsActivity).selected.remove(words[position])
                } else (context as WordsActivity).selected.add(words[position])
                Log.i("select", (context as WordsActivity).selected.toString())
                whenClick?.let { it1 -> it1() }
                notifyDataSetChanged()
            }
            holder.cardBackground.setBackgroundColor(
                if (currentWord in (context as WordsActivity).selected)
                    ContextCompat.getColor(context, R.color.light_blue)
                else ContextCompat.getColor(context, R.color.white)
            )
        }
    }
}