package com.matttax.erica.activities

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.matttax.erica.*
import com.matttax.erica.dialogs.WordAnsweredDialog
import java.util.*


class LearnActivity : AppCompatActivity() {
    private val db: WordDBHelper = WordDBHelper(this)

    lateinit var studying: WordGroup
    lateinit var words: Stack<StudyCard>
    var incorrectWords = mutableListOf<StudyCard>()
    var correctWords = mutableListOf<StudyCard>()

    var allIncorrectWords = mutableListOf<StudyCard>()
    var allCorrectWords = mutableListOf<StudyCard>()

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
        studying = WordGroup(db.getWords(intent.getStringExtra("query")), intent.getIntExtra("batch_size", 7),
                                intent.getStringExtra("ask") ?: "Translation")

        definitionInputField = findViewById(R.id.wordInput)
        termAskedField = findViewById(R.id.wordAsked)
        answeredProgressBar = findViewById(R.id.studyProgressBar)
        answeredTextInfo = findViewById(R.id.studyProgressInfo)

        val close: ImageView = findViewById(R.id.closeLearn)
        val doNotKnow: TextView = findViewById(R.id.doNotKnowWord)

        answeredProgressBar.progressDrawable.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
        close.setOnClickListener {
            finish()
        }

        doNotKnow.setOnClickListener {
            readWord(true)
        }

        words = studying.getNextBatch(mutableListOf())
        getNext()
        words.peek().spellTerm(this)

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
        words.clear()
        super.onDestroy()
    }

    fun getNext() {
        if (words.isNotEmpty()) {
            termAskedField.text = words.peek().word.word
//            Log.i("test", words.peek().word.word.toString())
//            if (words.peek().word.translation == "gait") {
//                val f: LinearLayout = findViewById(R.id.wordField)
//                f.removeAllViews()
//                f.addView(MaterialButton(ContextThemeWrapper(this, R.style.AppTheme_Button), null, R.style.AppTheme_Button))
//                f.addView(MaterialButton(ContextThemeWrapper(this, R.style.AppTheme_Button), null, R.style.AppTheme_Button))
//                f.addView(MaterialButton(ContextThemeWrapper(this, R.style.AppTheme_Button), null, R.style.AppTheme_Button))
//                f.addView(MaterialButton(ContextThemeWrapper(this, R.style.AppTheme_Button), null, R.style.AppTheme_Button))
//            }
        } else if (studying.words.isNotEmpty()) {
            words = studying.getNextBatch(incorrectWords)
            if (studying.words.isEmpty()) {
                finish()
            } else {
                total = words.size
                answeredProgressBar.max = total
                answered = 0
                allIncorrectWords.addAll(incorrectWords)
                allCorrectWords.addAll(correctWords)
                incorrectWords.clear()
                correctWords.clear()
                termAskedField.text = words.peek().word.word
                answeredProgressBar.progress = 0
            }
        } else {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun readWord(unknownWord:Boolean=false) {
        words.peek().spellDefinition(this)
        val answer: String? = if (unknownWord || definitionInputField.text.isEmpty()) null else definitionInputField.text.toString()
        WordAnsweredDialog(this,
            R.layout.word_answered, StudyItem(answer, words.peek().word.translation), words.peek().id, words.peek()).showDialog()
    }

    fun updateQuestion() {
        words.pop()
        getNext()
        definitionInputField.text.clear()
    }

}