package com.matttax.erica.presentation.viewmodels.impl

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matttax.erica.domain.config.SetGroupConfig
import com.matttax.erica.domain.config.SetSorting
import com.matttax.erica.domain.model.Language
import com.matttax.erica.domain.model.SetDomainModel
import com.matttax.erica.domain.model.WordDomainModel
import com.matttax.erica.domain.model.translate.TranslationRequest
import com.matttax.erica.domain.usecases.words.crud.AddWordUseCase
import com.matttax.erica.domain.usecases.sets.crud.GetSetsUseCase
import com.matttax.erica.domain.usecases.translate.GetDefinitionsUseCase
import com.matttax.erica.domain.usecases.translate.GetExamplesUseCase
import com.matttax.erica.domain.usecases.translate.GetTranslationsUseCase
import com.matttax.erica.presentation.states.DataState
import com.matttax.erica.presentation.states.TranslateState
import com.matttax.erica.presentation.viewmodels.StatefulObservable
import com.matttax.erica.presentation.viewmodels.TranslateInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateViewModel @Inject constructor(
    private val getTranslationsUseCase: GetTranslationsUseCase,
    private val getDefinitionsUseCase: GetDefinitionsUseCase,
    private val getExamplesUseCase: GetExamplesUseCase,
    private val getSetsUseCase: GetSetsUseCase,
    private val addWordUseCase: AddWordUseCase
) : ViewModel(), TranslateInteractor, StatefulObservable<TranslateState?> {

    private val translateStateFlow = MutableStateFlow<TranslateState?>(null)

    private val languageInFlow = MutableStateFlow<Language?>(null)
    private val languageOutFlow = MutableStateFlow<Language?>(null)
    private val textInFlow = MutableStateFlow<String?>(null)
    private val textOutFlow = MutableStateFlow<String?>(null)
    private val translationsDataStateFlow = MutableStateFlow<DataState?>(null)
    private val definitionsDataStateFlow = MutableStateFlow<DataState?>(null)
    private val examplesDataStateFlow = MutableStateFlow<DataState?>(null)
    private val isAddableFlow = MutableStateFlow<Boolean?>(false)

    private val currentSetIdFlow = MutableStateFlow<Long?>(null)
    private val currentSetsFlow = MutableStateFlow<List<SetDomainModel>?>(null)

    var lastTranslatedCache: String? = null
        private set

    init {
        combine(
            languageInFlow,
            languageOutFlow,
            textInFlow,
            textOutFlow,
            translationsDataStateFlow,
            definitionsDataStateFlow,
            examplesDataStateFlow,
            currentSetsFlow,
            currentSetIdFlow,
            isAddableFlow
        ) {
            array ->
            TranslateState(
                array[0] as Language?,
                array[1] as Language?,
                array[2] as String?,
                array[3] as String?,
                array[4] as DataState?,
                array[5] as DataState?,
                array[6] as DataState?,
                array[7] as List<SetDomainModel>?,
                array[8] as Long?,
                array[9] as Boolean?
            )
        }.onEach {
            translateStateFlow.value = it
        }.launchIn(viewModelScope)
    }

    override fun observeState(): Flow<TranslateState?> = translateStateFlow.asStateFlow()

    override fun getCurrentState(): TranslateState? = translateStateFlow.value

    override fun onInputTextLanguageChanged(language: String) {
        val newLanguage = Language(language)
        if (newLanguage == languageInFlow.value)
            return
        languageInFlow.value = Language(language)
        isAddableFlow.value = false
    }

    override fun onOutputLanguageChanged(language: String) {
        val newLanguage = Language(language)
        if (newLanguage == languageOutFlow.value)
            return
        languageOutFlow.value = newLanguage
        isAddableFlow.value = false
    }

    override fun onInputTextChanged(text: String) {
        textInFlow.value = text
        if (text == lastTranslatedCache
            && (translationsDataStateFlow.value == DataState.Loading
            || definitionsDataStateFlow.value == DataState.Loading
            || examplesDataStateFlow.value == DataState.Loading)
        ) {
            viewModelScope.launch {
                onTranslateAction()
            }
        }
        isAddableFlow.value = text == lastTranslatedCache
    }

    override fun onOutputTextChanged(text: String) {
        textOutFlow.value = text
    }

    override suspend fun onTranslateAction() {
        val text = textInFlow.value
        val languageIn = languageInFlow.value
        val languageOut = languageOutFlow.value
        lastTranslatedCache = text
        if (text.isNullOrBlank() || languageIn == null || languageOut == null) {
            translationsDataStateFlow.value = DataState.NotFound
            definitionsDataStateFlow.value = DataState.NotFound
            examplesDataStateFlow.value = DataState.NotFound
            return
        }

        translationsDataStateFlow.value = DataState.Loading
        definitionsDataStateFlow.value = DataState.Loading
        examplesDataStateFlow.value = DataState.Loading

        val request = TranslationRequest(
            text = text,
            fromLanguage = languageIn,
            toLanguage = languageOut
        )
        getTranslationsUseCase.execute(request) {
            translationsDataStateFlow.value = when {
                it.size <= 1 && it.firstOrNull()?.endsWith("Error@") ?: true -> DataState.NotFound
                else -> DataState.LoadedInfo(it)
            }
            isAddableFlow.value = true
        }
        getDefinitionsUseCase.execute(request) {
            definitionsDataStateFlow.value = when {
                it.isEmpty() -> DataState.NotFound
                else -> DataState.LoadedInfo(it)
            }
        }
        getExamplesUseCase.execute(request) {
            examplesDataStateFlow.value = when {
                it.size <= 1 && it.firstOrNull()?.text?.endsWith("Error@") ?: true -> DataState.NotFound
                else -> DataState.LoadedInfo(it)
            }
        }
    }

    override fun onTranslationSelected(translation: String) = onOutputTextChanged(translation)

    override fun onClear() {
//        translationsDataStateFlow.value = null
//        definitionsDataStateFlow.value = null
//        examplesDataStateFlow.value = null
    }

    override suspend fun onAddAction() {
        addWordUseCase.execute(
            WordDomainModel(
                text = textInFlow.value ?: "",
                translation = textOutFlow.value ?: "",
                textLanguage = languageInFlow.value ?: Language("en"),
                translationLanguage = languageOutFlow.value ?: Language("en"),
                setId = currentSetIdFlow.value ?: 0
            )
        ) {}
    }

    override suspend fun onGetSetsAction() {
        getSetsUseCase.execute(
            SetGroupConfig(
                sorting = SetSorting.LAST_MODIFIED,
                limit = Int.MAX_VALUE
            )
        ) {
            currentSetsFlow.value = it
        }
    }

    override fun onSetSelected(position: Int) {
        currentSetIdFlow.value = currentSetsFlow.value?.getOrNull(position)?.id
    }
}
