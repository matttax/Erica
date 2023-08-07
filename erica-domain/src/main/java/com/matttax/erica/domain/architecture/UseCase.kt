package com.matttax.erica.domain.architecture

interface UseCase<INPUT, OUTPUT> {
    suspend fun execute(input: INPUT, onResult: (OUTPUT) -> Unit)
}