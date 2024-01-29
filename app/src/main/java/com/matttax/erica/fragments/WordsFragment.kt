package com.matttax.erica.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.matttax.erica.R
import com.matttax.erica.adaptors.WordAdaptor
import com.matttax.erica.adaptors.callback.WordCallback
import com.matttax.erica.adaptors.listeners.OnItemClickedListener
import com.matttax.erica.adaptors.listeners.SearchFieldListener.Companion.setSearchListener
import com.matttax.erica.databinding.FragmentWordsBinding
import com.matttax.erica.dialogs.selection.DeleteDialog
import com.matttax.erica.dialogs.selection.EditDialog
import com.matttax.erica.dialogs.selection.MoveDialog
import com.matttax.erica.dialogs.selection.StartLearnDialog
import com.matttax.erica.model.WordSet
import com.matttax.erica.presentation.model.translate.TextCardState
import com.matttax.erica.presentation.model.translate.TranslatedText
import com.matttax.erica.presentation.model.translate.TranslatedTextCard
import com.matttax.erica.presentation.states.WordsState
import com.matttax.erica.presentation.viewmodels.impl.ChoiceViewModel
import com.matttax.erica.speechtotext.WordSpeller
import com.matttax.erica.utils.AppSettings
import com.matttax.erica.utils.ChoiceNavigator.Companion.SET_DESCRIPTION_EXTRA_NAME
import com.matttax.erica.utils.ChoiceNavigator.Companion.SET_ID_EXTRA_NAME
import com.matttax.erica.utils.ChoiceNavigator.Companion.SET_NAME_EXTRA_NAME
import com.matttax.erica.utils.ChoiceNavigator.Companion.WORD_COUNT_EXTRA_NAME
import com.matttax.erica.utils.Utils.getConfigByPosition
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

    @Inject
    lateinit var appSettings: AppSettings

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

    private val wordCallback by lazy {
        object : WordCallback {
            override fun onClick(position: Int) {
                if (words[position].isSelected) {
                    choiceViewModel.onWordDeselected(position)
                } else {
                    choiceViewModel.onWordSelected(position)
                }
                words[position] = words[position].copy(isSelected = !words[position].isSelected)
                binding.wordsList.adapter?.notifyItemChanged(position)
                if (wordsSelected && choiceViewModel.getSelectedPositions().isEmpty()) {
                    wordsSelected = false
                    onSelectionChanged()
                } else if (!wordsSelected && choiceViewModel.getSelectedPositions().isNotEmpty()) {
                    wordsSelected = true
                    onSelectionChanged()
                }
            }

            override fun onEditClick(position: Int) {
                EditDialog(
                    context = requireActivity(),
                    headerText = "Edit word",
                    firstField = "Text" to words[position].translatedText.text,
                    secondField = "Translation" to words[position].translatedText.translation,
                    ignoreSecondField = false,
                    onSuccess = { text, translation ->
                        run {
                            val newWord = words[position].translatedText.copy(
                                text = text,
                                translation = translation
                            )
                            lifecycleScope.launch {
                                choiceViewModel.onDeleteWordAt(position)
                                choiceViewModel.onAddWord(newWord)
                            }
                            val newCard = words[position].copy(translatedText = newWord)
                            words.removeAt(position)
                            words.add(0, newCard)
                        }
                    }
                ).showDialog()
            }

            override fun onDeleteClick(position: Int) {
                DeleteDialog(
                    context = requireActivity(),
                    headerText = "Ready to remove this word?",
                    detailedExplanationText = null
                ) {
                    lifecycleScope.launch {
                        choiceViewModel.onDeleteWordAt(position)
                    }
                    words.removeAt(position)
                    binding.wordsList.adapter?.notifyItemRemoved(position)
                }.showDialog()
            }

            override fun onSpell(icon: ImageView, text: TranslatedText) {
                icon.setColorFilter(Color.argb(255, 255, 165, 0))
                wordSpeller.spell(text) {
                    icon.setColorFilter(Color.argb(255, 41, 45, 54))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentSet = getSetFromBundle(savedInstanceState ?: arguments)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordsBinding.inflate(inflater)
        initButtons()
        binding.startLearn.startAnimation(AnimationUtils.loadAnimation(context, R.anim.forward))
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
        binding.searchField?.setSearchListener {
            choiceViewModel.filterWordsByQuery(it)
        }
        binding.sortByWords.apply {
            adapter = ArrayAdapter(
                requireActivity(),
                R.layout.params_spinner_item,
                orders
            )
            setSelection(appSettings.wordsOrderId)
            onItemSelectedListener = OnItemClickedListener { position ->
                words.clear()
                lifecycleScope.launch {
                    choiceViewModel.onGetSets()
                    choiceViewModel.onGetWords(getConfigByPosition(currentSet.id, position))
                }
                appSettings.wordsOrderId = position
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
            callback = wordCallback
        )
        binding.wordsList.startAnimation(AnimationUtils.loadAnimation(context, R.anim.harsh_slide))

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
        binding.wordsList.startAnimation(AnimationUtils.loadAnimation(context, R.anim.harsh_slide))
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
        if (wordsSelected) {
            binding.searchField?.let {
                it.animate()
                    .translationY(-180f)
                    .alpha(0f)
                    .setDuration(400)
                    .withEndAction {
                        it.visibility = View.GONE
                    }.start()
            }
            binding.sortByWords.visibility = View.INVISIBLE
        } else {
            binding.sortByWords.isVisible = true
            binding.searchField?.apply {
                isVisible = true
                animate().translationY(0f).alpha(1f).setDuration(500).start()
            }
        }
        binding.startLearn.removeAllViews()
        if (wordsSelected) {
            binding.startLearn.apply {
                addView(
                    moveButton.also {
                        it.startAnimation(
                            AnimationUtils.loadAnimation(context, R.anim.light_slide).apply { duration = 100 }
                        )
                    }
                )
                addView(
                    deleteButton.also {
                        it.startAnimation(
                            AnimationUtils.loadAnimation(context, R.anim.light_slide).apply { duration = 100 }
                        )
                    }
                )
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
                lifecycleScope.launch {
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
                lifecycleScope.launch {
                    choiceViewModel.onDeleteSelected()
                }
                binding.wordsList.adapter?.notifyDataSetChanged()
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
