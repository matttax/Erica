package com.matttax.erica.di

import com.matttax.erica.data.repositories.SetsRepositoryImpl
import com.matttax.erica.data.repositories.WordsRepositoryImpl
import com.matttax.erica.domain.repositories.SetsRepository
import com.matttax.erica.domain.repositories.WordsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {

    @Binds
    abstract fun bindWordsRepository(wordsRepositoryImpl: WordsRepositoryImpl): WordsRepository

    @Binds
    abstract fun bindSetsRepositories(setsRepositoryImpl: SetsRepositoryImpl): SetsRepository
}
