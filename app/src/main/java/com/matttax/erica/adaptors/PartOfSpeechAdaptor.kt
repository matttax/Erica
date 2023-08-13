package com.matttax.erica.adaptors

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.R
import com.matttax.erica.domain.model.translate.DictionaryDefinition

class PartOfSpeechAdaptor(
    private val context: Activity,
    private val definitions: List<DictionaryDefinition>
) : RecyclerView.Adapter<PartOfSpeechAdaptor.PartOfSpeechViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartOfSpeechViewHolder {
        val inflater = context.layoutInflater
        val view = inflater.inflate(R.layout.definition_item, parent, false)
        return PartOfSpeechViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartOfSpeechViewHolder, position: Int) {
        holder.partOfSpeech.text = definitions[position].partOfSpeech
        holder.description.text = definitions[position].description

        holder.definitionsRV.adapter = TranslationAdaptor(context, definitions[position].definitions)
        holder.definitionsRV.layoutManager = LinearLayoutManager(context)
    }

    override fun getItemCount() = definitions.size

    class PartOfSpeechViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val partOfSpeech: TextView = itemView.findViewById(R.id.partOdSpeech)
        val description: TextView = itemView.findViewById(R.id.description)
        val definitionsRV: RecyclerView = itemView.findViewById(R.id.definitions)
    }
}