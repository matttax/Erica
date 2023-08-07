package com.matttax.erica.domain.coroutines

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

interface CoroutineContextProvider {
    val main: CoroutineContext
    val io: CoroutineContext

    object Default: CoroutineContextProvider {
        override val main: CoroutineContext
            get() = Dispatchers.Main

        override val io: CoroutineContext
            get() = Dispatchers.IO
    }
}
