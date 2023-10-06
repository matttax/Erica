package com.matttax.erica.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.matttax.erica.model.WordSet

interface ChoiceNavigator {
    fun showWords(set: WordSet)
    fun backToSets()
    fun notifyWordsSelected(areSelected: Boolean)
    fun listenBackPressed(lifecycleOwner: LifecycleOwner, listener: () -> Unit)

    companion object {
        const val SHARED_PREFS_NAME = "ericaPrefs"
        const val SHARED_PREFS_POSITION_KEY = "ORDER_POS"

        const val SET_ID_EXTRA_NAME = "set_id"
        const val SET_NAME_EXTRA_NAME = "set_name"
        const val SET_DESCRIPTION_EXTRA_NAME = "set_description"
        const val WORD_COUNT_EXTRA_NAME = "words_count"
    }
}

fun Fragment.getChoiceNavigator(): ChoiceNavigator {
    return requireActivity() as ChoiceNavigator
}
