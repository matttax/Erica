package com.matttax.erica

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class WordsActivity : AppCompatActivity() {

    var words = mutableListOf<Word>()

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

        var lrn: CardView = findViewById(R.id.startLearn)
        lrn.setOnClickListener {
            val i = Intent(this, LearnActivity::class.java)
            startActivity(i)
        }
    }

    fun getWords() {
        val db = WordDBHelper(this)
        val cursor = db.getWords(intent.getIntExtra("setid", 0))
        if (cursor != null) {
            if (cursor.count != 0) {
                while (cursor.moveToNext()) {
                    words += Word(cursor.getString(3), cursor.getString(4))
                }
            }
        }
    }
}