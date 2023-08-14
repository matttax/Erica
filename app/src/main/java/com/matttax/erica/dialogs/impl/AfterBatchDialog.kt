package com.matttax.erica.dialogs.impl

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.R
import com.matttax.erica.adaptors.WordAdaptor
import com.matttax.erica.dialogs.Dialog
import com.matttax.erica.presentation.model.study.StudiedWord
import com.matttax.erica.presentation.model.study.StudiedWordState
import com.matttax.erica.presentation.model.translate.TextCardState
import com.matttax.erica.presentation.model.translate.TranslatedTextCard
import com.matttax.erica.speechtotext.WordSpeller

class AfterBatchDialog(
    context: Context,
    results: List<StudiedWord>,
    remainingCount: Int,
    wordSpeller: WordSpeller,
    onNext: () -> Unit
) : Dialog(context, R.layout.after_batch) {

    private val remainingTextView: TextView = dialogView.findViewById(R.id.remainingText)

    init {
        remainingTextView.text = "Remaining: $remainingCount"
        initDismissButton(R.id.toNextBatch)

        val incorrectRecyclerViewer: RecyclerView = dialogView.findViewById(R.id.answered)
        incorrectRecyclerViewer.adapter = WordAdaptor(
            context = context,
            words = results.map {
                TranslatedTextCard(
                    translatedText = it.translatedText,
                    isEditable = false,
                    isSelected = false,
                    state = when(it.state) {
                        StudiedWordState.CORRECT -> TextCardState.CORRECT
                        StudiedWordState.INCORRECT -> TextCardState.INCORRECT
                        StudiedWordState.NOT_ASKED -> TextCardState.DEFAULT
                    }
                )
            },
            onSpell = { button, text ->
                button.setColorFilter(Color.argb(255, 255, 165, 0))
                wordSpeller.spell(text) {
                    button.setColorFilter(Color.argb(255, 41, 45, 54))
                }
            }
        )
        incorrectRecyclerViewer.layoutManager = LinearLayoutManager(context)
        dialog.setOnDismissListener {
            onNext()
        }
    }
}