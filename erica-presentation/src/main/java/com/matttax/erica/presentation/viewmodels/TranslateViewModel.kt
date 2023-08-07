package com.matttax.erica.presentation.viewmodels

import com.matttax.erica.presentation.states.TranslateState
import kotlinx.coroutines.flow.Flow

interface TranslateViewModel {
    fun observeState(): Flow<TranslateState?>
    fun onInputTextLanguageChanged(language: String)
    fun onOutputLanguageChanged(language: String)
    fun onInputTextChanged(text: String)
    fun onOutputTextChanged(text: String)
    fun onTranslationSelected(translation: String)
    fun onClear()
    suspend fun onTranslateAction()
}