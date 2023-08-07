package com.matttax.erica.domain.model

import java.util.Locale

data class Language(
    val code: String,
    val fullName: String = getFullName(code),
    val locale: Locale = getLocale(code),
) {
    companion object {
        private fun getFullName(language: String) = when(language) {
            "ru" -> "russian"
            "es" -> "spanish"
            "de" -> "german"
            "fr" -> "french"
            "it" -> "italian"
            else -> "english"
        }

        private fun getLocale(language: String) = when(language) {
            "ru" -> Locale("ru", "")
            "es" -> Locale("es", "")
            "de" -> Locale.GERMAN
            "fr" -> Locale.FRENCH
            "it" -> Locale.ITALIAN
            else -> Locale.US
        }

        public val GERMAN = Language("de")
    }
}