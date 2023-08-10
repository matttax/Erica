package com.matttax.erica.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.*
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
import com.matttax.erica.presentation.viewmodels.TranslateViewModel
import com.matttax.erica.utils.LanguageUtils.Companion.getLanguageCode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var translateViewModel: TranslateViewModel
    lateinit var binding: ActivityMainBinding

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    private var lastTranslateState: TranslateState? = null

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
                    updateAdaptor()
                }
            }.launchIn(scope)

        val preferences = getSharedPreferences("ericaPrefs", Context.MODE_PRIVATE)

        binding.setSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                translateViewModel.onSetSelected(position)
            }
        }

        binding.toSetsButton.setOnClickListener {
            val setsIntent = Intent(this@MainActivity, SetsActivity::class.java)
            startActivity(setsIntent)
        }

        binding.termTextField.doOnTextChanged { text, _, _, _ ->
            job?.cancel()
            showTranslateButton()
            translateViewModel.onInputTextChanged(text.toString())
            translateViewModel.onClear()
        }
        binding.defTextField.doOnTextChanged { text, _, _, _ ->
            translateViewModel.onOutputTextChanged(text.toString())
        }

        binding.addWord.setOnClickListener {
            if (binding.addWord.text.toString().lowercase() == "translate") {
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
                binding.termTextField.text = SpannableStringBuilder("")
                translateViewModel.onOutputTextChanged("")
                translateViewModel.onClear()
                showTranslateButton()
            }
        }

        binding.tabs.addOnTabSelectedListener(
            object: TabLayout.OnTabSelectedListener {
                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (binding.termTextField.text.toString() != "" && tab != null){
                        updateAdaptor()
                    }
                }
            }
        )

        binding.fromSpinner.apply {
            adapter = ArrayAdapter(this@MainActivity, R.layout.sets_spinner_item, languages)
            setSelection(preferences.getInt("FROM", 0))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    showTranslateButton()
                    translateViewModel.onClear()
                    translateViewModel.onInputTextLanguageChanged(getLanguageCode(binding.fromSpinner.selectedItem.toString()))
                }
            }
        }
        binding.toSpinner.apply {
            adapter = ArrayAdapter(this@MainActivity, R.layout.sets_spinner_item, languages)
            setSelection(preferences.getInt("TO", 1))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    showTranslateButton()
                    translateViewModel.onClear()
                    translateViewModel.onOutputLanguageChanged(getLanguageCode(binding.toSpinner.selectedItem.toString()))
                }
            }
        }
        translateViewModel.onInputTextLanguageChanged(getLanguageCode(binding.fromSpinner.selectedItem.toString()))
        translateViewModel.onOutputLanguageChanged(getLanguageCode(binding.toSpinner.selectedItem.toString()))
        binding.swapButton.setOnClickListener {
            val fr = binding.fromSpinner.selectedItemPosition
            binding.fromSpinner.setSelection(binding.toSpinner.selectedItemPosition)
            binding.toSpinner.setSelection(fr)
        }
    }

    override fun onResume() {
        super.onResume()
        scope.launch {
            translateViewModel.onGetSetsAction()
        }
    }

    override fun onStop() {
        super.onStop()
        val preferences = getSharedPreferences("ericaPrefs", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.apply {
            putInt("FROM", binding.fromSpinner.selectedItemPosition)
            putInt("TO", binding.toSpinner.selectedItemPosition)
        }.apply()
    }

    private fun setData(translateState: TranslateState) {
        lastTranslateState = translateState
        binding.defTextField.setText(translateState.textOut, TextView.BufferType.EDITABLE)
        binding.setSpinner.adapter = ArrayAdapter(
            this, R.layout.sets_spinner_item, translateState.sets?.map { it.name } ?: emptyList()
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun translateClicked() {
        job?.cancel()
        job = scope.launch(Dispatchers.Main) {
            binding.translations.layoutManager = FlexboxLayoutManager(this@MainActivity)
            translateViewModel.onTranslateAction()
        }
    }

    private fun showAddButtons() {
        binding.addWord.textSize = 15F
        binding.addWord.text = "add"
        binding.addWord.background.setTint(ContextCompat.getColor(this@MainActivity, R.color.green))
        binding.dismissWord.isInvisible = false
    }

    private fun showTranslateButton() {
        if (binding.addWord.text.toString().lowercase() == "add") {
            binding.addWord.textSize = 15F
            binding.addWord.text = "translate"
            binding.addWord.background.setTint(ContextCompat.getColor(this, R.color.blue))
            binding.dismissWord.isInvisible = true
        }
    }

    private fun updateAdaptor() {
        binding.translationsProgressBar.isVisible = false
        binding.translations.adapter = null
        when (binding.tabs.selectedTabPosition) {
            0 -> {
                lastTranslateState?.let {
                    when (it.translations) {
                        is DataState.LoadedInfo<*> -> {
                            showAddButtons()
                            binding.translations.layoutManager =
                                FlexboxLayoutManager(this@MainActivity)
                            binding.translations.adapter = TranslationAdaptor(
                                this@MainActivity,
                                (it.translations as DataState.LoadedInfo<*>).info as? List<String>
                                    ?: emptyList()
                            ) {
                                text -> translateViewModel.onOutputTextChanged(text.toString())
                            }
                        }
                        is DataState.Loading -> {
                            showTranslateButton()
                            binding.translationsProgressBar.isVisible = true
                        }
                        else -> {}
                    }
                }
            }
            1 -> {
                lastTranslateState?.let {
                    when(it.definitions) {
                        is DataState.LoadedInfo<*> -> {
                            binding.translations.layoutManager = LinearLayoutManager(this@MainActivity)
                            binding.translations.adapter = PartOfSpeechAdaptor(
                                this@MainActivity,
                                (it.definitions as DataState.LoadedInfo<*>).info as? List<DictionaryDefinition> ?: emptyList()
                            )
                        }
                        is DataState.Loading -> {
                            binding.translationsProgressBar.isVisible = true
                        }
                        else -> {}
                    }
                }
            }
            2 -> {
                lastTranslateState?.let {
                    when(it.examples) {
                        is DataState.LoadedInfo<*> -> {
                            binding.translations.layoutManager = LinearLayoutManager(this@MainActivity)
                            binding.translations.adapter = WordAdaptor(
                                this@MainActivity,
                                ((it.examples as DataState.LoadedInfo<*>).info as? List<UsageExample>)?.map {
                                    example -> TranslatedTextCard.fromUsageExample(example)
                                } ?: emptyList()
                            )
                        }
                        is DataState.Loading -> {
                            binding.translationsProgressBar.isVisible = true
                        }
                        else -> {}
                    }

                }
            }
        }
    }
}
