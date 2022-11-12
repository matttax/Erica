package com.matttax.erica.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.*
import com.matttax.erica.adaptors.WordAdaptor
import com.matttax.erica.WordDBHelper
import com.matttax.erica.dialogs.StartLearnDialog

class WordsActivity : AppCompatActivity() {
    private val db: WordDBHelper = WordDBHelper(this)
    var words = mutableListOf<QuizWord>()

    lateinit var rv: RecyclerView
    lateinit var head: TextView
    lateinit var subhead: TextView

    lateinit var set: WordSet


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_words)

        rv = findViewById(R.id.wordsList)
        rv.layoutManager = LinearLayoutManager(this)
        loadWords()
        set = getSetFromIntents()

        head = findViewById(R.id.setName)
        subhead = findViewById(R.id.setDescr)
        head.text = set.name
        subhead.text = set.description


        val lrn: CardView = findViewById(R.id.startLearn)
        lrn.setOnClickListener {
            StartLearnDialog(this, R.layout.start_learn_dialog, set.wordsCount, set.id).showDialog()
        }
    }

    fun loadWords() {
        words = db.getWords(intent.getIntExtra("setid", 1))
        rv.adapter = WordAdaptor(this, words, ContextCompat.getColor(this, R.color.blue))
    }

    private fun getSetFromIntents(): WordSet {
        val setId = intent.getIntExtra("setid", 1)
        val setName = intent.getStringExtra("setname").toString()
        val setDescription = intent.getStringExtra("setdescr").toString()
        val wordsCount = intent.getIntExtra("setwordcount", 0)
        return WordSet(setId, setName, setDescription, wordsCount)
    }
}