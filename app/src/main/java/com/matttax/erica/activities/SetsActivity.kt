package com.matttax.erica.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.R
import com.matttax.erica.adaptors.SetAdaptor
import com.matttax.erica.WordSet
import com.matttax.erica.WordDBHelper
import com.matttax.erica.databinding.ActivityLearnBinding
import com.matttax.erica.databinding.ActivitySetsBinding
import com.matttax.erica.dialogs.CreateSetDialog
import com.matttax.erica.dialogs.DeleteSetDialog
import com.matttax.erica.presentation.states.SetsState
import com.matttax.erica.presentation.viewmodels.SetsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class SetsActivity : AppCompatActivity() {

    @Inject
    lateinit var setsViewModel: SetsViewModel

    private lateinit var binding: ActivitySetsBinding
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sets = mutableListOf<WordSet>()

    private fun setData(setsState: SetsState) {
        Log.i("viewstate", setsState.sets.toString())
        sets.clear()
        setsState.sets?.map {
            WordSet(
                id = it.id.toInt(),
                name = it.name,
                description = it.description,
                wordsCount = 0
            )
        }?.let { sets.addAll(it) }
        binding.setsListRecyclerView.adapter?.notifyDataSetChanged()
    }

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
                resource = R.layout.create_set_dialog,
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

    private fun loadSets() {
        scope.launch {
            setsViewModel.onGetSetsAction()
        }
        binding.setsListRecyclerView.adapter = SetAdaptor(
            context = this,
            sets = sets,
            onClick = {
                val intent = Intent(this, WordsActivity::class.java).apply {
                    putExtra("setid", sets[it].id)
                    putExtra("setname", sets[it].name)
                    putExtra("setdescr", sets[it].description)
                    putExtra("setwordcount", sets[it].wordsCount)
                }
                startActivity(intent)
            },
            onLearnClick = {
                val i = Intent(this, LearnActivity::class.java)
                i.putExtra("query", "SELECT * FROM ${WordDBHelper.WORDS_TABLE_NAME} " +
                        "WHERE ${WordDBHelper.COLUMN_SET_ID}=${sets[it].id}")
                startActivity(i)
            },
            onDeleteClick = {
                DeleteSetDialog(this, R.layout.delete_set) {
                    scope.launch {
                        setsViewModel.onDelete(it)
                    }
                }.showDialog()
            }
        )
        binding.setsListRecyclerView.layoutManager = LinearLayoutManager(this)
    }

}