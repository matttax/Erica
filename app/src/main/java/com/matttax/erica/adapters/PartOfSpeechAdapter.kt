package com.matttax.erica.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.databinding.DefinitionItemBinding
import com.matttax.erica.domain.model.translate.DictionaryDefinition

class PartOfSpeechAdapter(
    private val definitions: List<DictionaryDefinition>
) : RecyclerView.Adapter<PartOfSpeechAdapter.PartOfSpeechViewHolder>() {

    private lateinit var binding: DefinitionItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartOfSpeechViewHolder {
        binding = DefinitionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PartOfSpeechViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartOfSpeechViewHolder, position: Int) {
        holder.onBind(definitions[position])
    }

    override fun getItemCount() = definitions.size

    inner class PartOfSpeechViewHolder(
        private val binding: DefinitionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(dictionaryDefinition: DictionaryDefinition) {
            binding.apply {
                partOfSpeech.text = dictionaryDefinition.partOfSpeech
                description.text = dictionaryDefinition.description
                definitions.layoutManager = LinearLayoutManager(root.context)
                definitions.adapter = TranslationAdapter(dictionaryDefinition.definitions)
            }
        }
    }
}