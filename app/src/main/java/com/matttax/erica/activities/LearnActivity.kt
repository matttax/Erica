package com.matttax.erica.activities

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.*
import com.matttax.erica.adaptors.WordAdaptor
import com.matttax.erica.dialogs.AfterBatchDialog
import com.matttax.erica.dialogs.WordAnsweredDialog


class LearnActivity : AppCompatActivity() {
    private val db: WordDBHelper = WordDBHelper(this)

    lateinit var studying: WordGroup
    var words = mutableListOf<QuizWord>()
    var incorrectWords = mutableListOf<QuizWord>()
    var correctWords = mutableListOf<QuizWord>()

    lateinit var definitionInputField: EditText
    lateinit var termAskedField: TextView
    lateinit var answeredProgressBar: ProgressBar
    lateinit var answeredTextInfo: TextView

    var total = 0
    var answered = 0

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        studying = WordGroup(db.getWords(intent.getStringExtra("query")), 3)

        definitionInputField = findViewById(R.id.wordInput)
        termAskedField = findViewById(R.id.wordAsked)
        answeredProgressBar = findViewById(R.id.studyProgressBar)
        answeredTextInfo = findViewById(R.id.studyProgressInfo)

        val close: ImageView = findViewById(R.id.closeLearn)
        val doNotKnow: TextView = findViewById(R.id.doNotKnowWord)

        close.setOnClickListener {
            finish()
        }

        doNotKnow.setOnClickListener {
            readWord(true)
        }

        words = studying.getNextBatch(mutableListOf()) as MutableList<QuizWord>
        getNext()

        total = words.size
        answeredTextInfo.text = "0/$total"
        answeredProgressBar.max = total

        definitionInputField.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if ((keyEvent.action == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                readWord()
                return@OnKeyListener true
            }
            false
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        words.clear()
    }

    fun getNext() {
        if (words.isNotEmpty()) {
            termAskedField.text = words.first().word.term
        } else if (!studying.completed()) {
            words = studying.getNextBatch(incorrectWords) as MutableList<QuizWord>
            total = words.size
            answered = 0
            incorrectWords.clear()
            correctWords.clear()
            termAskedField.text = words.first().word.term
            answeredProgressBar.progress = 0
        } else finish()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun readWord(unknownWord:Boolean=false) {
        words.first().spellDefinition(this)
        val answer: String? = if (unknownWord) null else definitionInputField.text.toString()
        WordAnsweredDialog(this,
            R.layout.word_answered, Word(answer, words.first().word.definition), words.first().id, words.first()).showDialog()
    }

    fun updateQuestion() {
        words.removeFirst()
        getNext()
        definitionInputField.text.clear()
    }
}