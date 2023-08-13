package com.matttax.erica.dialogs.impl

import android.content.Context
import android.content.Intent
import android.widget.*
import com.matttax.erica.activities.LearnActivity
import com.matttax.erica.R
import com.matttax.erica.WordDBHelper
import com.matttax.erica.dialogs.ActionDialog
import com.matttax.erica.dialogs.util.SpinnerDialog

class StartLearnDialog(val context: Context, resource: Int, wordsCount: Int, setId: Int): ActionDialog(context, resource) {
    private val learnIntent = Intent(context, LearnActivity::class.java)

    private val studyPriority: Spinner = dialogView.findViewById(R.id.priority)
    private val studyMode: Spinner = dialogView.findViewById(R.id.mode)
    private val askWith: Spinner = dialogView.findViewById(R.id.termDef)

    private val spinner: LinearLayout = dialogView.findViewById(R.id.select_num_of_words)
    private val wbspinner: LinearLayout = dialogView.findViewById(R.id.select_words_in_batch)

    private val nmwrdText: TextView = dialogView.findViewById(R.id.number_of_words_text)
    private val wnbText: TextView = dialogView.findViewById(R.id.words_in_batch_text)


    var woirt = wordsCount
    var nadab = 7

    init {
        val preferences = context.getSharedPreferences("ericaPrefs", Context.MODE_PRIVATE)

        nmwrdText.text = wordsCount.toString()
        wnbText.text = nadab.toString()

        spinner.setOnClickListener {
            val ss = SpinnerDialog(context, R.layout.words_to_study, wordsCount, woirt, "Words to learn")
            ss.showDialog()
            ss.dialog.setOnDismissListener {
                woirt = ss.wordsToBeStudied.value
                nmwrdText.text = ss.wordsToBeStudied.value.toString()

                if (nadab > woirt) {
                    nadab = woirt
                    wnbText.text = woirt.toString()
                }
            }
        }

        wbspinner.setOnClickListener {
            val ss = SpinnerDialog(context, R.layout.words_to_study, woirt, nadab, "Words in batch")
            ss.showDialog()
            ss.dialog.setOnDismissListener {
                nadab = ss.wordsToBeStudied.value
                wnbText.text = ss.wordsToBeStudied.value.toString()
            }
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
            val editor = preferences.edit()
            editor.putInt("STUDY_POS", studyPriority.selectedItemPosition).apply()
            val query = "SELECT * FROM ${WordDBHelper.WORDS_TABLE_NAME} " +
                        "WHERE ${WordDBHelper.COLUMN_SET_ID}=$setId AND ${WordDBHelper.COLUMN_TERM_LANGUAGE}<>\"null\" " +
                        "ORDER BY ${getOrderBy(studyPriority.selectedItem.toString())}" +
                        "LIMIT $woirt "
            learnIntent.putExtra("setId", setId.toLong())
            learnIntent.putExtra("query", query)
            learnIntent.putExtra("batch_size", nadab)
            learnIntent.putExtra("ask", askWith.selectedItem.toString())
            dialog.dismiss()
            context.startActivity(learnIntent)
        }
    }

    private fun <T> getAdaptor(elements: List<T>): ArrayAdapter<T> {
        return ArrayAdapter(context, R.layout.params_spinner_item, elements)
    }

    private fun getOrderBy(str: String) = when(str) {
            "Worst answered" -> "${WordDBHelper.COLUMN_TIMES_CORRECT} / CAST(${WordDBHelper.COLUMN_TIMES_ASKED} as float) ASC "
            "Least asked" -> "${WordDBHelper.COLUMN_TIMES_ASKED} ASC "
            "Long ago asked" -> "${WordDBHelper.COLUMN_LAST_ASKED} ASC "
            "Random" -> "RANDOM() "
            "Last added" -> "${WordDBHelper.COLUMN_WORD_ID} DESC "
            "First added" -> "${WordDBHelper.COLUMN_WORD_ID} ASC "
            else -> "${WordDBHelper.COLUMN_LAST_ASKED} DESC "
    }

}