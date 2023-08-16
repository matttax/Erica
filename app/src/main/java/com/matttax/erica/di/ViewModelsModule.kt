package com.matttax.erica.di

import com.matttax.erica.presentation.viewmodels.SetsViewModel
import com.matttax.erica.presentation.viewmodels.StudyViewModel
import com.matttax.erica.presentation.viewmodels.TranslateViewModel
import com.matttax.erica.presentation.viewmodels.WordsViewModel
import com.matttax.erica.presentation.viewmodels.impl.SetsViewModelImpl
import com.matttax.erica.presentation.viewmodels.impl.StudyViewModelImpl
import com.matttax.erica.presentation.viewmodels.impl.TranslateViewModelImpl
import com.matttax.erica.presentation.viewmodels.impl.WordsViewModelImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ViewModelsModule {

    @Binds
    abstract fun bindViewModel(translateViewModelImpl: TranslateViewModelImpl): TranslateViewModel

    @Binds
    abstract fun bindSetsViewModel(setsViewModelImpl: SetsViewModelImpl): SetsViewModel

    @Binds
    abstract fun bindWordsViewModel(wordsViewModelImpl: WordsViewModelImpl): WordsViewModel

    @Binds
    abstract fun bindStudyViewModel(studyViewModelImpl: StudyViewModelImpl): StudyViewModel
}
