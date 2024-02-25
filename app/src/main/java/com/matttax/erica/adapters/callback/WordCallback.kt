package com.matttax.erica.adapters.callback

import android.widget.ImageView
import com.matttax.erica.presentation.model.translate.TranslatedText

interface WordCallback {
    fun onClick(position: Int) {}
    fun onEditClick(position: Int) {}
    fun onDeleteClick(position: Int) {}
    fun onSpell(icon: ImageView, text: TranslatedText) {}
}
