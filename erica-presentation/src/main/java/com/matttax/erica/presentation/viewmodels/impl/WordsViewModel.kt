package com.matttax.erica.presentation.viewmodels.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matttax.erica.domain.config.*
import com.matttax.erica.domain.model.SetDomainModel
import com.matttax.erica.domain.model.WordDomainModel
import com.matttax.erica.domain.usecases.sets.crud.GetSetsUseCase
import com.matttax.erica.domain.usecases.words.crud.AddWordUseCase
import com.matttax.erica.domain.usecases.words.crud.DeleteWordsUseCase
import com.matttax.erica.domain.usecases.words.crud.GetWordsUseCase
import com.matttax.erica.domain.usecases.words.crud.MoveWordsUseCase
import com.matttax.erica.presentation.model.translate.TranslatedText
import com.matttax.erica.presentation.states.WordsState
import com.matttax.erica.presentation.viewmodels.StatefulObservable
import com.matttax.erica.presentation.viewmodels.WordsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordsViewModel @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase,
    private val moveWordsUseCase: MoveWordsUseCase,
    private val deleteWordsUseCase: DeleteWordsUseCase,
    private val getSetsUseCase: GetSetsUseCase,
    private val addWordUseCase: AddWordUseCase
) : ViewModel(), WordsInteractor, StatefulObservable<WordsState?> {

    private val wordsStateFlow = MutableStateFlow<WordsState?>(null)
    private val wordsListFlow = MutableStateFlow<List<WordDomainModel>?>(null)
    private val selectedWordsPositionsListFlow = MutableStateFlow<Set<Int>?>(emptySet())
    private val setsStateFlow = MutableStateFlow<List<SetDomainModel>?>(null)

    private var setId: Long = -1

    private lateinit var defaultConfig: WordGroupConfig

    init {
        combine(
            wordsListFlow,
            selectedWordsPositionsListFlow,
            setsStateFlow
        ) {
            words, selectedWords, sets -> WordsState(words, selectedWords, sets)
        }.onEach {
            wordsStateFlow.value = it
        }.launchIn(viewModelScope)
    }

    override fun observeState(): Flow<WordsState?> = wordsStateFlow.asStateFlow()

    override fun getCurrentState(): WordsState? = wordsStateFlow.value

    override fun onWordSelected(position: Int) {
        selectedWordsPositionsListFlow.value = selectedWordsPositionsListFlow.value?.plus(position)
    }

    override fun onWordDeselected(position: Int) {
        selectedWordsPositionsListFlow.value = selectedWordsPositionsListFlow.value?.minus(position)
    }

    override suspend fun onMoveSelected(toId: Long) {
        if (selectedWordsPositionsListFlow.value.isNullOrEmpty()) {
            return
        }
        val positions = selectedWordsPositionsListFlow.value ?: emptyList()
        val ids = mutableListOf<Long>()
        wordsListFlow.value?.let { list ->
            for (word in list.withIndex()) {
                if (word.index in positions) {
                    word.value.id?.let { ids.add(it) }
                }
            }
        }
        moveWordsUseCase.execute(
            MoveWordsRequest(
                idFrom = setId.toInt(),
                idTo = toId,
                words = ids
            )
        ) {
            if (it) {
                onDeselectAll()
            }
        }
    }

    override suspend fun onDelete(position: Int) {
        wordsListFlow.value?.get(position)?.id?.let {
            deleteWordsUseCase.execute(it to setId) {}
            onWordDeselected(position)
        }
    }

    override suspend fun onDeleteSelected() {
        selectedWordsPositionsListFlow.value?.forEach {
            onDelete(it)
        }
    }

    override suspend fun onGetSets() {
        getSetsUseCase.execute(
            SetGroupConfig(
                sorting = SetSorting.LAST_MODIFIED
            )
        ) {
            setsStateFlow.value = it
        }
    }

    override fun onDeselectAll() {
        selectedWordsPositionsListFlow.value = emptySet()
    }

    override suspend fun onGetWords(wordGroupConfig: WordGroupConfig) {
        defaultConfig = wordGroupConfig
        this.setId = (wordGroupConfig.setId as? SetId.One)?.id?.toLong() ?: -1L
        getWordsUseCase.execute(wordGroupConfig) {
            wordsListFlow.value = it
        }
        selectedWordsPositionsListFlow.value = emptySet()
    }

    override suspend fun onAddWord(translatedText: TranslatedText) {
        addWordUseCase.execute(
            WordDomainModel(
                text = translatedText.text,
                translation = translatedText.translation,
                textLanguage = translatedText.textLanguage,
                translationLanguage = translatedText.translationLanguage,
                setId = setId
            )
        ) {
            viewModelScope.launch {
                onGetWords(defaultConfig)
            }
        }
    }

}