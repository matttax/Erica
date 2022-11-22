package com.matttax.erica.adaptors

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.R
import com.matttax.erica.WordSet


class TranslationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text: TextView = itemView.findViewById(R.id.translation)
}


class TranslationAdaptor(var context: Context, var translations: List<String>, var language: String) :
    RecyclerView.Adapter<TranslationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
        val inflator = LayoutInflater.from(context)
        val view = inflator.inflate(R.layout.translation_item, parent, false)
        return TranslationViewHolder(view)
    }

    override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
        holder.text.text = translations[position]
    }

    override fun getItemCount(): Int = translations.size

}
