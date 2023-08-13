package com.matttax.erica.presentation.model.translate

import com.matttax.erica.domain.model.translate.UsageExample

data class TranslatedTextCard(
    val translatedText: TranslatedText,
    val isEditable: Boolean,
    val isSelected: Boolean,
    val state: TextCardState
) {
    companion object {
        fun fromUsageExample(usageExample: UsageExample) = TranslatedTextCard(
            translatedText = TranslatedText(
                usageExample.text,
                usageExample.translation,
                usageExample.textLanguage,
                usageExample.translationLanguage
            ),
            isEditable = false,
            isSelected = false,
            state = TextCardState.DEFAULT
        )
    }
}
