package com.matttax.erica.dialogs

import android.content.Context
import android.content.Intent
import android.widget.*
import com.matttax.erica.activities.LearnActivity
import com.matttax.erica.R
import com.matttax.erica.WordDBHelper
import com.matttax.erica.dialogs.start_study.SpinnerDialog

class StartLearnDialog(val context: Context, resource: Int, wordsCount: Int, setId: Int): ActionDialog(context, resource) {
    private val learnIntent = Intent(context, LearnActivity::class.java)

    private val studyPriority: Spinner = dialogView.findViewById(R.id.priority)
    private val studyMode: Spinner = dialogView.findViewById(R.id.mode)

    private val spinner: LinearLayout = dialogView.findViewById(R.id.select_num_of_words)
    private val wbspinner: LinearLayout = dialogView.findViewById(R.id.select_words_in_batch)

    private val nmwrdText: TextView = dialogView.findViewById(R.id.number_of_words_text)
    private val wnbText: TextView = dialogView.findViewById(R.id.words_in_batch_text)


    var woirt = wordsCount
    var nadab = 7

    init {
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

        studyPriority.adapter = getAdaptor(listOf("Worst answered", "Least asked", "Long ago asked"))
        studyMode.adapter = getAdaptor(listOf("Study", "Learn"))

        initDismissButton(R.id.noStartLearn)
        initActionButton(R.id.yesStartLearn) {
            val query = "SELECT * FROM ${WordDBHelper.WORDS_TABLE_NAME} " +
                        "WHERE ${WordDBHelper.COLUMN_SET_ID}=$setId " +
                        "ORDER BY ${getOrderBy(studyPriority.selectedItem.toString())}" +
                        "LIMIT $woirt "
            learnIntent.putExtra("query", query)
            learnIntent.putExtra("batch_size", nadab)
            dialog.dismiss()
            context.startActivity(learnIntent)
        }
    }

    private fun <T> getAdaptor(elements: List<T>): ArrayAdapter<T> {
        val adaptor = ArrayAdapter(context, R.layout.sets_spinner_item, elements)
        adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adaptor
    }

    private fun getOrderBy(str: String) = when(str) {
            "Worst answered" -> "times_correct / CAST(times_asked as float) ASC "
            "Least asked" -> "times_asked ASC "
            else -> "last_asked ASC "
    }

}