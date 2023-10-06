package com.matttax.erica.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.matttax.erica.R
import com.matttax.erica.adaptors.WordAdaptor
import com.matttax.erica.databinding.FragmentWordsBinding
import com.matttax.erica.dialogs.impl.DeleteDialog
import com.matttax.erica.dialogs.impl.EditDialog
import com.matttax.erica.dialogs.impl.MoveDialog
import com.matttax.erica.dialogs.impl.StartLearnDialog
import com.matttax.erica.model.WordSet
import com.matttax.erica.presentation.model.translate.TextCardState
import com.matttax.erica.presentation.model.translate.TranslatedText
import com.matttax.erica.presentation.model.translate.TranslatedTextCard
import com.matttax.erica.presentation.states.WordsState
import com.matttax.erica.presentation.viewmodels.impl.ChoiceViewModel
import com.matttax.erica.speechtotext.WordSpeller
import com.matttax.erica.utils.ChoiceNavigator.Companion.SET_DESCRIPTION_EXTRA_NAME
import com.matttax.erica.utils.ChoiceNavigator.Companion.SET_ID_EXTRA_NAME
import com.matttax.erica.utils.ChoiceNavigator.Companion.SET_NAME_EXTRA_NAME
import com.matttax.erica.utils.ChoiceNavigator.Companion.SHARED_PREFS_NAME
import com.matttax.erica.utils.ChoiceNavigator.Companion.SHARED_PREFS_POSITION_KEY
import com.matttax.erica.utils.ChoiceNavigator.Companion.WORD_COUNT_EXTRA_NAME
import com.matttax.erica.utils.Utils.getConfigByPosition
import com.matttax.erica.utils.Utils.launchSuspend
import com.matttax.erica.utils.getChoiceNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WordsFragment : Fragment() {

    @Inject
    lateinit var wordSpeller: WordSpeller

    private lateinit var preferences: SharedPreferences

    private val choiceViewModel: ChoiceViewModel by activityViewModels()

    private val binding get() = _binding!!
    private var _binding: FragmentWordsBinding? = null

    private lateinit var currentSet: WordSet
    private var words = mutableListOf<TranslatedTextCard>()
    private var wordsSelected: Boolean = false

    private lateinit var deleteButton: MaterialButton
    private lateinit var moveButton: MaterialButton

    private val orders = listOf(
        "Last changed first",
        "First changed first",
        "Last answered first",
        "Most accurate first",
        "Least accurate first"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentSet = getSetFromBundle(savedInstanceState ?: arguments)
        preferences = requireActivity()
            .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordsBinding.inflate(inflater)
        initButtons()
        wordsSelected = choiceViewModel.getSelectedPositions().isNotEmpty()
        getChoiceNavigator().apply {
            notifyWordsSelected(wordsSelected)
            listenBackPressed(viewLifecycleOwner) {
                choiceViewModel.getSelectedPositions().forEach {
                    words[it] = words[it].copy(isSelected = false)
                    binding.wordsList.adapter?.notifyItemChanged(it)
                }
                choiceViewModel.onDeselectAll()
                wordsSelected = false
                updateHead()
                notifyWordsSelected(false)
            }
        }
        choiceViewModel.wordsStateObservable.observeState()
            .flowOn(Dispatchers.Main)
            .distinctUntilChanged()
            .onEach { data ->
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        data?.let { setData(it) }
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sortByWords.apply {
            adapter = ArrayAdapter(
                requireActivity(),
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
                    launchSuspend {
                        choiceViewModel.onGetSets()
                        choiceViewModel.onGetWords(getConfigByPosition(currentSet.id, position))
                    }
                    preferences.edit().putInt(SHARED_PREFS_POSITION_KEY, position).apply()
                }
            }
        }
        binding.setName.text = currentSet.name
        binding.setDescr?.text = currentSet.description

        if (currentSet.description.isBlank()) {
            binding.setDescr?.isVisible = false
        }
        binding.wordsList.layoutManager = LinearLayoutManager(requireActivity())
        binding.wordsList.adapter = WordAdaptor(
            context = requireActivity(),
            words = words,
            onClick = {
                if (words[it].isSelected) {
                    choiceViewModel.onWordDeselected(it)
                } else {
                    choiceViewModel.onWordSelected(it)
                }
                words[it] = words[it].copy(isSelected = !words[it].isSelected)
                binding.wordsList.adapter?.notifyItemChanged(it)
                if (wordsSelected && choiceViewModel.getSelectedPositions().isEmpty()) {
                    wordsSelected = false
                    onSelectionChanged()
                } else if (!wordsSelected && choiceViewModel.getSelectedPositions().isNotEmpty()) {
                    wordsSelected = true
                    onSelectionChanged()
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
                    context = requireActivity(),
                    headerText = "Ready to remove this word?",
                    detailedExplanationText = null
                ) {
                    launchSuspend {
                        choiceViewModel.onDeleteWordAt(it)
                    }
                    words.removeAt(it)
                    binding.wordsList.adapter?.notifyItemRemoved(it)
                }.showDialog()
            },
            onEdit = {
                EditDialog(
                    context = requireActivity(),
                    headerText = "Edit word",
                    firstField = "Text" to words[it].translatedText.text,
                    secondField = "Translation" to words[it].translatedText.translation,
                    ignoreSecondField = false,
                    onSuccess = { text, translation ->
                        run {
                            val newWord = words[it].translatedText.copy(text = text, translation = translation)
                            launchSuspend {
                                choiceViewModel.onDeleteWordAt(it)
                                choiceViewModel.onAddWord(newWord)
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
                requireActivity(),
                currentSet.wordsCount,
                currentSet.id
            ).showDialog()
        }
    }

    private fun onSelectionChanged() {
        getChoiceNavigator().notifyWordsSelected(wordsSelected)
        updateHead()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData(wordsState: WordsState) {
        loadWords(wordsState)
        val selected = choiceViewModel.getSelectedPositions()
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
        } else {
            wordsSelected = false
        }
        binding.wordsList.adapter?.notifyDataSetChanged()
        updateHead()
    }

    private fun loadWords(wordsState: WordsState) {
        words.clear()
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

    private fun updateHead() {
        binding.sortByWords.visibility = if (wordsSelected) View.INVISIBLE else View.VISIBLE
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
            val sets =
                choiceViewModel.setsStateObservable.getCurrentState()?.sets
                    ?.filter { it.id != currentSet.id }
                    ?.take(15)
                    ?.map { it.name to it.id }
            if (sets.isNullOrEmpty())
                return@getButton
            MoveDialog(
                context = requireActivity(),
                sets = sets.map { it.first }
            ) {
                launchSuspend {
                    choiceViewModel.onMoveSelectedWords(sets[it].second)
                }
            }.showDialog()
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
                context = requireActivity(),
                headerText = "Ready to remove ${choiceViewModel.getSelectedPositions().size} words?",
                detailedExplanationText = null
            ) {
                choiceViewModel.getSelectedPositions().forEach {
                    words.removeAt(it)
                    binding.wordsList.adapter?.notifyItemRemoved(it)
                }
                launchSuspend {
                    choiceViewModel.onDeleteSelected()
                }
            }.showDialog()
            updateHead()
        }
    }

    private fun getButton(
        layoutParams: LinearLayout.LayoutParams,
        text: String,
        color: Int,
        onClick: View.OnClickListener
    ) = MaterialButton(
        ContextThemeWrapper(requireContext(), R.style.AppTheme_Button), null,
        R.style.AppTheme_Button
    ).apply {
        setLayoutParams(layoutParams)
        setText(text)
        gravity = Gravity.CENTER
        setBackgroundColor(ContextCompat.getColor(requireActivity(), color))
        setOnClickListener(onClick)
    }

    private fun getSetFromBundle(bundle: Bundle?): WordSet {
        val setId = bundle?.getLong(SET_ID_EXTRA_NAME) ?: 1
        val setName = bundle?.getString(SET_NAME_EXTRA_NAME).toString()
        val setDescription = bundle?.getString(SET_DESCRIPTION_EXTRA_NAME).toString()
        val wordsCount = bundle?.getInt(WORD_COUNT_EXTRA_NAME) ?: 1
        return WordSet(setId, setName, setDescription, wordsCount)
    }

    companion object {

        @JvmStatic
        fun getInstance(set: WordSet): WordsFragment {
            val args = Bundle().apply {
                with(set) {
                    putLong(SET_ID_EXTRA_NAME, id)
                    putString(SET_NAME_EXTRA_NAME, name)
                    putString(SET_DESCRIPTION_EXTRA_NAME, description)
                    putInt(WORD_COUNT_EXTRA_NAME, wordsCount)
                }
            }
            return WordsFragment().also { it.arguments = args }
        }
    }

}
