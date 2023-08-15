package com.matttax.erica.presentation.viewmodels.impl

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matttax.erica.domain.config.SetGroupConfig
import com.matttax.erica.domain.config.SetSorting
import com.matttax.erica.domain.model.SetDomainModel
import com.matttax.erica.domain.usecases.sets.crud.AddSetUseCase
import com.matttax.erica.domain.usecases.sets.crud.DeleteSetUseCase
import com.matttax.erica.domain.usecases.sets.crud.GetSetsUseCase
import com.matttax.erica.domain.usecases.sets.crud.UpdateSetUseCase
import com.matttax.erica.presentation.states.SetsState
import com.matttax.erica.presentation.viewmodels.SetsViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetsViewModelImpl @Inject constructor(
    private val getSetsUseCase: GetSetsUseCase,
    private val addSetUseCase: AddSetUseCase,
    private val deleteSetUseCase: DeleteSetUseCase,
    private val updateSetUseCase: UpdateSetUseCase,
) : ViewModel(), SetsViewModel {

    private val setsStateFlow = MutableStateFlow<SetsState?>(null)
    private val setsFlow = MutableStateFlow<List<SetDomainModel>?>(null)

    init {
        setsFlow.onEach {
            setsStateFlow.value = SetsState(it)
        }.launchIn(viewModelScope)
    }

    override fun observeState(): Flow<SetsState?> = setsStateFlow.asStateFlow()

    override suspend fun onAddAction(name: String, description: String) {
        addSetUseCase.execute(name to description) {
            if (it != -1L) {
                setsFlow.value = setsFlow.value?.plus(SetDomainModel(it, name, description))
            }
        }
    }

    override suspend fun onUpdateAction(id: Long, name: String, description: String) {
        updateSetUseCase.execute(SetDomainModel(id, name, description)) {
            viewModelScope.launch { onGetSetsAction() }
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
                sorting = SetSorting.LAST_MODIFIED,
                limit = Int.MAX_VALUE
            )
        ) {
            setsFlow.value = it
        }
    }
}
