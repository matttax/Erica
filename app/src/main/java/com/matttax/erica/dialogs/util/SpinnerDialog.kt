package com.matttax.erica.dialogs.util

import android.content.Context
import android.widget.NumberPicker
import android.widget.TextView
import com.matttax.erica.R
import com.matttax.erica.dialogs.Dialog

class SpinnerDialog(
    val context: Context,
    wordsCount: Int,
    defaultValue: Int,
    text: String,
    onOk: (Int) -> Unit = {}
): Dialog(context, R.layout.words_to_study) {
    val wordsToBeStudied: NumberPicker = dialogView.findViewById(R.id.wordsNumberPicker)
    val wordsText: TextView = dialogView.findViewById(R.id.words_text)

    init {
        initDismissButton(R.id.ok_num_of_words)

        wordsText.text = text

        wordsToBeStudied.maxValue = wordsCount
        wordsToBeStudied.minValue = Integer.min(1, wordsCount)
        wordsToBeStudied.value = defaultValue
        dialog.setOnDismissListener {
            onOk(wordsToBeStudied.value)
        }
    }
}