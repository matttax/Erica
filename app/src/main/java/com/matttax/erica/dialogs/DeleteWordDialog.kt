package com.matttax.erica.dialogs

import android.content.Context
import com.matttax.erica.StudyCard
import com.matttax.erica.R
import com.matttax.erica.activities.WordsActivity

class DeleteWordDialog(context: Context, resource: Int, onAction: () -> Unit): ActionDialog(context, resource) {
    init {
        initDismissButton(R.id.noDeleteWord)
        initActionButton(R.id.yesDeleteWord) {
            onAction()
            dialog.dismiss()
        }
    }
}