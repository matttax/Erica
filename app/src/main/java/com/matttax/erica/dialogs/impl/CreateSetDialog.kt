package com.matttax.erica.dialogs.impl

import android.content.Context
import com.google.android.material.textfield.TextInputEditText
import com.matttax.erica.R
import com.matttax.erica.dialogs.ActionDialog

class CreateSetDialog(
    context: Context,
    onSuccess: (String, String) -> Unit,
    onFailure: () -> Unit = {}
): ActionDialog(context, R.layout.create_set_dialog) {

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
