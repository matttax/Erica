package com.matttax.erica.presentation.viewmodels.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matttax.erica.domain.config.*
import com.matttax.erica.domain.model.SetDomainModel
import com.matttax.erica.domain.model.WordDomainModel
import com.matttax.erica.domain.usecases.sets.crud.AddSetUseCase
import com.matttax.erica.domain.usecases.sets.crud.DeleteSetUseCase
import com.matttax.erica.domain.usecases.sets.crud.GetSetsUseCase
import com.matttax.erica.domain.usecases.sets.crud.UpdateSetUseCase
import com.matttax.erica.domain.usecases.words.crud.AddWordUseCase
import com.matttax.erica.domain.usecases.words.crud.DeleteWordsUseCase
import com.matttax.erica.domain.usecases.words.crud.GetWordsUseCase
import com.matttax.erica.domain.usecases.words.crud.MoveWordsUseCase
import com.matttax.erica.presentation.model.translate.TranslatedText
import com.matttax.erica.presentation.states.SetsState
import com.matttax.erica.presentation.states.WordsState
import com.matttax.erica.presentation.viewmodels.SetsInteractor
import com.matttax.erica.presentation.viewmodels.StatefulObservable
import com.matttax.erica.presentation.viewmodels.WordsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChoiceViewModel @Inject constructor(
    private val getSetsUseCase: GetSetsUseCase,
    private val addSetUseCase: AddSetUseCase,
    private val deleteSetUseCase: DeleteSetUseCase,
    private val updateSetUseCase: UpdateSetUseCase,
    private val getWordsUseCase: GetWordsUseCase,
    private val moveWordsUseCase: MoveWordsUseCase,
    private val deleteWordsUseCase: DeleteWordsUseCase,
    private val addWordUseCase: AddWordUseCase
) : ViewModel(), SetsInteractor, WordsInteractor {

    private val setsStateFlow = MutableStateFlow<SetsState?>(null)
    private val wordsStateFlow = MutableStateFlow<WordsState?>(null)

    private val setsFlow = MutableStateFlow<List<SetDomainModel>?>(null)
    private val wordsListFlow = MutableStateFlow<List<WordDomainModel>?>(null)
    private val selectedWordsPositions = mutableSetOf<Int>()

    private var setId: Long = -1
    private var defaultWordGroupConfig = WordGroupConfig()
    private var filteredWordList = emptyList<WordDomainModel>()
    private var fullWordList = emptyList<WordDomainModel>()

    val wordsStateObservable = object : StatefulObservable<WordsState?> {
        override fun observeState(): Flow<WordsState?> = wordsStateFlow.asStateFlow()
        override fun getCurrentState(): WordsState? = wordsStateFlow.value
    }

    val setsStateObservable = object : StatefulObservable<SetsState?> {
        override fun observeState(): Flow<SetsState?> = setsStateFlow.asStateFlow()
        override fun getCurrentState(): SetsState? = setsStateFlow.value
    }

    init {
        setsFlow.onEach {
            setsStateFlow.value = SetsState(it)
        }.launchIn(viewModelScope)

        wordsListFlow.onEach {
            wordsStateFlow.value = WordsState(it)
        }.launchIn(viewModelScope)
    }

    override suspend fun onGetSets() {
        getSetsUseCase.execute(
            SetGroupConfig(
                sorting = SetSorting.LAST_MODIFIED,
                limit = Int.MAX_VALUE
            )
        ) {
            setsFlow.value = it
        }
    }

    override suspend fun onAddSetAction(name: String, description: String) {
        addSetUseCase.execute(name to description) {
            if (it != -1L) {
                val newList = mutableListOf(SetDomainModel(it, name, description))
                setsFlow.value?.let { oldList -> newList.addAll(oldList) }
                setsFlow.value = newList
            }
        }
    }

    override suspend fun onUpdateSetAction(id: Long, name: String, description: String) {
        updateSetUseCase.execute(SetDomainModel(id, name, description)) {
            viewModelScope.launch { onGetSets() }
        }
    }

    override fun onWordSelected(position: Int) {
        selectedWordsPositions.add(position)
    }

    override fun onWordDeselected(position: Int) {
        selectedWordsPositions.remove(position)
    }

    override fun onDeselectAll() {
        selectedWordsPositions.clear()
    }

    override fun filterWordsByQuery(query: String) {
        filteredWordList = fullWordList.filter { word ->
            if (query.isNotEmpty()) {
                word.text.indifferent().contains(query.indifferent())
                        || word.translation.indifferent().contains(query.indifferent())
            } else true
        }
        wordsListFlow.value = filteredWordList
    }

    override fun filterSetsByQuery(query: String) {
        setsStateFlow.update {
            it?.copy(filter = query.indifferent())
        }
    }

    override suspend fun onGetWords(wordGroupConfig: WordGroupConfig) {
        if (defaultWordGroupConfig != wordGroupConfig) {
            onDeselectAll()
        }
        defaultWordGroupConfig = wordGroupConfig
        this.setId = (wordGroupConfig.setId as? SetId.One)?.id ?: -1L
        getWordsUseCase.execute(wordGroupConfig) {
            wordsListFlow.value = it
            fullWordList = it
        }
    }

    override suspend fun onMoveSelectedWords(toId: Long) {
        if (selectedWordsPositions.isEmpty()) {
            return
        }
        val ids = mutableListOf<Long>()
        wordsListFlow.value?.let { list ->
            for (word in list.withIndex()) {
                if (word.index in selectedWordsPositions) {
                    word.value.id?.let { ids.add(it) }
                }
            }
        }
        moveWordsUseCase.execute(
            MoveWordsRequest(
                idFrom = setId,
                idTo = toId,
                words = ids
            )
        ) {
            if (it) {
                onDeselectAll()
                viewModelScope.launch {
                    onGetWords(defaultWordGroupConfig)
                    onGetSets()
                }
            }
        }
    }

    override suspend fun onDeleteSetById(id: Long) {
        deleteSetUseCase.execute(id) {
            val list = setsFlow.value?.toMutableList() ?: mutableListOf()
            list.removeIf { it.id == id }
            setsFlow.value = list.toList()
        }
    }

    override suspend fun onDeleteWordAt(position: Int) {
        wordsListFlow.value?.get(position)?.id?.let {
            deleteWordsUseCase.execute(it to setId) {
                onWordDeselected(position)
            }
            onGetSets()
        }
    }

    override suspend fun onDeleteSelected() {
        getSelectedPositions().forEach {
            onDeleteWordAt(it)
        }
        onGetWords(defaultWordGroupConfig)
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
                onGetWords(defaultWordGroupConfig)
            }
        }
    }

    fun getSelectedPositions(): Set<Int> {
        return selectedWordsPositions.toSet()
    }

    private fun String.indifferent() = lowercase().trim()
}
