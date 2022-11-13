package com.matttax.erica.dialogs.start_study

import android.content.Context
import android.widget.NumberPicker
import android.widget.TextView
import com.matttax.erica.R
import com.matttax.erica.dialogs.Dialog

class SpinnerDialog(val context: Context, resource: Int, wordsCount: Int, defaultValue: Int, text:String="Words"): Dialog(context, resource) {
    val wordsToBeStudied: NumberPicker = dialogView.findViewById(R.id.wordsNumberPicker)
    val wordsText: TextView = dialogView.findViewById(R.id.words_text)

    init {
        initDismissButton(R.id.ok_num_of_words)

        wordsText.text = text

        wordsToBeStudied.maxValue = wordsCount
        wordsToBeStudied.minValue = Integer.min(1, wordsCount)
        wordsToBeStudied.value = defaultValue
    }
}