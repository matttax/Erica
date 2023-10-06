package com.matttax.erica.presentation.viewmodels

interface SetsInteractor {
    suspend fun onGetSets()
    suspend fun onAddSetAction(name: String, description: String)
    suspend fun onUpdateSetAction(id: Long, name: String, description: String)
    suspend fun onDeleteSetById(id: Int)
}
