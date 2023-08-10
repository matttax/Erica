package com.matttax.erica.presentation.viewmodels

import com.matttax.erica.presentation.states.SetsState
import kotlinx.coroutines.flow.Flow

interface SetsViewModel {
    fun observeState(): Flow<SetsState?>
    suspend fun onGetSetsAction()
    suspend fun onAddAction(name: String, description: String)
    suspend fun onDelete(id: Int)
}
