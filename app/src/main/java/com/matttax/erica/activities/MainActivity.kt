package com.matttax.erica.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.tabs.TabLayout
import com.matttax.erica.R
import com.matttax.erica.adaptors.PartOfSpeechAdaptor
import com.matttax.erica.adaptors.TranslationAdaptor
import com.matttax.erica.adaptors.WordAdaptor
import com.matttax.erica.databinding.ActivityMainBinding
import com.matttax.erica.domain.model.translate.DictionaryDefinition
import com.matttax.erica.domain.model.translate.UsageExample
import com.matttax.erica.presentation.model.translate.TranslatedTextCard
import com.matttax.erica.presentation.states.DataState
import com.matttax.erica.presentation.states.TranslateState
import com.matttax.erica.presentation.viewmodels.impl.TranslateViewModel
import com.matttax.erica.speechtotext.WordSpeller
import com.matttax.erica.utils.Utils.getLanguageCode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val translateViewModel: TranslateViewModel by viewModels()

    @Inject
    lateinit var wordSpeller: WordSpeller

    lateinit var binding: ActivityMainBinding

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    private var lastTranslateState: TranslateState? = null

    private var currentSet = -1L
    private var currentPosition = 0

    private val languages = listOf("English", "Russian", "German", "French", "Spanish", "Italian")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        translateViewModel.observeState()
            .flowOn(Dispatchers.Main)
            .onEach { data ->
                runOnUiThread {
                    data?.let { setData(it) }
                    updateAdaptor(translateViewModel.lastTranslatedCache == binding.termTextField.text?.toString())
                }
            }.launchIn(scope)

        val preferences = getSharedPreferences(SharedPrefs.NAME, Context.MODE_PRIVATE)

        binding.setSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentPosition = position
                translateViewModel.onSetSelected(position)
            }
        }

        binding.toSetsButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, ChoiceActivity::class.java))
        }

        binding.termTextField.apply {
            doOnTextChanged { text, _, _, _ ->
                job?.cancel()
                with(translateViewModel) {
                    onInputTextChanged(text.toString())
                    onClear()
                }
            }
            setOnKeyListener { _, i, keyEvent ->
                if ((keyEvent.action == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    binding.addWord.callOnClick()
                    return@setOnKeyListener true
                }
                false
            }
        }
        binding.defTextField.doOnTextChanged { text, _, _, _ ->
            translateViewModel.onOutputTextChanged(text.toString())
        }

        binding.addWord.setOnClickListener {
            if (binding.addWord.text.toString().lowercase() == Buttons.TRANSLATE_BUTTON_TEXT) {
                binding.translations.adapter = null
                translateClicked()
            } else {
                scope.launch {
                    translateViewModel.onAddAction()
                }
                binding.dismissWord.callOnClick()
            }
        }
        binding.dismissWord.apply {
            isInvisible = true
            setOnClickListener {
                job?.cancel()
                binding.termTextField.text = SpannableStringBuilder("")
                binding.defTextField.text = SpannableStringBuilder("")
                translateViewModel.onClear()
            }
        }

        binding.tabs.apply {
            addOnTabSelectedListener(
                object: TabLayout.OnTabSelectedListener {
                    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                    override fun onTabReselected(tab: TabLayout.Tab?) = Unit
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        updateAdaptor(binding.termTextField.text?.toString() == translateViewModel.lastTranslatedCache)
                    }
                }
            )
            getTabAt(
                savedInstanceState?.getInt(SavedState.SELECTED_TAB_KEY) ?: 0
            )?.select()
        }

        binding.fromSpinner.apply {
            adapter = ArrayAdapter(this@MainActivity, R.layout.sets_spinner_item, languages)
            setSelection(preferences.getInt(SharedPrefs.FROM_LANGUAGE_KEY, 0))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
                = with(translateViewModel) {
                    onClear()
                    onInputTextLanguageChanged(getLanguageCode(binding.fromSpinner.selectedItem.toString()))
                }
            }
        }
        binding.toSpinner.apply {
            adapter = ArrayAdapter(this@MainActivity, R.layout.sets_spinner_item, languages)
            setSelection(preferences.getInt(SharedPrefs.TO_LANGUAGE_KEY, 1))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
                = with(translateViewModel) {
                    onClear()
                    onOutputLanguageChanged(getLanguageCode(binding.toSpinner.selectedItem.toString()))
                }
            }
        }
        translateViewModel.onInputTextLanguageChanged(getLanguageCode(binding.fromSpinner.selectedItem.toString()))
        translateViewModel.onOutputLanguageChanged(getLanguageCode(binding.toSpinner.selectedItem.toString()))
        binding.swapButton.setOnClickListener {
            val oldPosition = binding.fromSpinner.selectedItemPosition
            binding.fromSpinner.setSelection(binding.toSpinner.selectedItemPosition)
            binding.toSpinner.setSelection(oldPosition)
        }
        binding.startLearn.setOnClickListener {
            LearnActivity.start(this, currentSet.toInt())
        }
    }

    override fun onStart() {
        super.onStart()
        scope.launch {
            translateViewModel.onGetSetsAction()
        }
    }

    override fun onStop() {
        super.onStop()
        val preferences = getSharedPreferences(SharedPrefs.NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.apply {
            putInt(SharedPrefs.FROM_LANGUAGE_KEY, binding.fromSpinner.selectedItemPosition)
            putInt(SharedPrefs.TO_LANGUAGE_KEY, binding.toSpinner.selectedItemPosition)
        }.apply()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SavedState.SELECTED_TAB_KEY, binding.tabs.selectedTabPosition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
        lastTranslateState?.let { setData(it) }
    }

    private fun setData(translateState: TranslateState) {
        currentSet = translateState.currentSetId ?: -1
        lastTranslateState = translateState
        binding.setSpinner.adapter = ArrayAdapter(
            this, R.layout.sets_spinner_item, translateState.sets?.map { it.name } ?: emptyList()
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.setSpinner.setSelection(currentPosition)
        if (translateState.isAddable == true) {
            showAddButtons(translateState.textOut.isNullOrBlank().not())
        } else {
            showTranslateButton()
        }
    }

    private fun translateClicked() {
        job?.cancel()
        job = scope.launch(Dispatchers.Main) {
            translateViewModel.onTranslateAction()
        }
    }

    private fun showAddButtons(clickable: Boolean = true) {
        binding.addWord.apply {
            textSize = 15F
            text = Buttons.ADD_BUTTON_TEXT
            isClickable = clickable
            background.setTint(
                ContextCompat.getColor(this@MainActivity, R.color.green)
            )
            alpha = if (clickable) 1F else 0.5F
        }
        binding.dismissWord.isInvisible = false
    }

    private fun showTranslateButton() {
        binding.addWord.apply {
            textSize = 15F
            text = Buttons.TRANSLATE_BUTTON_TEXT
            background.setTint(ContextCompat.getColor(this@MainActivity, R.color.blue))
            isClickable = true
            alpha = 1F
        }
        binding.dismissWord.isInvisible = true
    }

    private fun updateAdaptor(isVisible: Boolean = true) {
        hideImages()
        binding.translations.adapter = null
        if (!isVisible) return
        when (binding.tabs.selectedTabPosition) {
            0 -> {
                lastTranslateState?.let {
                    setDataState(it.translations) {
                        setTranslations(
                            list = (it.translations as? DataState.LoadedInfo<*>)?.info as? List<String>
                        )
                    }
                }
            }
            1 -> {
                lastTranslateState?.let {
                    setDataState(it.definitions) {
                        setDefinitions(
                            list = (it.definitions as? DataState.LoadedInfo<*>)?.info as? List<DictionaryDefinition>
                        )
                    }
                }
            }
            2 -> {
                lastTranslateState?.let {
                    setDataState(it.examples) {
                        setExamples(
                            list = (it.examples as? DataState.LoadedInfo<*>)?.info as? List<UsageExample>
                        )
                    }
                }
            }
        }
    }

    private fun hideImages() {
        binding.apply {
            translationsProgressBar.isVisible = false
            noInternetImage.isVisible = false
            notFoundImage.isVisible = false
        }
    }

    private fun setTranslations(list: List<String>?) {
        binding.translations.layoutManager = FlexboxLayoutManager(this@MainActivity)
        binding.translations.adapter = TranslationAdaptor(
            this@MainActivity,
            list ?: emptyList()
        ) { text ->
            binding.defTextField.setText(text)
        }
    }

    private fun setDefinitions(list: List<DictionaryDefinition>?) {
        binding.translations.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.translations.adapter = PartOfSpeechAdaptor(
            this@MainActivity,
            list ?: emptyList()
        )
    }

    private fun setExamples(list: List<UsageExample>?) {
        binding.translations.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.translations.adapter = WordAdaptor(
            context = this@MainActivity,
            words = list?.map {
                example -> TranslatedTextCard.fromUsageExample(example)
            } ?: emptyList(),
            onSpell = { button, text ->
                button.setColorFilter(Color.argb(255, 255, 165, 0))
                wordSpeller.spell(text) {
                    button.setColorFilter(Color.argb(255, 41, 45, 54))
                }
            }
        )
    }

    private fun setDataState(dataState: DataState?, onLoaded: () -> Unit) {
        when(dataState) {
            is DataState.LoadedInfo<*> -> {
                onLoaded()
            }
            is DataState.Loading -> {
                binding.translationsProgressBar.isVisible = true
            }
            is DataState.NotFound -> {
                binding.notFoundImage.isVisible = true
            }
            is DataState.NoInternet -> {
                binding.noInternetImage.isVisible = true
            }
            else -> {
                translateViewModel.onOutputTextChanged("")
            }
        }
    }

    companion object {
        object SharedPrefs {
            const val NAME = "ericaPrefs"
            const val FROM_LANGUAGE_KEY = "FROM"
            const val TO_LANGUAGE_KEY = "TO"
        }
        object Buttons {
            const val ADD_BUTTON_TEXT = "add"
            const val TRANSLATE_BUTTON_TEXT = "translate"
        }
        object SavedState {
            const val SELECTED_TAB_KEY = "SELECTED_TAB"
        }
    }
}
