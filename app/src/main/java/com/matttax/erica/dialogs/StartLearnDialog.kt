package com.matttax.erica.dialogs

import android.content.Context
import android.content.Intent
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner
import com.matttax.erica.activities.LearnActivity
import com.matttax.erica.R
import com.matttax.erica.WordDBHelper
import java.lang.Integer.min

class StartLearnDialog(val context: Context, resource: Int, wordsCount: Int, setId: Int): ActionDialog(context, resource) {
    private val learnIntent = Intent(context, LearnActivity::class.java)

    private val wordsToBeStudied: NumberPicker = dialogView.findViewById(R.id.wordsNumberPicker)
    private val studyPriority: Spinner = dialogView.findViewById(R.id.priority)
    private val studyMode: Spinner = dialogView.findViewById(R.id.mode)
    private val studyBatchSize: Spinner = dialogView.findViewById(R.id.wordsInBatch)


    init {
        wordsToBeStudied.maxValue = wordsCount
        wordsToBeStudied.minValue = min(1, wordsCount)
        wordsToBeStudied.value = wordsCount

        studyPriority.adapter = getAdaptor(listOf("Worst answered", "Least asked", "Long ago asked"))
        studyMode.adapter = getAdaptor(listOf("Study", "Learn"))
        studyBatchSize.adapter = getAdaptor(listOf(3, 5, 7, 10, 12, 15))
        studyBatchSize.setSelection(2)

        initDismissButton(R.id.noStartLearn)
        initActionButton(R.id.yesStartLearn) {
            val query = "SELECT * FROM ${WordDBHelper.WORDS_TABLE_NAME} " +
                        "WHERE ${WordDBHelper.COLUMN_SET_ID}=$setId " +
                        "ORDER BY ${getOrderBy(studyPriority.selectedItem.toString())}" +
                        "LIMIT ${wordsToBeStudied.value} "
            learnIntent.putExtra("query", query)
            learnIntent.putExtra("batch_size", studyBatchSize.selectedItem.toString().toInt())
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