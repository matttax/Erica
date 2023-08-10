package com.matttax.erica.presentation.viewmodels.impl

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import com.matttax.erica.domain.config.SetGroupConfig
import com.matttax.erica.domain.config.SetSorting
import com.matttax.erica.domain.model.SetDomainModel
import com.matttax.erica.domain.usecases.sets.crud.AddSetUseCase
import com.matttax.erica.domain.usecases.sets.crud.DeleteSetUseCase
import com.matttax.erica.domain.usecases.sets.crud.GetSetsUseCase
import com.matttax.erica.presentation.states.SetsState
import com.matttax.erica.presentation.viewmodels.SetsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class SetsViewModelImpl @Inject constructor(
    private val getSetsUseCase: GetSetsUseCase,
    private val addSetUseCase: AddSetUseCase,
    private val deleteSetUseCase: DeleteSetUseCase
) : ViewModel(), SetsViewModel {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val setsStateFlow = MutableStateFlow<SetsState?>(null)
    private val setsFlow = MutableStateFlow<List<SetDomainModel>?>(null)

    init {
        setsFlow.onEach {
            setsStateFlow.value = SetsState(it)
        }.launchIn(scope)
    }

    override fun observeState(): Flow<SetsState?> = setsStateFlow.asStateFlow()

    override suspend fun onAddAction(name: String, description: String) {
        addSetUseCase.execute(name to description) {
            if (it != -1L) {
                setsFlow.value = setsFlow.value?.plus(SetDomainModel(it, name, description))
            }
        }
    }

    override suspend fun onDelete(id: Int) {
        deleteSetUseCase.execute(id.toLong()) {
            val list = setsFlow.value?.toMutableList() ?: mutableListOf()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                list.removeIf { it.id == id.toLong() }
            }
            setsFlow.value = list
        }
    }

    override suspend fun onGetSetsAction() {
        getSetsUseCase.execute(
            SetGroupConfig(
                sorting = SetSorting.LAST_ADDED,
                limit = Int.MAX_VALUE
            )
        ) {
            setsFlow.value = it
        }
    }
}