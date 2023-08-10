package com.matttax.erica.utils

class LanguageUtils {
    companion object {
        fun getLanguageCode(fullName: String) = when(fullName) {
            "Russian" -> "ru"
            "German" -> "de"
            "French" -> "fr"
            "Spanish" -> "es"
            "Italian" -> "it"
            else -> "en"
        }
    }
}
