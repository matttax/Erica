package com.matttax.erica.dialogs.impl

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.matttax.erica.R
import com.matttax.erica.dialogs.ActionDialog

class MoveDialog(
    context: Context,
    sets: Collection<String>,
    onAction: (Int) -> Unit
): ActionDialog(context, R.layout.move_dialog) {

    private val setsSpinner: Spinner = dialogView.findViewById(R.id.setsToMove)

    init {
        setsSpinner.adapter = ArrayAdapter(context, R.layout.sets_spinner_item, sets.toList())

        initDismissButton(R.id.noMove)
        initActionButton(R.id.yesMove) {
            onAction(setsSpinner.selectedItemPosition)
            dialog.dismiss()
        }
    }
}
