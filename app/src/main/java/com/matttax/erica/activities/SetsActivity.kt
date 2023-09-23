package com.matttax.erica.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.matttax.erica.adaptors.SetAdaptor
import com.matttax.erica.model.WordSet
import com.matttax.erica.databinding.ActivitySetsBinding
import com.matttax.erica.dialogs.impl.EditDialog
import com.matttax.erica.dialogs.impl.DeleteDialog
import com.matttax.erica.domain.config.AskMode
import com.matttax.erica.domain.config.WordsSorting
import com.matttax.erica.presentation.states.SetsState
import com.matttax.erica.presentation.viewmodels.SetsInteractor
import com.matttax.erica.presentation.viewmodels.impl.SetsViewModel
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

    private val setsViewModel: SetsViewModel by viewModels()

    private lateinit var binding: ActivitySetsBinding
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var sets: List<WordSet> = emptyList()

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
            EditDialog(
                context = this,
                headerText = "Create set",
                firstField = "Name" to "",
                secondField = "Description" to "",
                ignoreSecondField = true,
                onSuccess = { name, description ->
                    scope.launch {
                        setsViewModel.onAddAction(name, description)
                        runOnUiThread {
                            loadSets()
                        }
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
        scope.launch {
            setsViewModel.onGetSetsAction()
        }
    }

    private fun setData(setsState: SetsState) {
        sets = setsState.sets?.map {
            WordSet(
                id = it.id.toInt(),
                name = it.name,
                description = it.description ?: " ",
                wordsCount = it.wordsCount ?: 0
            )
        } ?: emptyList()
        loadSets()
    }

    private fun loadSets() {
        binding.setsListRecyclerView.adapter = SetAdaptor(
            context = this,
            sets = sets,
            onClick = {
                WordsActivity.start(this, sets[it])
            },
            onLearnClick = {
                LearnActivity.start(
                    context = this,
                    setId = sets[it].id,
                    batchSize = 7,
                    wordsCount = sets[it].wordsCount,
                    wordsSorting = WordsSorting.RANDOM,
                    askMode = AskMode.TEXT
                )
            },
            onDeleteClick = {
                DeleteDialog(
                    context = this,
                    headerText = "Sure?",
                    detailedExplanationText = "If you delete the set, all containing words are lost"
                ) {
                    scope.launch {
                        setsViewModel.onDelete(sets[it].id)
                    }
                }.showDialog()
            },
            onEditClick = {
                EditDialog(
                    context = this,
                    headerText = "Edit set",
                    firstField = "Name" to sets[it].name,
                    secondField = "Description" to sets[it].description,
                    ignoreSecondField = true,
                    onSuccess = {
                        name, description -> run {
                            scope.launch {
                                setsViewModel.onUpdateAction(sets[it].id.toLong(), name, description)
                            }
                        }
                    }
                ).showDialog()
            }
        )
        binding.setsListRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}
