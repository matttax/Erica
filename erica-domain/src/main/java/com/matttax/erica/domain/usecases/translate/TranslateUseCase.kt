package com.matttax.erica.domain.usecases.translate

import com.matttax.erica.domain.architecture.BackgroundUseCase
import com.matttax.erica.domain.coroutines.CoroutineContextProvider
import com.matttax.erica.domain.model.translate.TranslationRequest

abstract class TranslateUseCase<OUTPUT> : BackgroundUseCase<TranslationRequest, OUTPUT>(
    CoroutineContextProvider.Default
)