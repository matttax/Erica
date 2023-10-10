package com.matttax.erica.presentation.viewmodels.impl

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
import com.matttax.erica.presentation.states.TextState
import com.matttax.erica.presentation.states.DataState
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
) : ViewModel(), TranslateInteractor {

    private var languageIn: Language? = null
    private var languageOut: Language? = null
    private var textIn: String? = null
    private var textOut: String? = null

    private val currentSetsFlow = MutableStateFlow<List<SetDomainModel>?>(null)
    private val translationsDataStateFlow = MutableStateFlow<DataState?>(null)
    private val definitionsDataStateFlow = MutableStateFlow<DataState?>(null)
    private val examplesDataStateFlow = MutableStateFlow<DataState?>(null)
    private val isAddableFlow = MutableStateFlow<TextState?>(TextState.TRANSLATABLE)

    var lastTranslatedCache: String? = null
        private set

    var currentSetId: Long? = null
        private set

    val setsObservable = object : StatefulObservable<List<SetDomainModel>?> {
        override fun observeState(): Flow<List<SetDomainModel>?> = currentSetsFlow.asStateFlow()
        override fun getCurrentState(): List<SetDomainModel>? = currentSetsFlow.value
    }

    val translationsObservable = object : StatefulObservable<DataState?> {
        override fun observeState(): Flow<DataState?> = translationsDataStateFlow.asStateFlow()
        override fun getCurrentState(): DataState? = translationsDataStateFlow.value
    }

    val definitionsObservable = object : StatefulObservable<DataState?> {
        override fun observeState(): Flow<DataState?> = definitionsDataStateFlow.asStateFlow()
        override fun getCurrentState(): DataState? = definitionsDataStateFlow.value
    }

    val examplesObservable = object : StatefulObservable<DataState?> {
        override fun observeState(): Flow<DataState?> = examplesDataStateFlow.asStateFlow()
        override fun getCurrentState(): DataState? = examplesDataStateFlow.value
    }

    val isAddableObservable: Flow<TextState?> = isAddableFlow.asStateFlow()

    init {
        currentSetsFlow.launchIn(viewModelScope)
        translationsDataStateFlow.launchIn(viewModelScope)
        definitionsDataStateFlow.launchIn(viewModelScope)
        examplesDataStateFlow.launchIn(viewModelScope)
        isAddableFlow.launchIn(viewModelScope)
    }

    override fun onInputTextLanguageChanged(language: String) {
        val newLanguage = Language(language)
        if (newLanguage == languageIn)
            return
        languageIn = Language(language)
        isAddableFlow.value = TextState.TRANSLATABLE
    }

    override fun onOutputLanguageChanged(language: String) {
        val newLanguage = Language(language)
        if (newLanguage == languageOut)
            return
        languageOut = newLanguage
        isAddableFlow.value = TextState.TRANSLATABLE
    }

    override fun onInputTextChanged(text: String) {
        textIn = text
        if (text == lastTranslatedCache
            && (translationsDataStateFlow.value == DataState.Loading
            || definitionsDataStateFlow.value == DataState.Loading
            || examplesDataStateFlow.value == DataState.Loading)
        ) {
            viewModelScope.launch {
                onTranslateAction()
            }
        }
        isAddableFlow.value = when {
            text == lastTranslatedCache && textOut.isNullOrEmpty().not() -> TextState.ADDABLE
            text == lastTranslatedCache && textOut.isNullOrEmpty() -> TextState.TRANSLATED
            else -> TextState.TRANSLATABLE
        }
    }

    override fun onOutputTextChanged(text: String) {
        textOut = text
        if (textOut.isNullOrEmpty() && isAddableFlow.value == TextState.ADDABLE) {
            isAddableFlow.value = TextState.TRANSLATED
        }
        if (textOut.isNullOrEmpty().not() && isAddableFlow.value == TextState.TRANSLATED) {
            isAddableFlow.value = TextState.ADDABLE
        }
    }

    override suspend fun onTranslateAction() {
        val text = textIn
        val languageIn = languageIn
        val languageOut = languageOut
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
            isAddableFlow.value = TextState.TRANSLATED
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

    override suspend fun onAddAction() {
        addWordUseCase.execute(
            WordDomainModel(
                text = textIn ?: "",
                translation = textOut ?: "",
                textLanguage = languageIn ?: Language("en"),
                translationLanguage = languageOut ?: Language("en"),
                setId = currentSetId ?: 0
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
        currentSetId = currentSetsFlow.value?.getOrNull(position)?.id
    }
}
