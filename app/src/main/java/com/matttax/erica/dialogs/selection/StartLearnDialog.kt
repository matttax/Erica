package com.matttax.erica.dialogs.selection

import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import com.matttax.erica.activities.LearnActivity
import com.matttax.erica.R
import com.matttax.erica.activities.LearnActivity.Companion.toAskMode
import com.matttax.erica.activities.LearnActivity.Companion.toWordSorting
import com.matttax.erica.databinding.StartLearnDialogBinding
import com.matttax.erica.dialogs.ActionDialog
import com.matttax.erica.dialogs.util.SpinnerDialog
import com.matttax.erica.utils.AppSettings

class StartLearnDialog(
    context: Context,
    wordsCount: Int,
    setId: Long
): ActionDialog<StartLearnDialogBinding>(
    StartLearnDialogBinding.inflate(LayoutInflater.from(context))
) {

    private val appSettings = AppSettings(context)

    private var wordsTotalCount = wordsCount
    private var wordsInBatch = minOf(7, wordsCount)

    init {
        with(binding) {
            numberOfWordsText.text = wordsTotalCount.toString()
            wordsInBatchText.text = wordsInBatch.toString()

            selectNumOfWords.setOnClickListener {
                SpinnerDialog(context, wordsCount, wordsTotalCount, "Words to learn") {
                    wordsTotalCount = it
                    numberOfWordsText.text = it.toString()
                    if (wordsInBatch > wordsTotalCount) {
                        wordsInBatch = wordsTotalCount
                        wordsInBatchText.text = wordsTotalCount.toString()
                    }
                }.showDialog()
            }

            selectWordsInBatch.setOnClickListener {
                SpinnerDialog(context, wordsTotalCount, wordsInBatch, "Words in batch") {
                    wordsInBatch = it
                    wordsInBatchText.text = it.toString()
                }.showDialog()
            }

            priority.adapter = binding.root.context.getAdapter(
                listOf("Worst answered", "Long ago asked", "Recently asked", "Last added", "First added", "Random")
            )
            termDef.adapter = binding.root.context.getAdapter(
                listOf("Translation", "Word", "Both")
            )

            priority.setSelection(appSettings.studyPriorityId)

            initDismissButton(binding.noStartLearn)
            initActionButton(binding.yesStartLearn) {
                LearnActivity.start(
                    context = context,
                    setId = setId,
                    batchSize = wordsInBatch,
                    wordsCount = wordsTotalCount,
                    wordsSorting = priority.selectedItemPosition.toWordSorting(),
                    askMode = termDef.selectedItemPosition.toAskMode()
                )
                appSettings.studyPriorityId = priority.selectedItemPosition
                dialog.dismiss()
            }
        }
    }

    private fun <T> Context.getAdapter(elements: List<T>): ArrayAdapter<T> {
        return ArrayAdapter(this, R.layout.params_spinner_item, elements)
    }
}
