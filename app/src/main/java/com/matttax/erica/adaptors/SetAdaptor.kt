package com.matttax.erica.adaptors

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.matttax.erica.R
import com.matttax.erica.SetOfWords
import com.matttax.erica.WordDBHelper
import com.matttax.erica.activities.LearnActivity
import com.matttax.erica.activities.WordsActivity
import com.matttax.erica.dialogs.DeleteSetDialog


class SetAdaptor(var context: Context, var sets: List<SetOfWords>) :
    RecyclerView.Adapter<SetAdaptor.SetViewHolder>() {

    class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var setName: TextView
        var wordsCount: TextView
        var setLayout: LinearLayout

        var learnButton: MaterialButton
        var deleteButton: ImageButton

        init {
            setName = itemView.findViewById(R.id.setName)
            wordsCount = itemView.findViewById(R.id.setWordsCount)
            setLayout = itemView.findViewById(R.id.setItem)

            learnButton = itemView.findViewById(R.id.learnSet)
            deleteButton = itemView.findViewById(R.id.deleteSet)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.set_item, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.setName.text = sets[position].name
        holder.wordsCount.text = sets[position].wordsCount.toString()
        holder.setLayout.setOnClickListener {
            val intent = Intent(context, WordsActivity::class.java)
            intent.putExtra("setid", sets[position].id)
            intent.putExtra("setname", sets[position].name)
            intent.putExtra("setdescr", sets[position].description)
            intent.putExtra("setwordcount", sets[position].wordsCount)
            context.startActivity(intent)
        }

        holder.learnButton.setOnClickListener {
            val i = Intent(context, LearnActivity::class.java)
            i.putExtra("query", "SELECT * FROM ${WordDBHelper.WORDS_TABLE_NAME} WHERE set_id=${sets[position].id}")
            context.startActivity(i)
        }

        holder.deleteButton.setOnClickListener {
            DeleteSetDialog(context, R.layout.delete_set, sets[position].id).showDialog()
        }
    }

    override fun getItemCount(): Int = sets.size
}