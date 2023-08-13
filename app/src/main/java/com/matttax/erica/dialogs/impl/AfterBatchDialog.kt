package com.matttax.erica.dialogs.impl

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.R
import com.matttax.erica.adaptors.WordAdaptor
import com.matttax.erica.dialogs.Dialog
import com.matttax.erica.presentation.model.study.StudiedWord
import com.matttax.erica.presentation.model.study.StudiedWordState
import com.matttax.erica.presentation.model.translate.TextCardState
import com.matttax.erica.presentation.model.translate.TranslatedTextCard

class AfterBatchDialog(
    context: Context,
    results: List<StudiedWord>,
    onNext: () -> Unit,
) : Dialog(context, R.layout.after_batch) {

    init {
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
            }
        )
        incorrectRecyclerViewer.layoutManager = LinearLayoutManager(context)
        dialog.setOnDismissListener {
            onNext()
        }
    }
}