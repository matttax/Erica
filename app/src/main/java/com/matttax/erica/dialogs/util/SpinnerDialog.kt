package com.matttax.erica.dialogs.util

import android.content.Context
import android.view.LayoutInflater
import com.matttax.erica.databinding.WordsToStudyBinding
import com.matttax.erica.dialogs.Dialog

class SpinnerDialog(
    context: Context,
    wordsCount: Int,
    defaultValue: Int,
    text: String,
    onOk: (Int) -> Unit = {}
): Dialog<WordsToStudyBinding>(
    WordsToStudyBinding.inflate(LayoutInflater.from(context))
) {
    init {
        initDismissButton(binding.okNumOfWords)
        binding.apply {
            wordsText.text = text
            wordsNumberPicker.maxValue = wordsCount
            wordsNumberPicker.minValue = Integer.min(1, wordsCount)
            wordsNumberPicker.value = defaultValue
            dialog.setOnDismissListener {
                onOk(wordsNumberPicker.value)
            }
        }
    }
}
