package com.matttax.erica.presentation.viewmodels

import kotlinx.coroutines.flow.Flow

interface StateViewModel<T> {
    fun observeState(): Flow<T>
}