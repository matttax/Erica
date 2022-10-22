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


var words = mutableListOf<Word>()

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
                if (fld.text.toString() != words.first().definition) {
                    var w = words.first()
                    words.add(w)

                    val dlgAlert: AlertDialog.Builder = AlertDialog.Builder(this)
                    dlgAlert.setMessage(words.last().definition)
                    dlgAlert.setTitle("Incorrect!")
                    dlgAlert.setCancelable(true)
                    dlgAlert.create().show()

                } else {
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

    fun getWords() {
        val db = WordDBHelper(this)
        val cursor = db.getWords(3)
        if (cursor != null) {
            if (cursor.count != 0) {
                while (cursor.moveToNext()) {
                    words += Word(cursor.getString(3), cursor.getString(4))
                }
            }
        }
    }

    fun getNext() {
        if (!words.isEmpty()) {
            wrd.text = words.first().term
            currentWord = words.first().term
        } else finish()
    }
}