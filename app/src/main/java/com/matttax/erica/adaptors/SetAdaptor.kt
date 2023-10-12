package com.matttax.erica.adaptors

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.matttax.erica.R
import com.matttax.erica.adaptors.callback.SetCallback
import com.matttax.erica.model.WordSet

class SetAdaptor(
    private val context: Context,
    private val callback: SetCallback
) : ListAdapter<WordSet, SetAdaptor.SetViewHolder>(SetDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        return SetViewHolder(
            LayoutInflater
                .from(context)
                .inflate(R.layout.set_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.learnButton.isVisible = context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        holder.setName.text = getItem(position).name
        holder.wordsCount.text = getItem(position).wordsCount.toString()
        holder.setLayout.setOnClickListener {
            callback.onClick(position)
        }
        holder.learnButton.setOnClickListener {
            callback.onLearnClick(position)
        }
        holder.deleteButton.setOnClickListener {
            callback.onDeleteClick(position)
        }
        holder.editButton.setOnClickListener {
            callback.onEditClick(position)
        }
    }

    class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val setName: TextView = itemView.findViewById(R.id.setName)
        val wordsCount: TextView = itemView.findViewById(R.id.setWordsCount)
        val setLayout: LinearLayout = itemView.findViewById(R.id.setItem)
        val learnButton: MaterialButton = itemView.findViewById(R.id.learnSet)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteSet)
        val editButton: ImageButton = itemView.findViewById(R.id.editSet)
    }

    object SetDiffCallback : DiffUtil.ItemCallback<WordSet>() {
        override fun areItemsTheSame(oldItem: WordSet, newItem: WordSet): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WordSet, newItem: WordSet): Boolean {
            return oldItem == newItem
        }

    }

}

