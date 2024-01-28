package com.matttax.erica.presentation.viewmodels

interface SetsInteractor {
    fun filterSetsByQuery(query: String)
    suspend fun onGetSets()
    suspend fun onAddSetAction(name: String, description: String)
    suspend fun onUpdateSetAction(id: Long, name: String, description: String)
    suspend fun onDeleteSetById(id: Long)
}
