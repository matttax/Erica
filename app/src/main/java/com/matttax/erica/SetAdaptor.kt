package com.matttax.erica

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText


class SetAdaptor(var context: Context, var sets: List<SetOfWords>) :
    RecyclerView.Adapter<SetAdaptor.SetViewHolder>() {

    class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var setName: TextView
        var wordsCount: TextView
        var setLayout: LinearLayout

        var learnButton: MaterialButton
        var deleteButton: MaterialButton

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
            context.startActivity(intent)
        }

        holder.learnButton.setOnClickListener {
            val i = Intent(context, LearnActivity::class.java)
            context.startActivity(i)
        }

        holder.deleteButton.setOnClickListener {
            val bld = AlertDialog.Builder(context)
            val vwy = (context as Activity).layoutInflater.inflate(R.layout.create_set_dialog, null)
            bld.setView(vwy)
            val dlg = bld.create()
            dlg.show()

            val ad: MaterialButton = vwy.findViewById(R.id.yesDeleteSet)
            val dm: MaterialButton = vwy.findViewById(R.id.noDeleteSet)

            ad.setOnClickListener {
                val db = WordDBHelper(context)
                db.deleteSet(sets[position].id)
                dlg.dismiss()
            }
            dm.setOnClickListener {
                dlg.dismiss()
            }
        }
    }

    override fun getItemCount(): Int = sets.size
}