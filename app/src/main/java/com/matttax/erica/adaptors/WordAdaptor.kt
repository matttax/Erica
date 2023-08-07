package com.matttax.erica.adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.matttax.erica.R
import com.matttax.erica.activities.WordsActivity
import com.matttax.erica.dialogs.DeleteWordDialog
import com.matttax.erica.domain.model.translate.UsageExample
import com.matttax.erica.presentation.model.translate.TextCardState
import com.matttax.erica.presentation.model.translate.TranslatedText
import com.matttax.erica.presentation.model.translate.TranslatedTextCard

class WordAdaptor constructor(
    private val context: Context,
    private val words: List<TranslatedTextCard>,
    private val onClick: (TranslatedTextCard) -> Unit = {},
    private val onDelete: () -> Unit = {},
    private val onSpell: (TranslatedText) -> Unit = {},
) : RecyclerView.Adapter<WordAdaptor.WordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        return WordViewHolder(
            itemView = LayoutInflater.from(context).inflate(
                R.layout.term_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = words.size

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.term.text = words[position].translatedText.text
        holder.definition.text = words[position].translatedText.translation
        holder.item.strokeColor = when(words[position].state) {
            TextCardState.CORRECT -> ContextCompat.getColor(context, R.color.green)
            TextCardState.INCORRECT -> ContextCompat.getColor(context, R.color.crimson)
            TextCardState.DEFAULT -> ContextCompat.getColor(context, R.color.blue)
        }
        if (!words[position].isEditable) {
            holder.cardBackground.removeView(holder.edit)
            holder.cardBackground.removeView(holder.delete)
        } else {
            holder.itemView.setOnClickListener {
                onClick(words[holder.adapterPosition])
            }
        }

        holder.play.setOnClickListener {
            onSpell(words[holder.adapterPosition].translatedText)
        }
        holder.delete.setOnClickListener {
            onDelete()
        }
    }

    class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val term: TextView = itemView.findViewById(R.id.wordTerm)
        val definition: TextView = itemView.findViewById(R.id.wordDef)
        val play: ImageView = itemView.findViewById(R.id.playSound)
        val delete: ImageView = itemView.findViewById(R.id.deleteWord)
        val edit: ImageView = itemView.findViewById(R.id.editWord)
        val item: MaterialCardView = itemView.findViewById(R.id.item)
        val cardBackground: LinearLayout = itemView.findViewById(R.id.cardBackground)
    }
}