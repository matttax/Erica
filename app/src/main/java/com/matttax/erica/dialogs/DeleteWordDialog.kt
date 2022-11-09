package com.matttax.erica.dialogs

import android.content.Context
import com.matttax.erica.R
import com.matttax.erica.activities.WordsActivity

class DeleteWordDialog(context: Context, resource: Int, setId: Int, wordId: Int): ActionDialog(context, resource) {
    init {
        initDismissButton(R.id.noDeleteWord)
        initActionButton(R.id.yesDeleteWord) {
            db.deleteWord(wordId, setId)
            dialog.dismiss()
            (context as WordsActivity).loadWords()
        }
    }
}