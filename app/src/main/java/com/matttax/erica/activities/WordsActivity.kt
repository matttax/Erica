package com.matttax.erica.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.matttax.erica.*
import com.matttax.erica.adaptors.WordAdaptor
import com.matttax.erica.databinding.ActivityWordsBinding
import com.matttax.erica.dialogs.DeleteWordDialog
import com.matttax.erica.dialogs.MoveDialog
import com.matttax.erica.dialogs.StartLearnDialog
import com.matttax.erica.domain.model.Language
import com.matttax.erica.presentation.model.translate.TextCardState
import com.matttax.erica.presentation.model.translate.TranslatedText
import com.matttax.erica.presentation.model.translate.TranslatedTextCard

class WordsActivity : AppCompatActivity() {
    private val db: WordDBHelper = WordDBHelper(this)
    public lateinit var binding: ActivityWordsBinding
    var words = mutableListOf<StudyCard>()
    var selected = mutableListOf<StudyCard>()
    lateinit var set: WordSet
    var shitSelected: Boolean = false

    lateinit var studyButton: MaterialButton
    lateinit var deleteButton: MaterialButton
    lateinit var moveButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val preferences = getSharedPreferences("ericaPrefs", Context.MODE_PRIVATE)

        binding.sortByWords.apply {
            adapter = ArrayAdapter(this@WordsActivity, R.layout.params_spinner_item,
                listOf("Last changed first", "First changed first",
                    "Last answered first", "Most accurate first", "Least accurate first"))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (position) {
                        0 -> loadWords("id DESC")
                        1 -> loadWords()
                        2 -> loadWords("last_asked DESC")
                        3 -> loadWords("${WordDBHelper.COLUMN_TIMES_CORRECT} / CAST(${WordDBHelper.COLUMN_TIMES_ASKED} as float) DESC ")
                        4 -> loadWords("${WordDBHelper.COLUMN_TIMES_CORRECT} / CAST(${WordDBHelper.COLUMN_TIMES_ASKED} as float) ASC ")
                    }
                    binding.wordsList.adapter!!.notifyDataSetChanged()
                    val editor = preferences.edit()
                    editor.putInt("ORDER_POS", position).apply()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            setSelection(preferences.getInt("ORDER_POS", 0))
        }

        binding.wordsList.layoutManager = LinearLayoutManager(this@WordsActivity)
        loadWords()
        set = getSetFromIntents()

        binding.setName.text = set.name
        binding.setDescr.text = set.description

        if (set.description.isEmpty())
            binding.descrLayout.removeAllViews()

        binding.startLearn.setOnClickListener {
                StartLearnDialog(this@WordsActivity, R.layout.start_learn_dialog, set.wordsCount, set.id).showDialog()
        }

        initButtons()
    }

    fun loadWords(order: String = "id") {
        words = db.getWords(intent.getIntExtra("setid", 1), order)
        binding.wordsList.adapter = WordAdaptor(this, words.map {
            TranslatedTextCard(
                translatedText = TranslatedText(
                    it.word.word ?: "",
                    it.word.translation,
                    Language(it.langPair.termLanguage),
                    Language(it.langPair.definitionLanguage),
                ),
                isEditable = true,
                isSelected = false,
                state = TextCardState.DEFAULT
            )
        })
    }

    private fun getSetFromIntents(): WordSet {
        val setId = intent.getIntExtra("setid", 1)
        val setName = intent.getStringExtra("setname").toString()
        val setDescription = intent.getStringExtra("setdescr").toString()
        val wordsCount = intent.getIntExtra("setwordcount", 0)
        return WordSet(setId, setName, setDescription, wordsCount)
    }

    private fun initButtons() {
        val moveLP = LinearLayout.LayoutParams(binding.startLearn.width / 3 - 50, LinearLayout.LayoutParams.WRAP_CONTENT,1f)
        moveLP.setMargins(50, 20, 20, 0)
        moveButton = getButton(moveLP, "Move", R.color.blue) {
            MoveDialog(this, R.layout.move_dialog, set.id, selected.map { it.id }).showDialog()
            words.removeAll { selected.contains(it) }
            selected.clear()
            updateHead()
            binding.wordsList.adapter!!.notifyDataSetChanged()
        }

        val studyLP = LinearLayout.LayoutParams(binding.startLearn.width / 3 - 50, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        studyLP.setMargins(20, 20, 20, 0)
        studyButton = getButton(studyLP, "Study", R.color.green) {
            val query = "SELECT * FROM ${WordDBHelper.WORDS_TABLE_NAME} " +
                    "WHERE ${WordDBHelper.COLUMN_WORD_ID} IN " +
                    selected.map { it.id }.toString().replace("[", "(")
                        .replace("]", ")")
            val learnIntent = Intent(this, LearnActivity::class.java).apply {
                putExtra("query", query)
                putExtra("batch_size", 7)
            }
            startActivity(learnIntent)
            selected.clear()
            loadWords()
            binding.startLearn.apply {
                removeAllViews()
                addView(binding.startLearnImage)
                addView(binding.setName)
            }
        }

        val deleteLP = LinearLayout.LayoutParams(binding.startLearn.width / 3 - 50, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        deleteLP.setMargins(20, 20, 50, 0)
        deleteButton = getButton(deleteLP, "Delete", R.color.crimson) {
            DeleteWordDialog(this, R.layout.delete_word, *selected.toTypedArray()).showDialog()
        }
    }

    fun getButton(layoutParams: LinearLayout.LayoutParams,
                  text: String,
                  color: Int,
                  onClick: View.OnClickListener) = MaterialButton(ContextThemeWrapper(this,
        R.style.AppTheme_Button),
        null, R.style.AppTheme_Button).apply {
            setLayoutParams(layoutParams)
            setText(text)
            gravity = Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(this@WordsActivity, color))
            setOnClickListener(onClick)
        }

    fun updateHead()  {
        if (shitSelected == selected.isNotEmpty())
            return
        shitSelected = selected.isNotEmpty()
        binding.startLearn.removeAllViews()
        if (shitSelected)
            binding.startLearn.apply {
                addView(moveButton)
                addView(studyButton)
                addView(deleteButton)
            }
        else binding.startLearn.apply {
                addView(binding.startLearnImage)
                addView(binding.setName)
            }
    }

    override fun onBackPressed() {
        if (selected.isEmpty())
            super.onBackPressed()
        else {
            selected.clear()
            binding.wordsList.adapter!!.notifyDataSetChanged()
            updateHead()
        }
    }
}