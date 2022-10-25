package com.matttax.erica

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.*


var words = mutableListOf<QuizWord>()

class LearnActivity : AppCompatActivity() {
    lateinit var fld: EditText
    lateinit var wrd: TextView
    lateinit var pbr: ProgressBar
    lateinit var ptx: TextView

    var total = 0
    var answered = 0

    lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        fld = findViewById(R.id.wordInput)
        wrd = findViewById(R.id.wordAsked)
        pbr = findViewById(R.id.studyProgressBar)
        ptx = findViewById(R.id.studyProgressInfo)

        val close: ImageView = findViewById(R.id.closeLearn)
        val doNotKnow: TextView = findViewById(R.id.doNotKnowWord)

        close.setOnClickListener {
            finish()
        }

        doNotKnow.setOnClickListener {
            val write = WordDBHelper(this).writableDatabase
            var w = words.first()
            words.add(w)
            write.execSQL("UPDATE words SET times_asked = times_asked + 1, " +
                    "last_asked = CURRENT_TIMESTAMP " +
                    "WHERE id=${words.first().id}")

            val dlgAlert: AlertDialog.Builder = AlertDialog.Builder(this)
            dlgAlert.setMessage(words.last().word.definition)
            dlgAlert.setTitle("Incorrect!")
            dlgAlert.setCancelable(true)
            dlgAlert.create().show()
            words.removeFirst()
        }

        getWords()
        total = words.size
        ptx.text = "0/$total"
        pbr.max = total

        getNext()
        fld.setOnKeyListener(View.OnKeyListener { view, i, keyEvent ->
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
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

    fun getWords() {
        val cursor = WordDBHelper(this).writableDatabase.rawQuery(intent.getStringExtra("query"), null)
        if (cursor != null) {
            if (cursor.count != 0) {
                while (cursor.moveToNext()) {
                    words += QuizWord(cursor.getInt(0), LanguagePair(cursor.getString(1), cursor.getString(2)),
                        Word(cursor.getString(3), cursor.getString(4)), cursor.getInt(5), cursor.getInt(6),
                        Date(cursor.getLong(7)*1000), cursor.getInt(8))
                }
            }
        }
    }

    fun getNext() {
        if (words.isNotEmpty()) {
            wrd.text = words.first().word.term
            spell(words.first().word.term, words.first().langPair.termLanguage)
        } else finish()
    }

    fun readWord() {
        val bld = androidx.appcompat.app.AlertDialog.Builder(this)
        val vwy = layoutInflater.inflate(R.layout.word_answered, null)
        bld.setView(vwy)
        val dlg = bld.create()
        val hd: TextView = vwy.findViewById(R.id.answeredHeader)
        val wd: TextView = vwy.findViewById(R.id.answeredCorrectWord)
        val ad: Button = vwy.findViewById(R.id.answerNext)

        spell(words.first().word.definition, words.first().langPair.definitionLanguage)
        val write = WordDBHelper(this).writableDatabase
        if (fld.text.toString() != words.first().word.definition) {
            var w = words.first()
            words.add(w)
            write.execSQL("UPDATE words SET times_asked = times_asked + 1, " +
                    "last_asked = CURRENT_TIMESTAMP " +
                    "WHERE id=${words.first().id}")
            hd.setBackgroundColor(ContextCompat.getColor(this, R.color.crimson))
            hd.text = "Incorrect"
            wd.text = words.first().word.definition

        } else {
            write.execSQL("UPDATE words SET times_asked = times_asked + 1, " +
                    "last_asked = CURRENT_TIMESTAMP, " +
                    "times_correct = times_correct + 1 " +
                    "WHERE id=${words.first().id}")
            answered++
            pbr.incrementProgressBy(1)
            ptx.text = "$answered/$total"

            wd.text = ""
        }
        ad.setOnClickListener {
            words.removeFirst()
            getNext()
            fld.text.clear()
            dlg.dismiss()
        }
        dlg.show()

    }

    fun spell(word: String, language: String) {
        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = if (language == "en") Locale.US else Locale("ru", "RU")
                tts.speak(word, TextToSpeech.QUEUE_ADD, null, "")
            }
        }
    }
}