package com.matttax.erica.di

import com.matttax.erica.data.translator.PythonTranslator
import com.matttax.erica.domain.repositories.Translator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TranslationModule {

    @Binds
    abstract fun bindTranslator(pythonTranslator: PythonTranslator): Translator
}