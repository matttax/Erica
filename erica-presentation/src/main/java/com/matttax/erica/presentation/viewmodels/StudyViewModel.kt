package com.matttax.erica.presentation.viewmodels

import com.matttax.erica.domain.config.StudyConfig
import com.matttax.erica.presentation.states.StudyState

interface StudyViewModel: StateViewModel<StudyState?> {
    suspend fun onGetWords(studyConfig: StudyConfig)
    suspend fun onWordAnswered(answer: String)
    fun onGetNextWordAction()
    fun onGetNewBatchAction()
}
