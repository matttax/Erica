package com.matttax.erica.adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.Definitions
import com.matttax.erica.R

class PartOfSpeechViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val partOfSpeech: TextView = itemView.findViewById(R.id.partOdSpeech)
    val description: TextView = itemView.findViewById(R.id.description)
    val definitionsRV: RecyclerView = itemView.findViewById(R.id.definitions)
}

class PartOfSpeechAdaptor(var context: Context, private var definitions: List<Definitions>) : RecyclerView.Adapter<PartOfSpeechViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartOfSpeechViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.definition_item, parent, false)
        return PartOfSpeechViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartOfSpeechViewHolder, position: Int) {
        holder.partOfSpeech.text = definitions[position].partOfSpeech
        holder.description.text = definitions[position].description

        holder.definitionsRV.adapter = TranslationAdaptor(context, definitions[position].definition, "en", TRANSLATION.DEFINITION)
        holder.definitionsRV.layoutManager = LinearLayoutManager(context)
    }

    override fun getItemCount() = definitions.size

}