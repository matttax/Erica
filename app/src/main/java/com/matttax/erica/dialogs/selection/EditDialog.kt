package com.matttax.erica.dialogs.selection

import android.content.Context
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.matttax.erica.R
import com.matttax.erica.dialogs.ActionDialog

class EditDialog(
    context: Context,
    headerText: String,
    firstField: Pair<String, String>,
    secondField: Pair<String, String>,
    ignoreSecondField: Boolean,
    onSuccess: (String, String) -> Unit,
    onFailure: () -> Unit = {}
): ActionDialog(context, R.layout.edit_dialog) {

    private val dialogHeaderText: TextView = dialogView.findViewById(R.id.editDialogHeader)

    private val firstFieldLayout: TextInputLayout = dialogView.findViewById(R.id.firstFieldLayout)
    private val firstFieldInputTextView: TextInputEditText = dialogView.findViewById(R.id.firstFieldText)

    private val secondFieldLayout: TextInputLayout = dialogView.findViewById(R.id.secondFieldLayout)
    private val secondFieldInputTextView: TextInputEditText = dialogView.findViewById(R.id.secondFieldText)

    init {
        dialogHeaderText.text = headerText
        firstFieldLayout.hint = firstField.first
        firstFieldInputTextView.setText(firstField.second)
        secondFieldLayout.hint = secondField.first
        secondFieldInputTextView.setText(secondField.second)

        initDismissButton(R.id.dismissSet)
        initActionButton(R.id.addSet) {
            if ((!firstFieldInputTextView.text.isNullOrBlank() && ignoreSecondField) ||
                (!firstFieldInputTextView.text.isNullOrBlank() && !secondFieldInputTextView.text.isNullOrBlank() && !ignoreSecondField)
            ) {
                onSuccess(firstFieldInputTextView.text.toString(), secondFieldInputTextView.text.toString())
                dialog.dismiss()
            } else {
                onFailure()
            }
        }
    }
}
