package com.matttax.erica.dialogs.results

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.matttax.erica.R
import com.matttax.erica.adaptors.PartOfSpeechAdaptor
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
    lifecycle: Lifecycle? = (context as? AppCompatActivity)?.lifecycle
): Dialog(context, R.layout.word_answered) {

    private val answeredHeader: TextView = dialogView.findViewById(R.id.answeredHeader)
    private val answeredCorrectWord: TextView = dialogView.findViewById(R.id.answeredCorrectWord)
    private val notIncorrectButton: TextView = dialogView.findViewById(R.id.notIncorrect)
    private val dialogCard: MaterialCardView = dialogView.findViewById(R.id.notification_main_column_container)

    private val hintContainer: FrameLayout = dialogView.findViewById(R.id.hintContainer)
    private val hint: RecyclerView = dialogView.findViewById(R.id.hint)
    private val hintLoading: ProgressBar = dialogView.findViewById(R.id.hintProgressBar)
    private val hintError: TextView = dialogView.findViewById(R.id.hintErrorText)

    private var hintShown = false

    init {
        initDismissButton(R.id.answerNext)
        answeredCorrectWord.text = answeredState.correctAnswer
        if (!answeredState.isCorrect) {
            answeredHeader.setBackgroundColor(ContextCompat.getColor(context, R.color.crimson))
            answeredHeader.text = "Incorrect"
            dialogCard.strokeColor = ContextCompat.getColor(context, R.color.crimson)
        } else {
            dialogCard.strokeColor = ContextCompat.getColor(context, R.color.green)
        }
        if (!answeredState.showNotIncorrect) {
            notIncorrectButton.visibility = View.INVISIBLE
        }
        dialog.setOnDismissListener {
            wordAnsweredCallback.onOk()
        }
        notIncorrectButton.setOnClickListener {
            wordAnsweredCallback.onNotIncorrect()
            dialog.dismiss()
        }
        answeredCorrectWord.setOnClickListener {
            if (!hintShown) wordAnsweredCallback.onShowHint()
        }
        hintState.onEach {
            when(it) {
                HintState.NotRequested -> hintContainer.isVisible = false
                HintState.Loading -> {
                    hintContainer.isVisible = true
                    hintLoading.isVisible = true
                }
                HintState.NotFound -> {
                    hintLoading.isVisible = false
                    hintError.isVisible = true
                }
                is HintState.Hint -> {
                    hintShown = true
                    hint.isVisible = true
                    hintError.isVisible = false
                    hintLoading.isVisible = false
                    hint.layoutManager = LinearLayoutManager(context)
                    hint.adapter = PartOfSpeechAdaptor(
                        context,
                        it.definitions
                    )
                }
            }
        }.launchIn(lifecycle?.coroutineScope ?: CoroutineScope(Dispatchers.Main))
    }
}
