package com.matttax.erica.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.R
import com.matttax.erica.adapters.callback.WordCallback
import com.matttax.erica.databinding.WordCardBinding
import com.matttax.erica.presentation.model.translate.TextCardState
import com.matttax.erica.presentation.model.translate.TranslatedTextCard
import kotlin.properties.Delegates

class WordAdapter(
    private val words: List<TranslatedTextCard>,
    private val callback: WordCallback
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    private lateinit var binding: WordCardBinding

    private var correctColor by Delegates.notNull<Int>()
    private var incorrectColor by Delegates.notNull<Int>()
    private var defaultColor by Delegates.notNull<Int>()

    private var selectedColor by Delegates.notNull<Int>()
    private var deselectedColor by Delegates.notNull<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        with(parent.context) {
            correctColor = ContextCompat.getColor(this, R.color.green)
            incorrectColor = ContextCompat.getColor(this, R.color.crimson)
            defaultColor = ContextCompat.getColor(this, R.color.blue)
            selectedColor = ContextCompat.getColor(this, R.color.light_blue)
            deselectedColor = ContextCompat.getColor(this, R.color.white)
        }
        binding = WordCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordViewHolder(binding)
    }

    override fun getItemCount(): Int = words.size

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.onBind(words[position])
    }

    inner class WordViewHolder(
        private val binding: WordCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(wordCard: TranslatedTextCard) {
            binding.wordTerm.text = wordCard.translatedText.text
            binding.wordDef.text = wordCard.translatedText.translation
            binding.item.strokeColor = when(wordCard.state) {
                TextCardState.CORRECT -> correctColor
                TextCardState.INCORRECT -> incorrectColor
                TextCardState.DEFAULT -> defaultColor
            }
            binding.root.setOnClickListener {
                callback.onClick(layoutPosition)
            }
            if (!wordCard.isEditable) {
                binding.editWord.isVisible = false
                binding.deleteWord.isVisible = false
            } else {
                binding.editWord.isVisible = true
                binding.deleteWord.isVisible = true
            }
            if (wordCard.isSelected) {
                binding.cardBackground.setBackgroundColor(selectedColor)
            } else {
                binding.cardBackground.setBackgroundColor(deselectedColor)
            }

            binding.playSound.setOnClickListener {
                callback.onSpell(binding.playSound, wordCard.translatedText)
            }
            binding.deleteWord.setOnClickListener {
                callback.onDeleteClick(layoutPosition)
            }
            binding.editWord.setOnClickListener {
                callback.onEditClick(layoutPosition)
            }
        }
    }
}
