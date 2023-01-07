package com.matttax.erica.activities

import android.content.Context
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
import com.matttax.erica.LanguagePair
import com.matttax.erica.R
import com.matttax.erica.Translator
import com.matttax.erica.WordDBHelper
import com.matttax.erica.adaptors.PartOfSpeechAdaptor
import com.matttax.erica.adaptors.TRANSLATION
import com.matttax.erica.adaptors.TranslationAdaptor
import com.matttax.erica.adaptors.WordAdaptor
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private val db: WordDBHelper = WordDBHelper(this)

    private lateinit var setSpinner: Spinner
    private lateinit var termTextField: TextInputEditText
    private lateinit var defTextField: TextInputEditText
    private lateinit var toSetsButton: MaterialButton

    private lateinit var addWord: MaterialButton
    private lateinit var dismissWord: MaterialButton

    private lateinit var translations: RecyclerView

    private var translator: Translator? = null

    private lateinit var fromSpinner: Spinner
    private lateinit var toSpinner: Spinner
    private lateinit var swapButton: ImageView

    private lateinit var tabs: TabLayout
    private var sets = mutableMapOf<String, Int>()

    private lateinit var lp: LanguagePair

    private val languages = listOf("English", "Russian", "German", "French", "Spanish", "Italian")

    private val scope = CoroutineScope(SupervisorJob() +
            CoroutineExceptionHandler { _, t -> Log.i("Coroutine ex", t.message.toString())})
    private var job: Job? = null
    private var loadingJob: Job? = null

    private var selected = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val preferences = getSharedPreferences("ericaPrefs", Context.MODE_PRIVATE)

        loadSets()
        var setID = sets.values.first()
        setSpinner = findViewById<Spinner?>(R.id.setsSpinner).apply {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    setID = sets[parent!!.selectedItem.toString()]!!
                    selected = position
                }
            }
            adapter = ArrayAdapter(this@MainActivity, R.layout.sets_spinner_item, sets.keys.toList())
            setSelection(selected)
        }
        toSetsButton = findViewById<MaterialButton?>(R.id.toset).apply {
            setOnClickListener {
                val setsIntent = Intent(this@MainActivity, SetsActivity::class.java)
                startActivity(setsIntent)
            }
        }

        termTextField = findViewById<TextInputEditText?>(R.id.editSource).apply {
            doOnTextChanged { _, _, _, _ ->
                job?.cancel()
                loadingJob?.cancel()
                switchButtons()
            }
        }
        defTextField = findViewById(R.id.defText)

        addWord = findViewById<MaterialButton?>(R.id.addWord).apply {
            setOnClickListener {
                if (addWord.text.toString().lowercase() == "translate") {
                    translator = null
                    translations.adapter = null
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
        }
        dismissWord = findViewById<MaterialButton?>(R.id.dismissWord).apply {
            isInvisible = true
            setOnClickListener {
                termTextField.text = SpannableStringBuilder("")
                defTextField.text = SpannableStringBuilder("")
                translations.adapter = null
            }
        }

        tabs = findViewById<TabLayout?>(R.id.sliding_tabs).apply {
            addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (termTextField.text.toString() == "" || tab == null)
                        return
                    updateAdaptor(translator)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
        translations = findViewById(R.id.translations)

        val switcher = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                switchButtons()
            }
        }
        fromSpinner = findViewById<Spinner?>(R.id.fromLanguage).apply {
            adapter = ArrayAdapter(this@MainActivity, R.layout.sets_spinner_item, languages)
            setSelection(preferences.getInt("FROM", 0))
            onItemSelectedListener = switcher
        }
        toSpinner = findViewById<Spinner?>(R.id.toLanguage).apply {
            adapter = ArrayAdapter(this@MainActivity, R.layout.sets_spinner_item, languages)
            setSelection(preferences.getInt("TO", 1))
            onItemSelectedListener = switcher
        }
        swapButton = findViewById<ImageView?>(R.id.swapLanguages).apply {
            setOnClickListener {
                val fr = fromSpinner.selectedItemPosition
                fromSpinner.setSelection(toSpinner.selectedItemPosition)
                toSpinner.setSelection(fr)
            }
        }
    }

    fun loadSets() {
        val allSets = db.getSets()
        sets.clear()
        for (set in allSets)
            sets[set.name] = set.id
        if (sets.isEmpty())
            sets["All Words"] = db.addSet("All words", "Unordered set od words").toInt()

        val lastModifiedSetId = db.getLastSetAdded()
        var selectedIndex = 0
        if (lastModifiedSetId != -1) {
            for (set in sets) {
                if (set.value == lastModifiedSetId) {
                    selected = selectedIndex
                    break
                }
                selectedIndex++
            }
        }
        if (sets.size - 1 < selected)
            selected = 0
    }

    override fun onResume() {
        super.onResume()
        loadSets()
    }

    override fun onStop() {
        super.onStop()
        val preferences = getSharedPreferences("ericaPrefs", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.apply {
            putInt("FROM", fromSpinner.selectedItemPosition)
            putInt("TO", toSpinner.selectedItemPosition)
        }.apply()
    }

    fun switchButtons() {
        if (addWord.text.toString().lowercase() == "add"
                || addWord.text.toString().isEmpty()
                || addWord.text.toString().contains(".")) {
            addWord.textSize = 15F
            addWord.text = "translate"
            addWord.background.setTint(ContextCompat.getColor(this, R.color.blue))
            dismissWord.isInvisible = true
        }
    }

    private fun translateClicked() {
        job?.cancel()
        loadingJob = scope.launch(Dispatchers.Main) {
            for (i in 1..100) {
                if (translator != null) {
                    addWord.textSize = 15F
                    addWord.text = "add"
                    addWord.background.setTint(ContextCompat.getColor(this@MainActivity, R.color.green))
                    dismissWord.isInvisible = false
                    break
                }
                addWord.textSize = 30F
                addWord.text = ".".repeat(i % 4)
                delay(400)
            }
        }
        job = scope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                translator = getTranslator()
            }
            updateAdaptor(translator)
        }
    }

    private fun getTranslator() = Translator(this, termTextField.text.toString(),
                                            LanguagePair(
                                                getLang(fromSpinner.selectedItem.toString()),
                                                getLang(toSpinner.selectedItem.toString())))

    fun updateAdaptor(translator: Translator?) {
        Log.i("adaptor", "update_entered")
        if (translator == null)
            return
        when (tabs.selectedTabPosition) {
            0 -> {
                translations.layoutManager = FlexboxLayoutManager(this)
                translations.adapter = TranslationAdaptor(this, translator.translations, TRANSLATION.WORD)
            }
            1 -> {
                translations.layoutManager = LinearLayoutManager(this)
                translations.adapter = PartOfSpeechAdaptor(this, translator.definitions)
            }
            else -> {
                translations.layoutManager = LinearLayoutManager(this)
                translations.adapter = WordAdaptor(this, translator.examples,
                    ContextCompat.getColor(this, R.color.blue), Int.MAX_VALUE-1)
            }
        }
        Log.i("adaptor", "updated")
    }

    fun setDefText(text: CharSequence) {
        defTextField.setText(text, TextView.BufferType.EDITABLE)
    }

}

fun getLang(l: String) = when(l) {
    "Russian" -> "ru"
    "German" -> "de"
    "French" -> "fr"
    "Spanish" -> "es"
    "Italian" -> "it"
    else -> "en"
}

