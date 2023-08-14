package com.matttax.erica.dialogs.impl

import android.content.Context
import android.util.Log
import android.widget.*
import com.matttax.erica.activities.LearnActivity
import com.matttax.erica.R
import com.matttax.erica.dialogs.ActionDialog
import com.matttax.erica.dialogs.util.SpinnerDialog
import com.matttax.erica.domain.config.WordsSorting

class StartLearnDialog(
    private val context: Context,
    wordsCount: Int,
    setId: Int
): ActionDialog(context, R.layout.start_learn_dialog) {

    private val studyPriority: Spinner = dialogView.findViewById(R.id.priority)
    private val studyMode: Spinner = dialogView.findViewById(R.id.mode)
    private val askWith: Spinner = dialogView.findViewById(R.id.termDef)

    private val wordCountSpinner: LinearLayout = dialogView.findViewById(R.id.select_num_of_words)
    private val wordsInBatchSpinner: LinearLayout = dialogView.findViewById(R.id.select_words_in_batch)

    private val numberOfWordsText: TextView = dialogView.findViewById(R.id.number_of_words_text)
    private val wordsInBatchText: TextView = dialogView.findViewById(R.id.words_in_batch_text)


    private var wordsTotalCount = wordsCount
    private var wordsInBatch = 7

    init {
        val preferences = context.getSharedPreferences("ericaPrefs", Context.MODE_PRIVATE)

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
            listOf("Worst answered", "Least answered", "Long ago asked", "Recently asked", "Last added", "First added", "Random")
        )
        studyMode.adapter = getAdaptor(
            listOf("Study", "Learn", "Test", "Flashcards")
        )
        askWith.adapter = getAdaptor(
            listOf("Translation", "Word", "Both")
        )

        studyPriority.setSelection(preferences.getInt("STUDY_POS", 0))

        initDismissButton(R.id.noStartLearn)
        initActionButton(R.id.yesStartLearn) {
            Log.i("viewstate", wordsTotalCount.toString())
            LearnActivity.start(context, setId, wordsInBatch, wordsTotalCount, WordsSorting.BEST_ANSWERED_FIRST)
            val editor = preferences.edit()
            editor.putInt("STUDY_POS", studyPriority.selectedItemPosition).apply()
            dialog.dismiss()
        }
    }

    private fun <T> getAdaptor(elements: List<T>): ArrayAdapter<T> {
        return ArrayAdapter(context, R.layout.params_spinner_item, elements)
    }

}