package com.matttax.erica.dialogs.selection

import android.content.Context
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.matttax.erica.databinding.DeleteDialogBinding
import com.matttax.erica.dialogs.ActionDialog

class DeleteDialog(
    context: Context,
    headerText: String,
    detailedExplanationText: String?,
    action: () -> Unit,
): ActionDialog<DeleteDialogBinding>(
    DeleteDialogBinding.inflate(LayoutInflater.from(context))
) {
    init {
        binding.dialogHeader.text = headerText
        if (detailedExplanationText.isNullOrBlank()) {
            binding.explanationText.isVisible = false
        } else {
            binding.explanationText.text = detailedExplanationText
        }

        initDismissButton(binding.noDeleteSet)
        initActionButton(binding.yesDeleteSet) {
            action()
            dialog.dismiss()
        }
    }
}
