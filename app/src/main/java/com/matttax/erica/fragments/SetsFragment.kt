package com.matttax.erica.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.matttax.erica.R
import com.matttax.erica.activities.LearnActivity
import com.matttax.erica.adapters.SetAdapter
import com.matttax.erica.adapters.callback.SetCallback
import com.matttax.erica.adapters.listeners.SearchFieldListener.Companion.setSearchListener
import com.matttax.erica.databinding.FragmentSetsBinding
import com.matttax.erica.dialogs.selection.DeleteDialog
import com.matttax.erica.dialogs.selection.EditDialog
import com.matttax.erica.domain.config.AskMode
import com.matttax.erica.domain.config.WordsSorting
import com.matttax.erica.model.WordSet
import com.matttax.erica.presentation.states.SetsState
import com.matttax.erica.presentation.viewmodels.impl.ChoiceViewModel
import com.matttax.erica.utils.AppSettings
import com.matttax.erica.utils.Utils.getConfigByPosition
import com.matttax.erica.utils.getChoiceNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SetsFragment : Fragment() {

    @Inject
    lateinit var appSettings: AppSettings

    private val choiceViewModel: ChoiceViewModel by activityViewModels()

    private val binding get() = _binding!!
    private var _binding: FragmentSetsBinding? = null

    private var sets: List<WordSet> = emptyList()

    private val setCallback by lazy {
        object : SetCallback {
            override fun onClick(position: Int) {
                lifecycleScope.launch {
                    choiceViewModel.onGetWords(
                        getConfigByPosition(
                            sets[position].id,
                            appSettings.wordsOrderId
                        )
                    )
                }
                getChoiceNavigator().showWords(sets[position])
            }

            override fun onLearnClick(position: Int) {
                LearnActivity.start(
                    context = requireActivity(),
                    setId = sets[position].id,
                    batchSize = 7,
                    wordsCount = sets[position].wordsCount,
                    wordsSorting = WordsSorting.RANDOM,
                    askMode = AskMode.TEXT
                )
            }

            override fun onDeleteClick(position: Int) {
                DeleteDialog(
                    context = requireActivity(),
                    headerText = "Sure?",
                    detailedExplanationText = "If you delete the set, all containing words are lost"
                ) {
                    lifecycleScope.launch {
                        choiceViewModel.onDeleteSetById(sets[position].id)
                    }
                }.showDialog()
            }

            override fun onEditClick(position: Int) {
                EditDialog(
                    context = requireActivity(),
                    headerText = "Edit set",
                    firstField = "Name" to sets[position].name,
                    secondField = "Description" to sets[position].description,
                    ignoreSecondField = true,
                    onSuccess = { name, description ->
                        lifecycleScope.launch {
                            choiceViewModel.onUpdateSetAction(sets[position].id, name, description)
                        }
                    }
                ).showDialog()
            }
        }
    }


    private val setAdapter by lazy { SetAdapter(setCallback) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        choiceViewModel.setsStateObservable.observeState()
            .flowOn(Dispatchers.Main)
            .onEach { data ->
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        data?.let { setData(it) }
                    }
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.searchField.setSearchListener {
            choiceViewModel.filterSetsByQuery(it)
        }
        binding.addNewSet.setOnClickListener {
            binding.addIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate))
            EditDialog(
                context = requireActivity(),
                headerText = "Create set",
                firstField = "Name" to "",
                secondField = "Description" to "",
                ignoreSecondField = true,
                onSuccess = { name, description ->
                    lifecycleScope.launch {
                        choiceViewModel.onAddSetAction(name, description)
                    }
                },
                onFailure = {
                    Toast.makeText(requireContext(), "Input name", Toast.LENGTH_LONG).show()
                }
            ).showDialog()
        }
        binding.setsListRecyclerView.adapter = setAdapter
        binding.setsListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.setsListRecyclerView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.light_slide))
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            choiceViewModel.onGetSets()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setData(setsState: SetsState) {
        sets = setsState.sets
            ?.filter {
                it.name.lowercase().trim().contains(setsState.filter)
            }
            ?.map {
                WordSet(
                    id = it.id,
                    name = it.name,
                    description = it.description ?: " ",
                    wordsCount = it.wordsCount ?: 0
                )
            } ?: emptyList()
        if (setsState.sets != null && sets.isEmpty()) {
            binding.addIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate))
        }
        if (_binding == null) return
        setAdapter.submitList(sets)
    }
}
