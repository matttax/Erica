package com.matttax.erica.dialogs

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.matttax.erica.R

class MoveDialog(
    context: Context,
    resource: Int,
    sets: List<String>,
    onAction: (Int) -> Unit
): ActionDialog(context, resource) {

    private val setsSpinner: Spinner = dialogView.findViewById(R.id.setsToMove)

    init {
        setsSpinner.adapter = ArrayAdapter(context, R.layout.sets_spinner_item, sets)

        initDismissButton(R.id.noMove)
        initActionButton(R.id.yesMove) {
            onAction(setsSpinner.selectedItemPosition)
            dialog.dismiss()
        }
    }
}