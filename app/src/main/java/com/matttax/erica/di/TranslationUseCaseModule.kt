package com.matttax.erica.di

import com.matttax.erica.domain.repositories.Translator
import com.matttax.erica.domain.usecases.translate.GetDefinitionsUseCase
import com.matttax.erica.domain.usecases.translate.GetExamplesUseCase
import com.matttax.erica.domain.usecases.translate.GetTranslationsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TranslationUseCaseModule {

    @Provides
    @Singleton
    fun provideGetTranslationsUseCase(translator: Translator): GetTranslationsUseCase {
        return GetTranslationsUseCase(translator)
    }

    @Provides
    @Singleton
    fun provideGetDefinitionsUseCase(translator: Translator): GetDefinitionsUseCase {
        return GetDefinitionsUseCase(translator)
    }

    @Provides
    @Singleton
    fun provideGetExamplesUseCase(translator: Translator): GetExamplesUseCase {
        return GetExamplesUseCase(translator)
    }

}