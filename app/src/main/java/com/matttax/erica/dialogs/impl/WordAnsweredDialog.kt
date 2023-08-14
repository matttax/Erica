package com.matttax.erica.dialogs.impl

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.matttax.erica.R
import com.matttax.erica.dialogs.Dialog

class WordAnsweredDialog(
    context: Context,
    correctAnswer: String,
    isCorrect: Boolean,
    showNotIncorrect: Boolean,
    onOk: () -> Unit,
    onNotIncorrect: () -> Unit,
): Dialog(context, R.layout.word_answered) {

    private val answeredHeader: TextView = dialogView.findViewById(R.id.answeredHeader)
    private val answeredCorrectWord: TextView = dialogView.findViewById(R.id.answeredCorrectWord)
    private val notIncorrectButton: TextView = dialogView.findViewById(R.id.notIncorrect)
    private val dialogCard: MaterialCardView = dialogView.findViewById(R.id.notification_main_column_container)

    init {
        initDismissButton(R.id.answerNext)
        answeredCorrectWord.text = correctAnswer
        if (!isCorrect) {
            answeredHeader.setBackgroundColor(ContextCompat.getColor(context, R.color.crimson))
            answeredHeader.text = "Incorrect"
            dialogCard.strokeColor = ContextCompat.getColor(context, R.color.crimson)
        } else {
            dialogCard.strokeColor = ContextCompat.getColor(context, R.color.green)
        }
        if (!showNotIncorrect) {
            notIncorrectButton.visibility = View.INVISIBLE
        }
        dialog.setOnDismissListener {
            onOk()
        }
        notIncorrectButton.setOnClickListener {
            onNotIncorrect()
            dialog.dismiss()
        }
    }
}
