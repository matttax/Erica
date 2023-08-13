package com.matttax.erica.dialogs.impl

import android.content.Context
import com.matttax.erica.R
import com.matttax.erica.dialogs.ActionDialog

class DeleteSetDialog(
    context: Context,
    action: () -> Unit,
): ActionDialog(context, R.layout.delete_set) {

    init {
        initDismissButton(R.id.noDeleteSet)
        initActionButton(R.id.yesDeleteSet) {
            dialog.dismiss()
            action()
        }
    }
}
