package com.matttax.erica.presentation.states

sealed class DataState {
    object Loading: DataState()
    object NoInternet: DataState()
    object NotFound: DataState()
    data class LoadedInfo<T>(val info: T): DataState()
}
