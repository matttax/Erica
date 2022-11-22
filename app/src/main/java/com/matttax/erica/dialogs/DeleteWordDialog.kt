package com.matttax.erica.dialogs

import android.content.Context
import android.util.Log
import com.matttax.erica.QuizWord
import com.matttax.erica.R
import com.matttax.erica.activities.WordsActivity
import com.matttax.erica.adaptors.WordAdaptor

class DeleteWordDialog(context: Context, resource: Int, word: QuizWord): ActionDialog(context, resource) {
    init {
        initDismissButton(R.id.noDeleteWord)
        initActionButton(R.id.yesDeleteWord) {
            db.deleteWord(word.id, word.setId)
            dialog.dismiss()
            (context as WordsActivity).words.remove(word)
            context.selected.remove(word)
            if (context.selected.isEmpty())
                (context.rv.adapter!! as WordAdaptor).whenClick?.let { it1 -> it1() }
            context.rv.adapter!!.notifyDataSetChanged()
        }
    }
}