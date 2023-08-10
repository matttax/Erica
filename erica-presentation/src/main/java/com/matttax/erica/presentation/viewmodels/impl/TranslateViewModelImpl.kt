package com.matttax.erica.presentation.viewmodels.impl

import android.util.Log
import androidx.lifecycle.ViewModel
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
import com.matttax.erica.presentation.viewmodels.TranslateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class TranslateViewModelImpl @Inject constructor(
    private val getTranslationsUseCase: GetTranslationsUseCase,
    private val getDefinitionsUseCase: GetDefinitionsUseCase,
    private val getExamplesUseCase: GetExamplesUseCase,
    private val getSetsUseCase: GetSetsUseCase,
    private val addWordUseCase: AddWordUseCase
) : ViewModel(), TranslateViewModel {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val translateStateFlow = MutableStateFlow<TranslateState?>(null)

    private val languageInFlow = MutableStateFlow<Language?>(null)
    private val languageOutFlow = MutableStateFlow<Language?>(null)
    private val textInFlow = MutableStateFlow<String?>(null)
    private val textOutFlow = MutableStateFlow<String?>(null)
    private val translationsDataStateFlow = MutableStateFlow<DataState?>(null)
    private val definitionsDataStateFlow = MutableStateFlow<DataState?>(null)
    private val examplesDataStateFlow = MutableStateFlow<DataState?>(null)

    private val currentSetIdFlow = MutableStateFlow<Long?>(null)
    private val currentSetsFlow = MutableStateFlow<List<SetDomainModel>?>(null)

    init {
        combine(
            languageInFlow,
            languageOutFlow,
            textInFlow,
            textOutFlow,
            translationsDataStateFlow,
            definitionsDataStateFlow,
            examplesDataStateFlow,
            currentSetsFlow
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
                array[7] as List<SetDomainModel>?
            )
        }.onEach {
            translateStateFlow.value = it
        }.launchIn(scope)
    }

    override fun observeState(): Flow<TranslateState?> = translateStateFlow.asStateFlow()

    override fun onInputTextLanguageChanged(language: String) {
        languageInFlow.value = Language(language)
    }

    override fun onOutputLanguageChanged(language: String) {
        languageOutFlow.value = Language(language)
    }

    override fun onInputTextChanged(text: String) {
        textInFlow.value = text
    }

    override fun onOutputTextChanged(text: String) {
        textOutFlow.value = text
    }

    override suspend fun onTranslateAction() {
        val text = textInFlow.value
        val languageIn = languageInFlow.value
        val languageOut = languageOutFlow.value
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
            translationsDataStateFlow.value = DataState.LoadedInfo(it)
        }
        getDefinitionsUseCase.execute(request) {
            definitionsDataStateFlow.value = DataState.LoadedInfo(it)
        }
        getExamplesUseCase.execute(request) {
            examplesDataStateFlow.value = DataState.LoadedInfo(it)
        }
    }

    override fun onTranslationSelected(translation: String) = onOutputTextChanged(translation)

    override fun onClear() {
        translationsDataStateFlow.value = DataState.Loading
        definitionsDataStateFlow.value = DataState.Loading
        examplesDataStateFlow.value = DataState.Loading
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
                sorting = SetSorting.LAST_ADDED,
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