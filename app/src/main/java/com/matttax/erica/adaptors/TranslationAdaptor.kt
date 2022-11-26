package com.matttax.erica.adaptors

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.R
import com.matttax.erica.WordSet
import com.matttax.erica.activities.MainActivity


class TranslationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text: TextView = itemView.findViewById(R.id.translation)
    val back: LinearLayout = itemView.findViewById(R.id.translationBackground)
}


class TranslationAdaptor(var context: Context, var translations: List<String>, var language: String, private val translation: TRANSLATION) :
    RecyclerView.Adapter<TranslationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
        val inflator = LayoutInflater.from(context)
        val view = inflator.inflate(R.layout.translation_item, parent, false)
        return TranslationViewHolder(view)
    }

    override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
        holder.text.text = translations[position]

        if (translation == TRANSLATION.WORD)
        holder.back.setOnClickListener {
            (context as MainActivity).defTextField.setText(holder.text.text, TextView.BufferType.EDITABLE)
        }
    }

    override fun getItemCount(): Int = translations.size

}

enum class TRANSLATION {
    WORD,
    DEFINITION
}