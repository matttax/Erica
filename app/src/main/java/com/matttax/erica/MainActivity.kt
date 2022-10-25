package com.matttax.erica

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions


class MainActivity : AppCompatActivity() {

    private lateinit var setSpinner: Spinner
    private lateinit var termTextField: TextInputEditText
    private lateinit var defTextField: TextInputEditText
    private lateinit var toSetsButton: MaterialButton
    private lateinit var translatedTV: WebView

    private lateinit var translateOxford: MaterialButton
    private lateinit var translateCambridge: MaterialButton
    private lateinit var translateGlobse: MaterialButton

    private lateinit var addWord: MaterialButton
    private lateinit var dismissWord: MaterialButton


    private val languages = listOf("From", "Russian", "English", "German")
    private var sets = mutableMapOf<String, Int>()

    private var termCode = 0
    private var defCode = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        termTextField = findViewById(R.id.editSource)
        translatedTV = findViewById(R.id.translatedText)
        toSetsButton = findViewById(R.id.toset)
        setSpinner = findViewById(R.id.setsSpinner)
        defTextField = findViewById(R.id.defText)

        translateOxford = findViewById(R.id.translateOxford)
        translateCambridge = findViewById(R.id.translateCambridge)
        translateGlobse = findViewById(R.id.translateGlobse)

        addWord = findViewById(R.id.addWord)
        dismissWord = findViewById(R.id.dismissWord)

        val db = WordDBHelper(this)
        val cursor = db.getSet()
        if (cursor != null) {
            if (cursor.count != 0) {
                while (cursor.moveToNext()) {
                    sets[cursor.getString(1)] = cursor.getInt(0)
                }
            }
        }

        if (sets.isEmpty()) {
            val wdb = WordDBHelper(this).writableDatabase
            val cv = ContentValues()
            cv.put(WordDBHelper.COLUMN_NAME, "All Words")
            cv.put(WordDBHelper.COLUMN_WORDS_COUNT, 0)
            cv.put(WordDBHelper.COLUMN_SET_DESCRIPTION, "Unordered set of words")
            wdb.insert(WordDBHelper.SETS_TABLE_NAME, null, cv)
            sets["All Words"] = 1
        }
        var setID = sets.values.first()

        setSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //if (setID == sets[parent!!.selectedItem.toString()]!!)
                    // goto set
                setID = sets[parent!!.selectedItem.toString()]!!
                println(setID)
            }

        }


        val setAdaptor = ArrayAdapter(this, R.layout.sets_spinner_item, sets.keys.toList())
        setAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        setSpinner.adapter = setAdaptor

        translateOxford.setOnClickListener {
            translateText(termCode, defCode, termTextField.text.toString(), 0)
        }

        translateCambridge.setOnClickListener {
            translateText(termCode, defCode, termTextField.text.toString(), 1)
        }

        translateGlobse.setOnClickListener {
            translateText(termCode, defCode, termTextField.text.toString(), 2)
        }

        toSetsButton.setOnClickListener {
            val i = Intent(this, SetsActivity::class.java)
            startActivity(i)
        }

        dismissWord.setOnClickListener {
            termTextField.text = SpannableStringBuilder("")

            defTextField.text = SpannableStringBuilder("")
            translatedTV.loadUrl("about:blank")
        }

        addWord.setOnClickListener {
            val allWordsSetId = 3
            val db = WordDBHelper(this)
            //db.addWord("en", "ru", termTextField.text.toString(), defTextField.text.toString(), allWordsSetId)
            db.addWord("en", "ru", termTextField.text.toString(), defTextField.text.toString(), setID)

            val write = db.writableDatabase
            write.execSQL("UPDATE sets SET words_count = words_count + 1 WHERE id=$setID")
            //write.execSQL("UPDATE sets SET words_count = words_count + 1 WHERE id=$allWordsSetId")

            termTextField.text = SpannableStringBuilder("")
            defTextField.text = SpannableStringBuilder("")
            translatedTV.loadUrl("about:blank")
        }
    }

    private fun translateText(termCode: Int, defCode: Int, text: String, siteCode: Int) {
        val options = FirebaseTranslatorOptions.Builder().setSourceLanguage(FirebaseTranslateLanguage.EN).
                                                          setTargetLanguage(FirebaseTranslateLanguage.RU).
                                                          build()
        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        val conditions = FirebaseModelDownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener {
            translator.translate(text).addOnSuccessListener {
                defTextField.text = SpannableStringBuilder(it)
            }
        }

        translatedTV.webViewClient = WebViewClient()
        CookieManager.getInstance().setAcceptCookie(true)
        val webSettings = translatedTV.settings
        webSettings.javaScriptEnabled = true
        when (siteCode) {
            0 -> translatedTV.loadUrl("https://www.oxfordlearnersdictionaries.com/definition/english/${text.replace(' ', '-')}")
            1 -> translatedTV.loadUrl("https://dictionary.cambridge.org/dictionary/english-russian/${text.replace(' ', '-')}")
            2 -> translatedTV.loadUrl("https://ru.glosbe.com/словарь-английский-русский/${text.replace(" ", "%20")}")
        }
    }

}
