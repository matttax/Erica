package com.matttax.erica.presentation.viewmodels.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.matttax.erica.domain.config.SetGroupConfig
import com.matttax.erica.domain.config.SetSorting
import com.matttax.erica.domain.model.SetDomainModel
import com.matttax.erica.domain.usecases.sets.crud.AddSetUseCase
import com.matttax.erica.domain.usecases.sets.crud.DeleteSetUseCase
import com.matttax.erica.domain.usecases.sets.crud.GetSetsUseCase
import com.matttax.erica.domain.usecases.sets.crud.UpdateSetUseCase
import com.matttax.erica.presentation.states.SetsState
import com.matttax.erica.presentation.viewmodels.SetsInteractor
import com.matttax.erica.presentation.viewmodels.StatefulObservable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetsViewModel @Inject constructor(
    private val getSetsUseCase: GetSetsUseCase,
    private val addSetUseCase: AddSetUseCase,
    private val deleteSetUseCase: DeleteSetUseCase,
    private val updateSetUseCase: UpdateSetUseCase,
) : ViewModel(), SetsInteractor, StatefulObservable<SetsState?> {

    private val setsStateFlow = MutableStateFlow<SetsState?>(null)
    private val setsFlow = MutableStateFlow<List<SetDomainModel>?>(null)

    init {
        setsFlow.onEach {
            setsStateFlow.value = SetsState(it)
        }.launchIn(viewModelScope)
    }

    override fun observeState(): Flow<SetsState?> = setsStateFlow.asStateFlow()

    override fun getCurrentState(): SetsState? = setsStateFlow.value

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
            list.removeIf { it.id == id.toLong() }
            setsFlow.value = list.toList()
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
