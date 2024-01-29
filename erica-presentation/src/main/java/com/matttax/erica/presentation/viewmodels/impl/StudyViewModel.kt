package com.matttax.erica.presentation.viewmodels.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matttax.erica.domain.config.AskMode.*
import com.matttax.erica.domain.config.StudyConfig
import com.matttax.erica.domain.model.WordDomainModel
import com.matttax.erica.domain.model.translate.TranslationRequest
import com.matttax.erica.domain.usecases.translate.GetDefinitionsUseCase
import com.matttax.erica.domain.usecases.words.crud.GetWordsUseCase
import com.matttax.erica.domain.usecases.words.study.WordAnsweredUseCase
import com.matttax.erica.presentation.model.study.StudiedWord
import com.matttax.erica.presentation.model.study.StudiedWordState
import com.matttax.erica.presentation.model.translate.TranslatedText
import com.matttax.erica.presentation.states.HintState
import com.matttax.erica.presentation.states.StudyState
import com.matttax.erica.presentation.viewmodels.StatefulObservable
import com.matttax.erica.presentation.viewmodels.StudyInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase,
    private val wordAnsweredUseCase: WordAnsweredUseCase,
    private val getDefinitionsUseCase: GetDefinitionsUseCase
): ViewModel(), StudyInteractor, StatefulObservable<StudyState?> {

    private var batchSize = 0
    private var remaining = 0
    private val correctList = mutableListOf<Long>()
    private val results = mutableSetOf<StudiedWord>()

    private val studyStateFlow = MutableStateFlow<StudyState?>(null)

    private val wordsListFlow = MutableStateFlow<List<WordDomainModel>?>(null)
    private val batchFlow = MutableStateFlow<List<WordDomainModel>?>(null)
    private val currentAskedPosition = MutableStateFlow<Int?>(null)
    private val isFinishedFlow = MutableStateFlow<Boolean?>(false)
    private val isCorrectFlow = MutableStateFlow<Boolean?>(null)

    private val _hintFlow = MutableStateFlow<HintState>(HintState.NotRequested)
    val hintFlow = _hintFlow.asStateFlow()

    init {
        combine(
            wordsListFlow,
            batchFlow,
            currentAskedPosition,
            isFinishedFlow,
            isCorrectFlow,
        ) {
            arr ->
            StudyState(
                allWords = arr[0] as? List<WordDomainModel>,
                currentBatch = arr[1] as? List<WordDomainModel>,
                currentAskedPosition = arr[2] as? Int,
                isFinished = arr[3] as? Boolean,
                isLastCorrect = arr[4] as? Boolean,
                batchResult = results
                    .sortedBy { it.state == StudiedWordState.CORRECT } as? List<StudiedWord>,
                remainingWords = remaining
            )
        }.onEach {
            studyStateFlow.value = it
        }.launchIn(viewModelScope)
    }

    override fun observeState(): Flow<StudyState?> = studyStateFlow.asStateFlow()

    override fun getCurrentState(): StudyState? = studyStateFlow.value

    override suspend fun onGetWords(studyConfig: StudyConfig) {
        getWordsUseCase.execute(studyConfig.wordGroupConfig) {
            wordsListFlow.value = when(studyConfig.askMode) {
                TEXT -> it
                TRANSLATION -> it.map { word -> word.reverse() }
                BOTH -> it + it.map { word -> word.reverse() }
            }
            remaining = wordsListFlow.value?.size ?: 0
        }
        batchSize = studyConfig.batchSize
    }

    override fun onGetNewBatchAction() {
        results.clear()
        isCorrectFlow.value = null
        currentAskedPosition.value = 0
        val newBatch = wordsListFlow.value?.shuffled()
            ?.filter { !correctList.contains(it.id) }
            ?.take(batchSize)
        if (newBatch?.isEmpty() == true) {
            isFinishedFlow.value = true
        }
        batchFlow.value = newBatch
    }

    override suspend fun onWordAnswered(answer: String) {
        val currentWord = batchFlow.value?.getOrNull(currentAskedPosition.value ?: -1) ?: return
        wordAnsweredUseCase.execute(currentWord to answer) {
            isCorrectFlow.value = it
            if (it) {
                currentWord.id?.let {
                    id -> correctList.add(id)
                }
                remaining--
            }
        }
        results += currentWord.let {
            it.toStudiedWord(correctList.contains(it.id))
        }
    }

    override suspend fun onWordForceCorrectAnswer() {
        val currentWord = batchFlow.value?.getOrNull(currentAskedPosition.value ?: -1) ?: return
        wordAnsweredUseCase.execute(currentWord to currentWord.translation) {
            currentWord.id?.let { id -> correctList.add(id) }
            val newAnswer = currentWord.toStudiedWord(true)
            results.removeIf { it.translatedText == newAnswer.translatedText }
            results.add(newAnswer)
            remaining--
        }
    }

    override fun onGetNextWordAction() {
        _hintFlow.value = HintState.NotRequested
        isCorrectFlow.value = null
        currentAskedPosition.value = currentAskedPosition.value?.plus(1)
    }

    override suspend fun onGetHint() {
        currentAskedPosition.value?.let {
            batchFlow.value?.get(it)?.let { word ->
                println(word)
                _hintFlow.value = HintState.Loading
                getDefinitionsUseCase.execute(
                    input = TranslationRequest(
                        word.text, word.textLanguage, word.translationLanguage
                    )
                ) { list ->
                    if (list.isEmpty())
                        _hintFlow.value = HintState.NotFound
                    else _hintFlow.value = HintState.Hint(list)
                }
            } ?: run { _hintFlow.value = HintState.NotFound }
        }
    }

    companion object {
        fun WordDomainModel.reverse(): WordDomainModel {
            return WordDomainModel(
                Long.MAX_VALUE.minus(id ?: 1),
                translation, text, translationLanguage, textLanguage,
                setId, askedCount, answeredCount, addedTimestamp
            )
        }

        fun WordDomainModel.toStudiedWord(isCorrect: Boolean): StudiedWord {
            return StudiedWord(
                translatedText = TranslatedText(
                    textLanguage = textLanguage,
                    translationLanguage = translationLanguage,
                    text = text,
                    translation = translation
                ),
                state = if (isCorrect) {
                    StudiedWordState.CORRECT
                } else {
                    StudiedWordState.INCORRECT
                }
            )
        }
    }
}
