package com.matttax.erica.dialogs

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.QuizWord
import com.matttax.erica.R
import com.matttax.erica.adaptors.WordAdaptor

class AfterBatchDialog(context: Context, resource: Int, incorrectWords: List<QuizWord>, correctWords: List<QuizWord>) : Dialog(context, resource) {
    init {
        initDismissButton(R.id.toNextBatch)

        val incorrectRecyclerViewer: RecyclerView = dialogView.findViewById(R.id.answered)
        incorrectRecyclerViewer.adapter = WordAdaptor(context,
            db.getWordsAt(incorrectWords.map { it.id }) + db.getWordsAt(correctWords.map { it.id }),
            ContextCompat.getColor(context, R.color.crimson), incorrectWords.size)
        incorrectRecyclerViewer.layoutManager = LinearLayoutManager(context)
    }
}