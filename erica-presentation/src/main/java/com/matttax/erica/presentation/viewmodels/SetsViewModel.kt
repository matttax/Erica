package com.matttax.erica.presentation.viewmodels

import com.matttax.erica.presentation.states.SetsState

interface SetsViewModel: StateViewModel<SetsState?> {
    suspend fun onGetSetsAction()
    suspend fun onAddAction(name: String, description: String)
    suspend fun onUpdateAction(id: Long, name: String, description: String)
    suspend fun onDelete(id: Int)
}
