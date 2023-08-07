package com.matttax.erica.activities

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.matttax.erica.*
import com.matttax.erica.databinding.ActivityLearnBinding
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
    var total = 0
    var answered = 0
    lateinit var definitionInputField: EditText
    public lateinit var binding: ActivityLearnBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnBinding.inflate(layoutInflater)

        setContentView(binding.root)
        definitionInputField = findViewById(R.id.definitionInputField)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        studying = WordGroup(db.getWords(intent.getStringExtra("query")),
                                    intent.getIntExtra("batch_size", 7),
                                intent.getStringExtra("ask") ?: "Translation")

        definitionInputField.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if ((keyEvent.action == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                readWord()
                return@OnKeyListener true
            }
            false
        })
        binding.close.setOnClickListener {
            finish()
        }
        binding.doNotKnow.setOnClickListener {
            readWord(true)
        }

        words = studying.getNextBatch(mutableListOf())
        getNext()
        words.peek().spellTerm(this)
        total = words.size

        binding.answeredTextInfo.text = "0/$total"
        binding.answeredProgressBar.apply {
            progressDrawable.setColorFilter(ContextCompat.getColor(this@LearnActivity, R.color.blue), PorterDuff.Mode.SRC_IN)
            max = total
        }
    }

    override fun onDestroy() {
        words.clear()
        super.onDestroy()
    }

    fun getNext() {
        if (words.isNotEmpty()) {
            binding.termAskedField.text = words.peek().word.word
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
                binding.answeredProgressBar.max = total
                answered = 0
                allIncorrectWords.addAll(incorrectWords)
                allCorrectWords.addAll(correctWords)
                incorrectWords.clear()
                correctWords.clear()
                binding.termAskedField.text = words.peek().word.word
                binding.answeredProgressBar.progress = 0
            }
        } else {
            finish()
        }
    }

    private fun readWord(unknownWord:Boolean=false) {
        words.peek().spellDefinition(this)
        val answer: String? = if (unknownWord || binding.definitionInputField.text?.isEmpty() != false) null
            else binding.definitionInputField.text.toString()
        WordAnsweredDialog(this,
            R.layout.word_answered, StudyItem(answer, words.peek().word.translation), words.peek().id, words.peek()).showDialog()
    }

    fun updateQuestion() {
        words.pop()
        getNext()
        binding.definitionInputField.text?.clear()
    }

}