package com.matttax.erica.dialogs

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.cardview.widget.CardView
import com.matttax.erica.R
import com.matttax.erica.StudyCard
import com.matttax.erica.activities.WordsActivity

class MoveDialog(context: Context, resource: Int, private val fromId: Int, private val selected: List<Int>): ActionDialog(context, resource) {

    private val setsSpinner: Spinner = dialogView.findViewById(R.id.setsToMove)
    val sets = db.getSets().associate { Pair(it.name, it.id) }

    init {
        setsSpinner.adapter = ArrayAdapter(context, R.layout.sets_spinner_item, sets.keys.toList())

        initDismissButton(R.id.noMove)
        initActionButton(R.id.yesMove) {
            db.moveWords(selected, fromId, sets[setsSpinner.selectedItem.toString()] ?: 0)
            dialog.dismiss()
        }
    }
}