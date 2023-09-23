package com.matttax.erica.presentation.viewmodels

interface SetsInteractor {
    suspend fun onGetSetsAction()
    suspend fun onAddAction(name: String, description: String)
    suspend fun onUpdateAction(id: Long, name: String, description: String)
    suspend fun onDelete(id: Int)
}
