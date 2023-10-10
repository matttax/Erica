package com.matttax.erica.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppSettings @Inject constructor(
    @ApplicationContext val context: Context
) {
    private val preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    var fromLanguageId: Int = 0
        get() = preferences.getInt(FROM_LANGUAGE_KEY, 0)
        set(value) {
            preferences.edit {
                putInt(FROM_LANGUAGE_KEY, value)
                apply()
            }.also { field = value }
        }

    var toLanguageId: Int = 0
        get() = preferences.getInt(TO_LANGUAGE_KEY, 0)
        set(value) {
            preferences.edit {
                putInt(TO_LANGUAGE_KEY, value)
                apply()
            }.also { field = value }
        }

    var studyPriorityId: Int = 0
        get() = preferences.getInt(STUDY_PRIORITY_KEY, 0)
        set(value) {
            preferences.edit(commit = true) {
                putInt(STUDY_PRIORITY_KEY, value)
            }.also { field = value }
        }

    var wordsOrderId: Int = 0
        get() {
            Log.i("words get", preferences.getInt(WORDS_ORDER_KEY, -1).toString())
            return preferences.getInt(WORDS_ORDER_KEY, 0)
        }
        set(value) {
            preferences.edit(commit = true) {
                putInt(WORDS_ORDER_KEY, value)
            }.also {
                field = value
                Log.i("words set", value.toString())
            }
        }

    companion object {
        const val SHARED_PREFS_NAME = "ericaPrefs"

        const val FROM_LANGUAGE_KEY = "FROM"
        const val TO_LANGUAGE_KEY = "TO"
        const val STUDY_PRIORITY_KEY = "STUDY_POS"
        const val WORDS_ORDER_KEY = "ORDER_POS"
    }
}
