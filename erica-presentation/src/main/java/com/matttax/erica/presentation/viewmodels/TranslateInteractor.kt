package com.matttax.erica.presentation.viewmodels

interface TranslateInteractor {
    fun onInputTextLanguageChanged(language: String)
    fun onOutputLanguageChanged(language: String)
    fun onInputTextChanged(text: String)
    fun onOutputTextChanged(text: String)
    fun onTranslationSelected(translation: String)
    fun onSetSelected(position: Int)
    fun onClear()
    suspend fun onTranslateAction()
    suspend fun onAddAction()
    suspend fun onGetSetsAction()
}