package com.matttax.erica.domain.architecture

import com.matttax.erica.domain.coroutines.CoroutineContextProvider
import kotlinx.coroutines.withContext

abstract class BackgroundUseCase<INPUT, OUTPUT>(
    private val coroutineContextProvider: CoroutineContextProvider
): UseCase<INPUT, OUTPUT> {

    override suspend fun execute(input: INPUT, onResult: (OUTPUT) -> Unit) {
        val result = withContext(coroutineContextProvider.io) {
            executeInBackground(input)
        }
        onResult(result)
    }

    abstract fun executeInBackground(input: INPUT): OUTPUT
}