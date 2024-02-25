package com.matttax.erica.dialogs.results

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.matttax.erica.R
import com.matttax.erica.adapters.PartOfSpeechAdapter
import com.matttax.erica.databinding.WordAnsweredBinding
import com.matttax.erica.dialogs.Dialog
import com.matttax.erica.presentation.states.HintState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WordAnsweredDialog(
    context: Context,
    hintState: Flow<HintState>,
    answeredState: AnsweredState,
    wordAnsweredCallback: WordAnsweredCallback,
): Dialog<WordAnsweredBinding>(
    WordAnsweredBinding.inflate(LayoutInflater.from(context))
) {
    private var hintShown = false
    private val lifecycleScope =
        (context as? AppCompatActivity)?.lifecycle?.coroutineScope ?: CoroutineScope(Dispatchers.Main)

    private val incorrectColor = ContextCompat.getColor(context, R.color.crimson)
    private val correctColor = ContextCompat.getColor(context, R.color.green)

    init {
        binding.hint.layoutManager = LinearLayoutManager(context)
        initDismissButton(binding.answerNext)
        showAnsweredState(answeredState)
        initListeners(wordAnsweredCallback)
        hintState
            .onEach {
                showHintState(it)
            }.launchIn(lifecycleScope)
    }

    private fun showHintState(hintState: HintState) {
        when(hintState) {
            HintState.NotRequested -> {
                binding.hintContainer.isVisible = false
            }
            HintState.Loading -> {
                binding.hintContainer.isVisible = true
                binding.hintProgressBar.isVisible = true
            }
            HintState.NotFound -> {
                binding.hintProgressBar.isVisible = false
                binding.hintErrorText.isVisible = true
            }
            is HintState.Hint -> {
                hintShown = true
                binding.hint.isVisible = true
                binding.hintErrorText.isVisible = false
                binding.hintProgressBar.isVisible = false
                binding.hint.adapter = PartOfSpeechAdapter(hintState.definitions)
            }
        }
    }

    private fun showAnsweredState(answeredState: AnsweredState) {
        binding.answeredCorrectWord.text = answeredState.correctAnswer
        if (!answeredState.isCorrect) {
            binding.answeredHeader.text = "Incorrect"
            binding.dialogCard.strokeColor = incorrectColor
            binding.answeredHeader.setBackgroundColor(incorrectColor)
        } else {
            binding.dialogCard.strokeColor = correctColor
        }
        if (!answeredState.showNotIncorrect) {
            binding.notIncorrect.visibility = View.INVISIBLE
        }
    }

    private fun initListeners(wordAnsweredCallback: WordAnsweredCallback) {
        dialog.setOnDismissListener {
            wordAnsweredCallback.onOk()
        }
        binding.notIncorrect.setOnClickListener {
            wordAnsweredCallback.onNotIncorrect()
            dialog.dismiss()
        }
        binding.answeredCorrectWord.setOnClickListener {
            if (!hintShown) {
                wordAnsweredCallback.onShowHint()
            }
        }
    }
}
