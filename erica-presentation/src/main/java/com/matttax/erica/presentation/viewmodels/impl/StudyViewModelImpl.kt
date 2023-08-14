package com.matttax.erica.presentation.viewmodels.impl

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matttax.erica.domain.config.StudyConfig
import com.matttax.erica.domain.model.WordDomainModel
import com.matttax.erica.domain.usecases.words.crud.GetWordsUseCase
import com.matttax.erica.domain.usecases.words.study.WordAnsweredUseCase
import com.matttax.erica.presentation.model.study.StudiedWord
import com.matttax.erica.presentation.model.study.StudiedWordState
import com.matttax.erica.presentation.model.translate.TranslatedText
import com.matttax.erica.presentation.states.StudyState
import com.matttax.erica.presentation.viewmodels.StudyViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class StudyViewModelImpl @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase,
    private val wordAnsweredUseCase: WordAnsweredUseCase,
): ViewModel(), StudyViewModel {

    private var batchSize = 0
    private val correctList = mutableListOf<Long>()
    private val results = mutableSetOf<StudiedWord>()

    private val studyStateFlow = MutableStateFlow<StudyState?>(null)

    private val wordsListFlow = MutableStateFlow<List<WordDomainModel>?>(null)
    private val batchFlow = MutableStateFlow<List<WordDomainModel>?>(null)
    private val currentAskedPosition = MutableStateFlow<Int?>(null)
    private val isFinishedFlow = MutableStateFlow<Boolean?>(false)
    private val isCorrectFlow = MutableStateFlow<Boolean?>(null)

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
                    .sortedBy { it.state == StudiedWordState.CORRECT } as? List<StudiedWord>
            )
        }.onEach {
            studyStateFlow.value = it
        }.launchIn(viewModelScope)
    }

    override fun observeState(): Flow<StudyState?> = studyStateFlow.asStateFlow()

    override suspend fun onGetWords(studyConfig: StudyConfig) {
        getWordsUseCase.execute(studyConfig.wordGroupConfig) {
            wordsListFlow.value = it
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
            }
        }
        results += currentWord.let {
            StudiedWord(
                translatedText = TranslatedText(
                    textLanguage = it.textLanguage,
                    translationLanguage = it.translationLanguage,
                    text = it.text,
                    translation = it.translation
                ),
                state = if (correctList.contains(it.id)) {
                    StudiedWordState.CORRECT
                } else {
                    StudiedWordState.INCORRECT
                }
            )
        }
    }

    override fun onGetNextWordAction() {
        isCorrectFlow.value = null
        currentAskedPosition.value = currentAskedPosition.value?.plus(1)
    }
}