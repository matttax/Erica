package com.matttax.erica.presentation.model.study

import com.matttax.erica.presentation.model.translate.TranslatedText

data class StudiedWord(
    val translatedText: TranslatedText,
    val state: StudiedWordState
)