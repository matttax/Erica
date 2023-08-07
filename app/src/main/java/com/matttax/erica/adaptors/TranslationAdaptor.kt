package com.matttax.erica.adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.R

class TranslationAdaptor constructor(
    private var context: Context,
    private var translations: List<String>,
    private val onTextClick: (CharSequence) -> Unit = {},
) : RecyclerView.Adapter<TranslationAdaptor.TranslationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
        return TranslationViewHolder(
            itemView = LayoutInflater.from(context).inflate(
                R.layout.translation_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
        holder.textView.text = translations[position]
        holder.background.setOnClickListener {
            onTextClick(translations[position])
        }
    }

    override fun getItemCount(): Int = translations.size

    data class TranslationViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.translation)
        val background: LinearLayout = itemView.findViewById(R.id.translationBackground)
    }

}
