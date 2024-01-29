package com.matttax.erica.dialogs.selection

import android.content.Context
import android.widget.TextView
import androidx.core.view.isVisible
import com.matttax.erica.R
import com.matttax.erica.dialogs.ActionDialog

class DeleteDialog(
    context: Context,
    headerText: String,
    detailedExplanationText: String?,
    action: () -> Unit,
): ActionDialog(context, R.layout.delete_dialog) {

    private val dialogHeader: TextView = dialogView.findViewById(R.id.dialogHeader)
    private val explanationText: TextView = dialogView.findViewById(R.id.explanationText)

    init {
        dialogHeader.text = headerText
        if (detailedExplanationText.isNullOrBlank()) {
            explanationText.isVisible = false
        } else {
            explanationText.text = detailedExplanationText
        }

        initDismissButton(R.id.noDeleteSet)
        initActionButton(R.id.yesDeleteSet) {
            action()
            dialog.dismiss()
        }
    }
}
