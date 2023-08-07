package com.matttax.erica.di

import com.matttax.erica.data.translator.PythonTranslator
import com.matttax.erica.domain.repositories.Translator
import com.matttax.erica.domain.usecases.translate.GetDefinitionsUseCase
import com.matttax.erica.domain.usecases.translate.GetExamplesUseCase
import com.matttax.erica.domain.usecases.translate.GetTranslationsUseCase
import com.matttax.erica.presentation.viewmodels.TranslateViewModel
import com.matttax.erica.presentation.viewmodels.impl.TranslateViewModelImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranslationModule {

    @Binds
    abstract fun bindTranslator(pythonTranslator: PythonTranslator): Translator

    @Binds
    abstract fun bindViewModel(translateViewModelImpl: TranslateViewModelImpl): TranslateViewModel
}