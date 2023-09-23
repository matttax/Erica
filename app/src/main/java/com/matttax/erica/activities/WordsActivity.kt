package com.matttax.erica.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.matttax.erica.*
import com.matttax.erica.adaptors.WordAdaptor
import com.matttax.erica.databinding.ActivityWordsBinding
import com.matttax.erica.dialogs.impl.DeleteDialog
import com.matttax.erica.dialogs.impl.EditDialog
import com.matttax.erica.dialogs.impl.MoveDialog
import com.matttax.erica.dialogs.impl.StartLearnDialog
import com.matttax.erica.domain.config.SetId
import com.matttax.erica.domain.config.WordGroupConfig
import com.matttax.erica.domain.config.WordsSorting
import com.matttax.erica.model.WordSet
import com.matttax.erica.presentation.model.translate.TextCardState
import com.matttax.erica.presentation.model.translate.TranslatedText
import com.matttax.erica.presentation.model.translate.TranslatedTextCard
import com.matttax.erica.presentation.states.WordsState
import com.matttax.erica.presentation.viewmodels.impl.WordsViewModel
import com.matttax.erica.speechtotext.WordSpeller
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class WordsActivity : AppCompatActivity() {

    private val wordsViewModel: WordsViewModel by viewModels()

    lateinit var binding: ActivityWordsBinding
    private val wordSpeller = WordSpeller(this)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val words = mutableListOf<TranslatedTextCard>()
    private var wordsSelected: Boolean = false

    private lateinit var sets: List<Pair<Long, String>>
    private lateinit var currentSet: WordSet
    private lateinit var selected: Set<Int>

    private lateinit var deleteButton: MaterialButton
    private lateinit var moveButton: MaterialButton

    private val orders = listOf(
        "Last changed first",
        "First changed first",
        "Last answered first",
        "Most accurate first",
        "Least accurate first"
    )

    private fun setData(wordsState: WordsState) {
        sets = wordsState.setsList
            ?.map { it.id to it.name }
            ?.filter { it.first != currentSet.id.toLong() }
            ?: emptyList()
        selected = wordsState.selectedWords ?: emptySet()

        words.withIndex().forEach {
            if (it.value.isSelected && it.index !in selected) {
                words[it.index] = words[it.index].copy(isSelected = false)
            }
        }
        if (words.isNotEmpty() && selected.isNotEmpty()) {
            wordsSelected = true
            selected.forEach {
                words[it] = words[it].copy(isSelected = true)
            }
        } else if (words.isEmpty()) {
            loadWords(wordsState)
        } else {
            wordsSelected = false
        }
        binding.sortByWords.visibility = if (wordsSelected) View.INVISIBLE else View.VISIBLE
        binding.wordsList.adapter?.notifyDataSetChanged()
        updateHead()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wordsViewModel.observeState()
            .flowOn(Dispatchers.Main)
            .onEach { data ->
                runOnUiThread {
                    data?.let { setData(it) }
                }
            }.launchIn(scope)

        currentSet = getSetFromIntents()
        val preferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        binding.sortByWords.apply {
            adapter = ArrayAdapter(
                this@WordsActivity,
                R.layout.params_spinner_item,
                orders
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    words.clear()
                    scope.launch {
                        wordsViewModel.onGetSets()
                        wordsViewModel.onGetWords(getConfigByPosition(position))
                    }
                    preferences.edit().putInt(SHARED_PREFS_POSITION_KEY, position).apply()
                }
            }
            setSelection(preferences.getInt(SHARED_PREFS_POSITION_KEY, 0))
        }
        binding.setName.text = currentSet.name
        binding.setDescr.text = currentSet.description

        if (currentSet.description.isBlank()) {
            binding.setDescr.isVisible = false
        }
        scope.launch {
            wordsViewModel.onGetWords(getConfigByPosition(binding.sortByWords.selectedItemPosition))
        }
        binding.wordsList.layoutManager = LinearLayoutManager(this@WordsActivity)
        binding.wordsList.adapter = WordAdaptor(
            context = this,
            words = words,
            onClick = {
                if (words[it].isSelected) {
                    wordsViewModel.onWordDeselected(it)
                } else {
                    wordsViewModel.onWordSelected(it)
                }
            },
            onSpell = { button, text ->
                button.setColorFilter(Color.argb(255, 255, 165, 0))
                wordSpeller.spell(text) {
                    button.setColorFilter(Color.argb(255, 41, 45, 54))
                }
            },
            onDelete = {
                DeleteDialog(
                    context = this,
                    headerText = "Ready to remove this word?",
                    detailedExplanationText = null
                ) {
                    scope.launch {
                        wordsViewModel.onDelete(it)
                    }
                    words.removeAt(it)
                    binding.wordsList.adapter?.notifyItemRemoved(it)
                }.showDialog()
            },
            onEdit = {
                EditDialog(
                    context = this,
                    headerText = "Edit word",
                    firstField = "Text" to words[it].translatedText.text,
                    secondField = "Translation" to words[it].translatedText.translation,
                    ignoreSecondField = false,
                    onSuccess = {
                        text, translation ->
                        run {
                            val newWord = words[it].translatedText.copy(text = text, translation = translation)
                            scope.launch {
                                wordsViewModel.onDelete(it)
                                wordsViewModel.onAddWord(newWord)
                            }
                            val newCard = words[it].copy(translatedText = newWord)
                            words.removeAt(it)
                            words.add(0, newCard)
                            binding.wordsList.adapter?.notifyItemRemoved(it)
                            binding.wordsList.adapter?.notifyItemInserted(0)
                        }
                    }
                ).showDialog()
            }
        )
        binding.startLearn.setOnClickListener {
            StartLearnDialog(
                this@WordsActivity,
                currentSet.wordsCount,
                currentSet.id
            ).showDialog()
        }
        initButtons()
    }

    private fun getSetFromIntents(): WordSet {
        val setId = intent.getIntExtra(SET_ID_EXTRA_NAME, 1)
        val setName = intent.getStringExtra(SET_NAME_EXTRA_NAME).toString()
        val setDescription = intent.getStringExtra(SET_DESCRIPTION_EXTRA_NAME).toString()
        val wordsCount = intent.getIntExtra(WORD_COUNT_EXTRA_NAME, 0)
        return WordSet(setId, setName, setDescription, wordsCount)
    }

    private fun initButtons() {
        moveButton = getButton(
            layoutParams = LinearLayout.LayoutParams(
                binding.startLearn.width / 3 - 50,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).also { it.setMargins(50, 20, 20, 0) },
            text = "Move",
            color = R.color.blue
        ) {
            MoveDialog(
                context = this,
                sets = sets.map { it.second }
            ) {
                scope.launch {
                    wordsViewModel.onMoveSelected(sets[it].first)
                }
                removeSelected()
            }.showDialog()
            binding.wordsList.adapter?.notifyItemRangeChanged(0, words.size - 1)
            updateHead()
        }

        deleteButton = getButton(
            layoutParams = LinearLayout.LayoutParams(
                binding.startLearn.width / 3 - 50,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).also { it.setMargins(20, 20, 50, 0) },
            text = "Delete",
            color = R.color.crimson
        ) {
            DeleteDialog(
                context = this,
                headerText = "Ready to remove ${selected.size} words?",
                detailedExplanationText = null
            ) {
                scope.launch {
                    wordsViewModel.onDeleteSelected()
                }
                removeSelected()
            }.showDialog()
            binding.wordsList.adapter?.notifyItemRangeChanged(0, words.size - 1)
            updateHead()
        }
    }

    private fun getButton(
        layoutParams: LinearLayout.LayoutParams,
        text: String,
        color: Int,
        onClick: View.OnClickListener
    ) = MaterialButton(
        ContextThemeWrapper(this, R.style.AppTheme_Button), null, R.style.AppTheme_Button
    ).apply {
        setLayoutParams(layoutParams)
        setText(text)
        gravity = Gravity.CENTER
        setBackgroundColor(ContextCompat.getColor(this@WordsActivity, color))
        setOnClickListener(onClick)
    }

    private fun updateHead() {
        binding.startLearn.removeAllViews()
        if (wordsSelected) {
            binding.startLearn.apply {
                addView(moveButton)
                addView(deleteButton)
            }
        } else {
            binding.startLearn.apply {
                addView(binding.startLearnImage)
                addView(binding.setName)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (selected.isEmpty())
            super.onBackPressed()
        else {
            wordsViewModel.onDeselectAll()
            binding.wordsList.adapter!!.notifyDataSetChanged()
            updateHead()
        }
    }

    private fun getConfigByPosition(position: Int): WordGroupConfig {
        return when (position) {
            0 -> WordGroupConfig(
                setId = SetId.One(currentSet.id),
                sorting = WordsSorting.LAST_ADDED_FIRST
            )
            1 -> WordGroupConfig(
                setId = SetId.One(currentSet.id),
                sorting = WordsSorting.FIRST_ADDED_FIRST
            )
            2 -> WordGroupConfig(
                setId = SetId.One(currentSet.id),
                sorting = WordsSorting.RECENTLY_ASKED_FIRST
            )
            3 -> WordGroupConfig(
                setId = SetId.One(currentSet.id),
                sorting = WordsSorting.BEST_ANSWERED_FIRST
            )
            4 -> WordGroupConfig(
                setId = SetId.One(currentSet.id),
                sorting = WordsSorting.WORST_ANSWERED_FIRST
            )
            else -> WordGroupConfig()
        }
    }

    private fun loadWords(wordsState: WordsState) {
        wordsState.words?.let { wordList ->
            for (word in wordList.withIndex()) {
                words.add(
                    TranslatedTextCard(
                        translatedText = TranslatedText(
                            text = word.value.text,
                            translation = word.value.translation,
                            textLanguage = word.value.textLanguage,
                            translationLanguage = word.value.translationLanguage
                        ),
                        isEditable = true,
                        isSelected = false,
                        state = TextCardState.DEFAULT
                    )
                )
            }
        }
    }

    private fun removeSelected() {
        var count = 0
        selected.forEach {
            words.removeAt(it - count)
            count++
        }
    }

    companion object {
        const val SHARED_PREFS_NAME = "ericaPrefs"
        const val SHARED_PREFS_POSITION_KEY = "ORDER_POS"

        const val SET_ID_EXTRA_NAME = "set_id"
        const val SET_NAME_EXTRA_NAME = "set_name"
        const val SET_DESCRIPTION_EXTRA_NAME = "set_description"
        const val WORD_COUNT_EXTRA_NAME = "words_count"

        fun start(context: Context, set: WordSet) {
            val intent = Intent(context, WordsActivity::class.java).apply {
                with(set) {
                    putExtra(SET_ID_EXTRA_NAME, id)
                    putExtra(SET_NAME_EXTRA_NAME, name)
                    putExtra(SET_DESCRIPTION_EXTRA_NAME, description)
                    putExtra(WORD_COUNT_EXTRA_NAME, wordsCount)
                }
            }
            context.startActivity(intent)
        }
    }
}
