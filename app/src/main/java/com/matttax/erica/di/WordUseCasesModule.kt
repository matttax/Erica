package com.matttax.erica.di

import com.matttax.erica.data.database.SqliteDatabaseManager
import com.matttax.erica.data.repositories.WordsRepositoryImpl
import com.matttax.erica.domain.repositories.WordsRepository
import com.matttax.erica.domain.usecases.words.crud.AddWordUseCase
import com.matttax.erica.domain.usecases.words.crud.DeleteWordsUseCase
import com.matttax.erica.domain.usecases.words.crud.GetWordsUseCase
import com.matttax.erica.domain.usecases.words.crud.MoveWordsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WordUseCasesModule {

    @Provides
    @Singleton
    fun provideWordRepositoriesImpl(sqliteDatabaseManager: SqliteDatabaseManager): WordsRepositoryImpl {
        return WordsRepositoryImpl(sqliteDatabaseManager)
    }

    @Provides
    @Singleton
    fun provideGetWordsUseCase(wordsRepository: WordsRepository): GetWordsUseCase {
        return GetWordsUseCase(wordsRepository)
    }

    @Provides
    @Singleton
    fun provideAddWordUseCase(wordsRepository: WordsRepository): AddWordUseCase {
        return AddWordUseCase(wordsRepository)
    }

    @Provides
    @Singleton
    fun provideMoveWordsUseCase(wordsRepository: WordsRepository): MoveWordsUseCase {
        return MoveWordsUseCase(wordsRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteWordsUseCase(wordsRepository: WordsRepository): DeleteWordsUseCase {
        return DeleteWordsUseCase(wordsRepository)
    }
}
