package com.matttax.erica.dialogs

import android.content.Context
import com.matttax.erica.StudyCard
import com.matttax.erica.R
import com.matttax.erica.activities.WordsActivity
import com.matttax.erica.adaptors.WordAdaptor

class DeleteWordDialog(context: Context, resource: Int, vararg words: StudyCard): ActionDialog(context, resource) {
    init {
        initDismissButton(R.id.noDeleteWord)
        initActionButton(R.id.yesDeleteWord) {
            for (word in words)
                db.deleteWord(word.id, word.setId)
            dialog.dismiss()
            (context as WordsActivity).words.removeAll(words.toSet())
            context.selected.removeAll(words.toSet())
            if (context.selected.isEmpty())
                context.updateHead()
            context.rv.adapter!!.notifyDataSetChanged()
        }
    }
}