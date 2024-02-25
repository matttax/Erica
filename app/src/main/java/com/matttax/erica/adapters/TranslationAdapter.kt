package com.matttax.erica.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.databinding.TranslationItemBinding

class TranslationAdapter constructor(
    private var translations: List<String>,
    private val onTextClick: (CharSequence) -> Unit = {},
) : RecyclerView.Adapter<TranslationAdapter.TranslationViewHolder>() {

    private lateinit var binding: TranslationItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
        binding = TranslationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TranslationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int = translations.size

    inner class TranslationViewHolder(
        private val binding: TranslationItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind() {
            binding.translation.text = translations[layoutPosition]
            binding.translationBackground.setOnClickListener {
                onTextClick(translations[layoutPosition])
            }
        }
    }

}
