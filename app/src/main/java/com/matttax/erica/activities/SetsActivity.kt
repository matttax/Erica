package com.matttax.erica.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.matttax.erica.adaptors.SetAdaptor
import com.matttax.erica.WordSet
import com.matttax.erica.databinding.ActivitySetsBinding
import com.matttax.erica.dialogs.impl.CreateSetDialog
import com.matttax.erica.dialogs.impl.DeleteSetDialog
import com.matttax.erica.presentation.states.SetsState
import com.matttax.erica.presentation.viewmodels.SetsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SetsActivity : AppCompatActivity() {

    @Inject
    lateinit var setsViewModel: SetsViewModel

    private lateinit var binding: ActivitySetsBinding
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sets = mutableListOf<WordSet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setsViewModel.observeState()
            .flowOn(Dispatchers.Main)
            .onEach { data ->
                runOnUiThread {
                    data?.let { setData(it) }
                }
            }.launchIn(scope)

        binding.addNewSet.setOnClickListener {
            CreateSetDialog(
                context = this,
                onSuccess = { name, description ->
                    scope.launch {
                        setsViewModel.onAddAction(name, description)
                    }
                },
                onFailure = {
                    Toast.makeText(this, "Input name", Toast.LENGTH_LONG).show()
                }
            ).showDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        loadSets()
    }

    private fun setData(setsState: SetsState) {
        sets.clear()
        setsState.sets?.map {
            WordSet(
                id = it.id.toInt(),
                name = it.name,
                description = it.description,
                wordsCount = it.wordsCount ?: 0
            )
        }?.let { sets.addAll(it) }
        binding.setsListRecyclerView.adapter?.notifyItemRangeChanged(0, sets.size - 1)
    }

    private fun loadSets() {
        scope.launch {
            setsViewModel.onGetSetsAction()
        }
        binding.setsListRecyclerView.adapter = SetAdaptor(
            context = this,
            sets = sets,
            onClick = {
                WordsActivity.start(this, sets[it])
            },
            onLearnClick = {
//                LearnActivity.start(this, sets[it].id, 7)
            },
            onDeleteClick = {
                DeleteSetDialog(this) {
                    scope.launch {
                        setsViewModel.onDelete(it)
                    }
                }.showDialog()
            }
        )
        binding.setsListRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}
