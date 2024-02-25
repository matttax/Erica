package com.matttax.erica.dialogs.results

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.R
import com.matttax.erica.adapters.WordAdapter
import com.matttax.erica.adapters.callback.WordCallback
import com.matttax.erica.databinding.AfterBatchBinding
import com.matttax.erica.dialogs.Dialog
import com.matttax.erica.presentation.model.study.StudiedWord
import com.matttax.erica.presentation.model.study.StudiedWordState
import com.matttax.erica.presentation.model.translate.TextCardState
import com.matttax.erica.presentation.model.translate.TranslatedText
import com.matttax.erica.presentation.model.translate.TranslatedTextCard
import com.matttax.erica.speechtotext.WordSpeller

class AfterBatchDialog(
    context: Context,
    results: List<StudiedWord>,
    remainingCount: Int,
    wordSpeller: WordSpeller,
    onNext: () -> Unit
) : Dialog<AfterBatchBinding>(
    AfterBatchBinding.inflate(LayoutInflater.from(context))
) {

    init {
        binding.remainingText.text = "Remaining: $remainingCount"
        initDismissButton(binding.toNextBatch)

        val incorrectRecyclerViewer: RecyclerView = dialogView.findViewById(R.id.answered)
        incorrectRecyclerViewer.adapter = WordAdapter(
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
            callback = object : WordCallback {
                override fun onSpell(icon: ImageView, text: TranslatedText) {
                    icon.setColorFilter(Color.argb(255, 255, 165, 0))
                    wordSpeller.spell(text) {
                        icon.setColorFilter(Color.argb(255, 41, 45, 54))
                    }
                }
            }
        )
        incorrectRecyclerViewer.layoutManager = LinearLayoutManager(context)
        dialog.setOnDismissListener {
            onNext()
        }
    }
}
