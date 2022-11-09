package com.matttax.erica.dialogs

import android.content.Context
import com.matttax.erica.R
import com.matttax.erica.activities.SetsActivity

class DeleteSetDialog(context: Context, resource: Int, setId: Int): ActionDialog(context, resource) {
    init {
        initDismissButton(R.id.noDeleteSet)
        initActionButton(R.id.yesDeleteSet) {
            db.deleteSet(setId)
            dialog.dismiss()
            (context as SetsActivity).loadSets()
        }
    }
}