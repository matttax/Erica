package com.matttax.erica.adapters

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.adapters.callback.SetCallback
import com.matttax.erica.databinding.SetItemBinding
import com.matttax.erica.model.WordSet

class SetAdapter(
    private val callback: SetCallback
) : ListAdapter<WordSet, SetAdapter.SetViewHolder>(SetDiffCallback) {

    private lateinit var binding: SetItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        binding = SetItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.onBind()
    }

    private object SetDiffCallback : DiffUtil.ItemCallback<WordSet>() {
        override fun areItemsTheSame(oldItem: WordSet, newItem: WordSet): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: WordSet, newItem: WordSet): Boolean {
            return oldItem == newItem
        }
    }

    inner class SetViewHolder(
        private val binding: SetItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind() = with(binding) {
            learnSet.isVisible =
                binding.root.context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
            setName.text = getItem(layoutPosition).name
            setWordsCount.text = getItem(layoutPosition).wordsCount.toString()
            setItem.setOnClickListener { callback.onClick(layoutPosition) }
            learnSet.setOnClickListener { callback.onLearnClick(layoutPosition) }
            deleteSet.setOnClickListener { callback.onDeleteClick(layoutPosition) }
            editSet.setOnClickListener { callback.onEditClick(layoutPosition) }
        }
    }

}
