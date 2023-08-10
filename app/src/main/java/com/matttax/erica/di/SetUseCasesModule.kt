package com.matttax.erica.di

import com.matttax.erica.data.database.SqliteDatabaseManager
import com.matttax.erica.data.repositories.SetsRepositoryImpl
import com.matttax.erica.domain.repositories.SetsRepository
import com.matttax.erica.domain.usecases.sets.crud.AddSetUseCase
import com.matttax.erica.domain.usecases.sets.crud.DeleteSetUseCase
import com.matttax.erica.domain.usecases.sets.crud.GetSetsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SetUseCasesModule {

    @Provides
    @Singleton
    fun provideSetRepositoryImpl(sqliteDatabaseManager: SqliteDatabaseManager): SetsRepositoryImpl {
        return SetsRepositoryImpl(sqliteDatabaseManager)
    }

    @Provides
    @Singleton
    fun provideGetSetsUseCase(setsRepository: SetsRepository): GetSetsUseCase {
        return GetSetsUseCase(setsRepository)
    }

    @Provides
    @Singleton
    fun provideAddSetUseCase(setsRepository: SetsRepository): AddSetUseCase {
        return AddSetUseCase(setsRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteSetUseCase(setsRepository: SetsRepository): DeleteSetUseCase {
        return DeleteSetUseCase(setsRepository)
    }
}