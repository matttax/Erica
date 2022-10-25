package com.matttax.erica

import android.app.AlertDialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*


var words = mutableListOf<QuizWord>()

class LearnActivity : AppCompatActivity() {
    lateinit var fld: EditText
    lateinit var wrd: TextView
    lateinit var pbr: ProgressBar
    lateinit var ptx: TextView
    lateinit var currentWord: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        fld = findViewById(R.id.wordInput)
        wrd = findViewById(R.id.wordAsked)
        pbr = findViewById(R.id.studyProgressBar)
        ptx = findViewById(R.id.studyProgressInfo)

        getWords()
        val total = words.size
        var progress = 0
        ptx.text = "0/$total"
        pbr.max = total

        getNext()
        fld.setOnKeyListener(View.OnKeyListener { view, i, keyEvent ->
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                val write = WordDBHelper(this).writableDatabase
                if (fld.text.toString() != words.first().word.definition) {
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

                } else {
                    write.execSQL("UPDATE words SET times_asked = times_asked + 1, " +
                            "last_asked = CURRENT_TIMESTAMP, " +
                            "times_correct = times_correct + 1 " +
                            "WHERE id=${words.first().id}")
                    progress++
                    pbr.incrementProgressBy(1)
                    ptx.text = "$progress/$total"
                }
                words.removeFirst()
                getNext()
                fld.text.clear()
                return@OnKeyListener true
            }
            false
        })

    }

    override fun onPause() {
        super.onPause()
        words.clear()
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
        if (!words.isEmpty()) {
            wrd.text = words.first().word.term
            currentWord = words.first().word.term
        } else finish()
    }
}