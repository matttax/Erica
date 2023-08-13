package com.matttax.erica.dialogs.impl

import android.content.Context
import com.matttax.erica.R
import com.matttax.erica.dialogs.ActionDialog

class DeleteWordDialog(
    context: Context,
    onAction: () -> Unit
): ActionDialog(context, R.layout.delete_word) {
    init {
        initDismissButton(R.id.noDeleteWord)
        initActionButton(R.id.yesDeleteWord) {
            onAction()
            dialog.dismiss()
        }
    }
}