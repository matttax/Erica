package com.matttax.erica.activities

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.matttax.erica.*
import com.matttax.erica.adaptors.TranslationAdaptor


class MainActivity : AppCompatActivity() {
    private val db: WordDBHelper = WordDBHelper(this)

    private lateinit var setSpinner: Spinner
    private lateinit var termTextField: TextInputEditText
    lateinit var defTextField: TextInputEditText
    private lateinit var toSetsButton: MaterialButton

    private lateinit var addWord: MaterialButton
    private lateinit var dismissWord: MaterialButton

    private lateinit var tr: RecyclerView

    private lateinit var translator: Translator

    private lateinit var tabs: TabLayout
    private var sets = mutableMapOf<String, Int>()

    var selected = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        termTextField = findViewById(R.id.editSource)
        //translatedTV = findViewById(R.id.translatedText)
        toSetsButton = findViewById(R.id.toset)
        setSpinner = findViewById(R.id.setsSpinner)
        defTextField = findViewById(R.id.defText)

        addWord = findViewById(R.id.addWord)
        dismissWord = findViewById(R.id.dismissWord)
        tabs = findViewById(R.id.sliding_tabs)

        tr = findViewById(R.id.translations)

        dismissWord.isInvisible = true

        termTextField.doOnTextChanged { text, start, before, count ->
            if (addWord.text.toString().lowercase() == "add") {
                addWord.text = "translate"
                addWord.background.setTint(ContextCompat.getColor(this, R.color.blue))
                dismissWord.isInvisible = true
            }
        }

        loadSets()
        var setID = sets.values.first()

        setSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setID = sets[parent!!.selectedItem.toString()]!!
                selected = position
            }
        }

        toSetsButton.setOnClickListener {
            val setsIntent = Intent(this, SetsActivity::class.java)
            startActivity(setsIntent)
        }

        dismissWord.setOnClickListener {
            termTextField.text = SpannableStringBuilder("")
            defTextField.text = SpannableStringBuilder("")
            tr.adapter = null
        }

        addWord.setOnClickListener {
            if (addWord.text.toString().lowercase() == "translate") {
                translator = Translator(this, termTextField.text.toString(),
                    LanguagePair("en", "ru"), tabs.selectedTabPosition)
                tr.adapter = translator.getLoadedAdaptor()
                when (tabs.selectedTabPosition) {
                    0 -> tr.layoutManager = FlexboxLayoutManager(this@MainActivity)
                    else -> tr.layoutManager = LinearLayoutManager(this@MainActivity)
                }

                addWord.text = "add"
                addWord.background.setTint(ContextCompat.getColor(this, R.color.green))
                dismissWord.isInvisible = false

            } else {

                val db = WordDBHelper(this)
                //db.addWord("en", "ru", termTextField.text.toString(), defTextField.text.toString(), allWordsSetId)
                db.addWord(
                    "en",
                    "ru",
                    termTextField.text.toString(),
                    defTextField.text.toString(),
                    setID
                )

                val write = db.writableDatabase
                write.execSQL("UPDATE sets SET words_count = words_count + 1 WHERE id=$setID")
                dismissWord.callOnClick()

                //write.execSQL("UPDATE sets SET words_count = words_count + 1 WHERE id=$allWordsSetId")

//            termTextField.text = SpannableStringBuilder("")
//            defTextField.text = SpannableStringBuilder("")
            }
        }

        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (termTextField.text.toString() == "")
                    return
                tr.adapter = translator.getAdaptorAtPosition(tab!!.position)

                when (tab.position) {
                    0 -> tr.layoutManager = FlexboxLayoutManager(this@MainActivity)
                    else -> tr.layoutManager = LinearLayoutManager(this@MainActivity)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
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
    }

    fun loadSets() {
        val allSets = db.getSets()
        sets.clear()
        Log.i("sets", allSets.map { it.id.toString() }.toString())
        for (set in allSets) {
            sets[set.name] = set.id
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

        val s = db.getLastSetAdded()
        var cnt = 0
        if (s != -1) {
            for (i in sets) {
                if (i.value == s)
                    selected = cnt
                cnt += 1
            }
        }

        if (sets.size - 1 < selected)
            selected = 0


        val setAdaptor = ArrayAdapter(this, R.layout.sets_spinner_item, sets.keys.toList())
        setAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        setSpinner.adapter = setAdaptor
        setSpinner.setSelection(selected)
    }

    override fun onResume() {
        super.onResume()
        loadSets()
    }



}

