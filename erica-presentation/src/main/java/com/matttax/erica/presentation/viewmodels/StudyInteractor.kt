package com.matttax.erica.presentation.viewmodels

import com.matttax.erica.domain.config.StudyConfig
import com.matttax.erica.presentation.states.StudyState

interface StudyInteractor {
    suspend fun onGetWords(studyConfig: StudyConfig)
    suspend fun onWordAnswered(answer: String)
    suspend fun onWordForceCorrectAnswer()
    suspend fun onGetHint()
    fun onGetNextWordAction()
    fun onGetNewBatchAction()
}
