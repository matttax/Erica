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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.matttax.erica.R
import com.matttax.erica.model.WordSet

class SetAdaptor(
    private val context: Context,
    private val sets: List<WordSet>,
    private val onClick: (Int) -> Unit = {},
    private val onLearnClick: (Int) -> Unit = {},
    private val onEditClick: (Int) -> Unit = {},
    private val onDeleteClick: (Int) -> Unit = {}
) : RecyclerView.Adapter<SetAdaptor.SetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.set_item, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.learnButton.isVisible = context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        holder.setName.text = sets[position].name
        holder.wordsCount.text = sets[position].wordsCount.toString()
        holder.setLayout.setOnClickListener {
            onClick(position)
        }
        holder.learnButton.setOnClickListener {
            onLearnClick(position)
        }
        holder.deleteButton.setOnClickListener {
            onDeleteClick(position)
        }
        holder.editButton.setOnClickListener {
            onEditClick(position)
        }
    }

    override fun getItemCount(): Int = sets.size

    class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val setName: TextView = itemView.findViewById(R.id.setName)
        val wordsCount: TextView = itemView.findViewById(R.id.setWordsCount)
        val setLayout: LinearLayout = itemView.findViewById(R.id.setItem)
        val learnButton: MaterialButton = itemView.findViewById(R.id.learnSet)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteSet)
        val editButton: ImageButton = itemView.findViewById(R.id.editSet)
    }
}
