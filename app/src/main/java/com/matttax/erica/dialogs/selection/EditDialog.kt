package com.matttax.erica.dialogs.selection

import android.content.Context
import android.view.LayoutInflater
import com.matttax.erica.databinding.EditDialogBinding
import com.matttax.erica.dialogs.ActionDialog

class EditDialog(
    context: Context,
    headerText: String,
    firstField: Pair<String, String>,
    secondField: Pair<String, String>,
    ignoreSecondField: Boolean,
    onSuccess: (String, String) -> Unit,
    onFailure: () -> Unit = {}
): ActionDialog<EditDialogBinding>(
    EditDialogBinding.inflate(LayoutInflater.from(context))
) {

    init {
        binding.apply {
            editDialogHeader.text = headerText
            firstFieldLayout.hint = firstField.first
            firstFieldText.setText(firstField.second)
            secondFieldLayout.hint = secondField.first
            secondFieldText.setText(secondField.second)
        }
        initDismissButton(binding.dismissSet)
        initActionButton(binding.addSet) {
            if ((!binding.firstFieldText.text.isNullOrBlank() && ignoreSecondField) ||
                (!binding.firstFieldText.text.isNullOrBlank() && !binding.secondFieldText.text.isNullOrBlank() && !ignoreSecondField)
            ) {
                onSuccess(binding.firstFieldText.text.toString(), binding.secondFieldText.text.toString())
                dialog.dismiss()
            } else {
                onFailure()
            }
        }
    }
}
