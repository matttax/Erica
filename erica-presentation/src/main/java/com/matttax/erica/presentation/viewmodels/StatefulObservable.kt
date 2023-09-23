package com.matttax.erica.presentation.viewmodels

import kotlinx.coroutines.flow.Flow

interface StatefulObservable<T> {
    fun observeState(): Flow<T>
    fun getCurrentState(): T
}
