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
import com.matttax.erica.adaptors.SetAdaptor
import com.matttax.erica.databinding.FragmentSetsBinding
import com.matttax.erica.dialogs.impl.DeleteDialog
import com.matttax.erica.dialogs.impl.EditDialog
import com.matttax.erica.domain.config.AskMode
import com.matttax.erica.domain.config.WordsSorting
import com.matttax.erica.model.WordSet
import com.matttax.erica.presentation.states.SetsState
import com.matttax.erica.presentation.viewmodels.impl.ChoiceViewModel
import com.matttax.erica.utils.AppSettings
import com.matttax.erica.utils.Utils.getConfigByPosition
import com.matttax.erica.utils.Utils.launchSuspend
import com.matttax.erica.utils.getChoiceNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@AndroidEntryPoint
class SetsFragment : Fragment() {

    @Inject
    lateinit var appSettings: AppSettings

    private val choiceViewModel: ChoiceViewModel by activityViewModels()

    private val binding get() = _binding!!
    private var _binding: FragmentSetsBinding? = null

    private var sets: List<WordSet> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        choiceViewModel.setsStateObservable.observeState()
            .flowOn(Dispatchers.Main)
            .onEach { data ->
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        data?.let { setData(it) }
                    }
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.addNewSet.setOnClickListener {
            binding.addIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate))
            EditDialog(
                context = requireActivity(),
                headerText = "Create set",
                firstField = "Name" to "",
                secondField = "Description" to "",
                ignoreSecondField = true,
                onSuccess = { name, description ->
                    launchSuspend {
                        choiceViewModel.onAddSetAction(name, description)
                        requireActivity().runOnUiThread {
                            loadSets()
                        }
                    }
                },
                onFailure = {
                    Toast.makeText(requireContext(), "Input name", Toast.LENGTH_LONG).show()
                }
            ).showDialog()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        launchSuspend {
            choiceViewModel.onGetSets()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setData(setsState: SetsState) {
        sets = setsState.sets?.map {
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
        loadSets()
    }

    private fun loadSets() {
        if (_binding == null) return
        binding.setsListRecyclerView.adapter = SetAdaptor(
            context = requireActivity(),
            sets = sets,
            onClick = {
                launchSuspend {
                    choiceViewModel.onGetWords(
                        getConfigByPosition(
                            sets[it].id,
                            appSettings.wordsOrderId
                        )
                    )
                }
                getChoiceNavigator().showWords(sets[it])
            },
            onLearnClick = {
                LearnActivity.start(
                    context = requireActivity(),
                    setId = sets[it].id,
                    batchSize = 7,
                    wordsCount = sets[it].wordsCount,
                    wordsSorting = WordsSorting.RANDOM,
                    askMode = AskMode.TEXT
                )
            },
            onDeleteClick = {
                DeleteDialog(
                    context = requireActivity(),
                    headerText = "Sure?",
                    detailedExplanationText = "If you delete the set, all containing words are lost"
                ) {
                    launchSuspend {
                        choiceViewModel.onDeleteSetById(sets[it].id)
                    }
                }.showDialog()
            },
            onEditClick = {
                EditDialog(
                    context = requireActivity(),
                    headerText = "Edit set",
                    firstField = "Name" to sets[it].name,
                    secondField = "Description" to sets[it].description,
                    ignoreSecondField = true,
                    onSuccess = {
                            name, description -> run {
                        launchSuspend {
                            choiceViewModel.onUpdateSetAction(sets[it].id, name, description)
                        }
                    }
                    }
                ).showDialog()
            }
        )
        binding.setsListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.setsListRecyclerView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.light_slide))
    }
}
