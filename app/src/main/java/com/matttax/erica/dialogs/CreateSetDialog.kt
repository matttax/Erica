package com.matttax.erica.dialogs

import android.content.Context
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.matttax.erica.R
import com.matttax.erica.activities.SetsActivity

class CreateSetDialog(
    context: Context,
    resource: Int,
    onSuccess: (String, String) -> Unit,
    onFailure: () -> Unit = {}
): ActionDialog(context, resource) {

    private val setNameInputField: TextInputEditText = dialogView.findViewById(R.id.setNameField)
    private val setDescriptionInputField: TextInputEditText = dialogView.findViewById(R.id.setDescriptionField)

    init {
        initDismissButton(R.id.dismissSet)
        initActionButton(R.id.addSet) {
            if (!setNameInputField.text.isNullOrBlank()) {
                onSuccess(setNameInputField.text.toString(), setDescriptionInputField.text.toString())
                dialog.dismiss()
            } else {
                onFailure()
            }
        }
    }
}
