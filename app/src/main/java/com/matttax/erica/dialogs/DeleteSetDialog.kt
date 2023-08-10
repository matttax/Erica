package com.matttax.erica.dialogs

import android.content.Context
import com.matttax.erica.R
import com.matttax.erica.activities.SetsActivity

class DeleteSetDialog(
    context: Context,
    resource: Int,
    action: () -> Unit,
): ActionDialog(context, resource) {

    init {
        initDismissButton(R.id.noDeleteSet)
        initActionButton(R.id.yesDeleteSet) {
            dialog.dismiss()
            action()
        }
    }
}