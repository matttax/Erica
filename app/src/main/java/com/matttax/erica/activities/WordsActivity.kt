package com.matttax.erica.activities

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.matttax.erica.*
import com.matttax.erica.adaptors.WordAdaptor
import com.matttax.erica.dialogs.DeleteWordDialog
import com.matttax.erica.dialogs.MoveDialog
import com.matttax.erica.dialogs.StartLearnDialog

class WordsActivity : AppCompatActivity() {
    private val db: WordDBHelper = WordDBHelper(this)
    var words = mutableListOf<StudyCard>()
    var selected = mutableListOf<StudyCard>()

    lateinit var rv: RecyclerView
    lateinit var head: TextView
    lateinit var subhead: TextView

    lateinit var set: WordSet

    lateinit var lrn: LinearLayout
    lateinit var strt: ImageView

    lateinit var studyButton: MaterialButton
    lateinit var deleteButton: MaterialButton
    lateinit var moveButton: MaterialButton

    var shitSelected: Boolean = false


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

        strt = findViewById(R.id.startLearnImage)
        lrn = findViewById(R.id.startLearn)
        lrn.setOnClickListener {
            StartLearnDialog(this, R.layout.start_learn_dialog, set.wordsCount, set.id).showDialog()
        }

        initButtons()
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

    private fun initButtons() {
        val moveLP = LinearLayout.LayoutParams(lrn.width / 3 - 50, LinearLayout.LayoutParams.WRAP_CONTENT,1f)
        moveLP.setMargins(50, 20, 20, 0)
        moveButton = getButton(moveLP, "Move", R.color.blue) {
            MoveDialog(this, R.layout.move_dialog, set.id, selected.map { it.id }).showDialog()
            words.removeAll { selected.contains(it) }
            selected.clear()
            updateHead()
            rv.adapter!!.notifyDataSetChanged()
        }

        val studyLP = LinearLayout.LayoutParams(lrn.width / 3 - 50, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        studyLP.setMargins(20, 20, 20, 0)
        studyButton = getButton(studyLP, "Study", R.color.green) {
            val learnIntent = Intent(this, LearnActivity::class.java)
            val query = "SELECT * FROM ${WordDBHelper.WORDS_TABLE_NAME} " +
                    "WHERE ${WordDBHelper.COLUMN_WORD_ID} IN ${selected.map { it.id }.toString().replace("[", "(").replace("]", ")")}"
            learnIntent.putExtra("query", query)
            learnIntent.putExtra("batch_size", 7)
            startActivity(learnIntent)
            selected.clear()
            loadWords()
            lrn.removeAllViews()
            lrn.addView(strt)
            lrn.addView(head)
        }

        val deleteLP = LinearLayout.LayoutParams(lrn.width / 3 - 50, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        deleteLP.setMargins(20, 20, 50, 0)
        deleteButton = getButton(deleteLP, "Delete", R.color.crimson) {
            DeleteWordDialog(this, R.layout.delete_word, *selected.toTypedArray()).showDialog()
        }
    }

    fun getButton(layoutParams: LinearLayout.LayoutParams, text: String, color: Int, onClick: View.OnClickListener): MaterialButton {
        val button = MaterialButton(ContextThemeWrapper(this, R.style.AppTheme_Button), null, R.style.AppTheme_Button)
        button.layoutParams = layoutParams
        button.text = text
        button.gravity = Gravity.CENTER
        button.setBackgroundColor(ContextCompat.getColor(this, color))
        button.setOnClickListener(onClick)
        return button
    }

    fun updateHead()  {
        if (shitSelected == selected.isNotEmpty())
            return
        shitSelected = selected.isNotEmpty()
        lrn.removeAllViews()
        if (shitSelected) {
            lrn.addView(moveButton)
            lrn.addView(studyButton)
            lrn.addView(deleteButton)
        } else {
            lrn.addView(strt)
            lrn.addView(head)

        }
    }

    override fun onBackPressed() {
        if (selected.isEmpty())
            super.onBackPressed()
        else {
            selected.clear()
            rv.adapter!!.notifyDataSetChanged()
            updateHead()
        }
    }
}