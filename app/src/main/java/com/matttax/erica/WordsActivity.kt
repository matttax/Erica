package com.matttax.erica

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.util.*

class WordsActivity : AppCompatActivity() {

    var words = mutableListOf<QuizWord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_words)

        val mainlayout: LinearLayout = findViewById(R.id.words_table)
        val rv: RecyclerView = findViewById(R.id.wordsList)

        val head: TextView = findViewById(R.id.setName)
        head.text = intent.getStringExtra("setname")

        val subhead: TextView = findViewById(R.id.setDescr)
        subhead.text = intent.getStringExtra("setdescr")

        getWords()
        val adpt = WordAdaptor(this, words)
        rv.adapter = adpt
        rv.layoutManager = LinearLayoutManager(this)


        val simpleCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val bld = AlertDialog.Builder(this@WordsActivity)
                val vwy = layoutInflater.inflate(R.layout.delete_word, null)
                bld.setView(vwy)
                val dlg = bld.create()
                dlg.show()

                val ad: MaterialButton = vwy.findViewById(R.id.yesDeleteWord)
                val dm: MaterialButton = vwy.findViewById(R.id.noDeleteWord)

                ad.setOnClickListener {
                    val db = WordDBHelper(this@WordsActivity)
                    db.deleteWord(words[viewHolder.adapterPosition].id, words[viewHolder.adapterPosition].setId)
                    words.removeAt(viewHolder.adapterPosition)
                    adpt.notifyItemRemoved(viewHolder.adapterPosition)
                    dlg.dismiss()
                }
                dm.setOnClickListener {
                    adpt.notifyDataSetChanged()
                    dlg.dismiss()
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(rv)

        var lrn: CardView = findViewById(R.id.startLearn)
        lrn.setOnClickListener {
            val i = Intent(this, LearnActivity::class.java)

            val bld = AlertDialog.Builder(this)
            val vwy = layoutInflater.inflate(R.layout.start_learn_dialog, null)
            bld.setView(vwy)
            val dlg = bld.create()
            dlg.show()

            val npk: NumberPicker = vwy.findViewById(R.id.wordsNumberPicker)
            npk.maxValue = intent.getIntExtra("setwordcount", 0)
            npk.minValue = 1
            npk.value = intent.getIntExtra("setwordcount", 0)

            val prt: Spinner = vwy.findViewById(R.id.priority)
            val prtAdaptor = ArrayAdapter(this, R.layout.sets_spinner_item, listOf("Worst answered", "Least asked", "Long ago asked"))
            prtAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            prt.adapter = prtAdaptor

            val rgm: Spinner = vwy.findViewById(R.id.regime)
            val rgmAdaptor = ArrayAdapter(this, R.layout.sets_spinner_item, listOf("Study", "Learn"))
            rgmAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            rgm.adapter = rgmAdaptor

            val no: Button = vwy.findViewById(R.id.noStartLearn)
            val yes: Button = vwy.findViewById(R.id.yesStartLearn)
            no.setOnClickListener {
                dlg.dismiss()
            }
            yes.setOnClickListener {
                val query = "SELECT * FROM ${WordDBHelper.WORDS_TABLE_NAME} " +
                        "WHERE set_id=${intent.getIntExtra("setid", 3)} " +
                        "ORDER BY ${getOrderBy(prt.selectedItem.toString())}" +
                        "LIMIT ${npk.value} "
                i.putExtra("query", query)
                dlg.dismiss()
                startActivity(i)

            }

        }
    }

    fun getWords() {
        val db = WordDBHelper(this)
        val cursor = db.getWords(intent.getIntExtra("setid", 0))
        if (cursor != null) {
            if (cursor.count != 0) {
                while (cursor.moveToNext()) {
                    words += QuizWord(cursor.getInt(0), LanguagePair(cursor.getString(1), cursor.getString(2)),
                        Word(cursor.getString(3), cursor.getString(4)), cursor.getInt(5), cursor.getInt(6), Date(), cursor.getInt(8))
                }
            }
        }
    }

    fun getOrderBy(str: String): String {
        return when(str) {
            "Worst answered" -> "times_correct / CAST(times_asked as float) ASC "
            "Least asked" -> "times_asked ASC "
            else -> "last_asked ASC "
        }
    }
}