package com.matttax.erica.dialogs.impl

import android.content.Context
import android.widget.*
import com.matttax.erica.activities.LearnActivity
import com.matttax.erica.R
import com.matttax.erica.activities.LearnActivity.Companion.toAskMode
import com.matttax.erica.activities.LearnActivity.Companion.toWordSorting
import com.matttax.erica.dialogs.ActionDialog
import com.matttax.erica.dialogs.util.SpinnerDialog
import com.matttax.erica.utils.AppSettings

class StartLearnDialog(
    private val context: Context,
    wordsCount: Int,
    setId: Long
): ActionDialog(context, R.layout.start_learn_dialog) {

    private val appSettings = AppSettings(context)

    private val studyPriority: Spinner = dialogView.findViewById(R.id.priority)
    private val askWith: Spinner = dialogView.findViewById(R.id.termDef)

    private val wordCountSpinner: LinearLayout = dialogView.findViewById(R.id.select_num_of_words)
    private val wordsInBatchSpinner: LinearLayout = dialogView.findViewById(R.id.select_words_in_batch)

    private val numberOfWordsText: TextView = dialogView.findViewById(R.id.number_of_words_text)
    private val wordsInBatchText: TextView = dialogView.findViewById(R.id.words_in_batch_text)

    private var wordsTotalCount = wordsCount
    private var wordsInBatch = minOf(7, wordsCount)

    init {
        numberOfWordsText.text = wordsTotalCount.toString()
        wordsInBatchText.text = wordsInBatch.toString()

        wordCountSpinner.setOnClickListener {
            SpinnerDialog(context, wordsCount, wordsTotalCount, "Words to learn") {
                wordsTotalCount = it
                numberOfWordsText.text = it.toString()
                if (wordsInBatch > wordsTotalCount) {
                    wordsInBatch = wordsTotalCount
                    wordsInBatchText.text = wordsTotalCount.toString()
                }
            }.showDialog()
        }

        wordsInBatchSpinner.setOnClickListener {
            SpinnerDialog(context, wordsTotalCount, wordsInBatch, "Words in batch") {
                wordsInBatch = it
                wordsInBatchText.text = it.toString()
            }.showDialog()
        }

        studyPriority.adapter = getAdaptor(
            listOf("Worst answered", "Long ago asked", "Recently asked", "Last added", "First added", "Random")
        )
        askWith.adapter = getAdaptor(
            listOf("Translation", "Word", "Both")
        )

        studyPriority.setSelection(appSettings.studyPriorityId)

        initDismissButton(R.id.noStartLearn)
        initActionButton(R.id.yesStartLearn) {
            LearnActivity.start(
                context = context,
                setId = setId,
                batchSize = wordsInBatch,
                wordsCount = wordsTotalCount,
                wordsSorting = studyPriority.selectedItemPosition.toWordSorting(),
                askMode = askWith.selectedItemPosition.toAskMode()
            )
            appSettings.studyPriorityId = studyPriority.selectedItemPosition
            dialog.dismiss()
        }
    }

    private fun <T> getAdaptor(elements: List<T>): ArrayAdapter<T> {
        return ArrayAdapter(context, R.layout.params_spinner_item, elements)
    }
}
