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
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.matttax.erica.*
import com.matttax.erica.adaptors.PartOfSpeechAdaptor
import com.matttax.erica.adaptors.TRANSLATION
import com.matttax.erica.adaptors.TranslationAdaptor
import com.matttax.erica.adaptors.WordAdaptor
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private val db: WordDBHelper = WordDBHelper(this)

    private lateinit var setSpinner: Spinner
    private lateinit var termTextField: TextInputEditText
    lateinit var defTextField: TextInputEditText
    private lateinit var toSetsButton: MaterialButton

    private lateinit var addWord: MaterialButton
    private lateinit var dismissWord: MaterialButton

    private lateinit var tr: RecyclerView

    private var translator: Translator? = null

    private lateinit var fromSpinner: Spinner
    private lateinit var toSpinner: Spinner
    private lateinit var swapButton: ImageView

    private lateinit var tabs: TabLayout
    private var sets = mutableMapOf<String, Int>()

    private lateinit var lp: LanguagePair

    val languages = listOf("English", "Russian", "German")

    val scope = CoroutineScope(SupervisorJob() +
            CoroutineExceptionHandler { _, t -> Log.i("Coroutine ex", t.message.toString())})
    var job: Job? = null

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

        fromSpinner = findViewById(R.id.fromLanguage)
        toSpinner = findViewById(R.id.toLanguage)
        swapButton = findViewById(R.id.swapLanguages)

        swapButton.setOnClickListener {
            val fr = fromSpinner.selectedItemPosition
            fromSpinner.setSelection(toSpinner.selectedItemPosition)
            toSpinner.setSelection(fr)
        }

        fromSpinner.adapter = ArrayAdapter(this, R.layout.sets_spinner_item, languages)
        toSpinner.adapter = ArrayAdapter(this, R.layout.sets_spinner_item, languages)

        toSpinner.setSelection(1)

        termTextField.doOnTextChanged { text, start, before, count ->
            switchButtons()
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

        fromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                switchButtons()
            }
        }

        toSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                switchButtons()
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
                addWord.text = "add"
                addWord.background.setTint(ContextCompat.getColor(this, R.color.green))
                dismissWord.isInvisible = false
                translator = null
                tr.adapter = null
                translateClicked()
            } else {
                db.addWord(
                    getLang(fromSpinner.selectedItem.toString()),
                    getLang(toSpinner.selectedItem.toString()),
                    termTextField.text.toString(),
                    defTextField.text.toString(),
                    setID
                )
                val write = db.writableDatabase
                write.execSQL("UPDATE sets SET words_count = words_count + 1 WHERE id=$setID")
                dismissWord.callOnClick()
            }
        }

        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (termTextField.text.toString() == "" || tab == null)
                    return
                updateAdaptor(translator)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
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
        //setAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        setSpinner.adapter = setAdaptor
        setSpinner.setSelection(selected)
    }

    override fun onResume() {
        super.onResume()
        loadSets()
    }

    fun switchButtons() {
        if (addWord.text.toString().lowercase() == "add") {
            addWord.text = "translate"
            addWord.background.setTint(ContextCompat.getColor(this, R.color.blue))
            dismissWord.isInvisible = true
        }
    }

    fun translateClicked() {
        job?.cancel()
        job = scope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                translator = getTranslator()
            }
            updateAdaptor(translator)
        }
    }

    fun getTranslator() = Translator(this, termTextField.text.toString(),
                                            LanguagePair(
                                                getLang(fromSpinner.selectedItem.toString()),
                                                getLang(toSpinner.selectedItem.toString())))

    fun updateAdaptor(translator: Translator?) {
        Log.i("adaptor", "update_entered")
        if (translator == null)
            return
        when (tabs.selectedTabPosition) {
            0 -> {
                tr.layoutManager = FlexboxLayoutManager(this)
                tr.adapter = TranslationAdaptor(this, translator.translations, TRANSLATION.WORD)
            }
            1 -> {
                tr.layoutManager = LinearLayoutManager(this)
                tr.adapter = PartOfSpeechAdaptor(this, translator.definitions)
            }
            else -> {
                tr.layoutManager = LinearLayoutManager(this)
                tr.adapter = WordAdaptor(this, translator.examples,
                    ContextCompat.getColor(this, R.color.blue), Int.MAX_VALUE-1)
            }
        }
        Log.i("adaptor", "updated")
    }

}

fun getLang(l: String) = when(l) {
    "Russian" -> "ru"
    "German" -> "de"
    else -> "en"
}

