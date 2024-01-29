package com.matttax.erica.utils

import com.matttax.erica.domain.config.SetId
import com.matttax.erica.domain.config.WordGroupConfig
import com.matttax.erica.domain.config.WordsSorting

object Utils {
    fun getLanguageCode(fullName: String) = when(fullName) {
        "Russian" -> "ru"
        "German" -> "de"
        "French" -> "fr"
        "Spanish" -> "es"
        "Italian" -> "it"
        else -> "en"
    }

    fun getConfigByPosition(id: Long, position: Int): WordGroupConfig {
        return when (position) {
            0 -> WordGroupConfig(
                setId = SetId.One(id),
                sorting = WordsSorting.LAST_ADDED_FIRST
            )
            1 -> WordGroupConfig(
                setId = SetId.One(id),
                sorting = WordsSorting.FIRST_ADDED_FIRST
            )
            2 -> WordGroupConfig(
                setId = SetId.One(id),
                sorting = WordsSorting.RECENTLY_ASKED_FIRST
            )
            3 -> WordGroupConfig(
                setId = SetId.One(id),
                sorting = WordsSorting.BEST_ANSWERED_FIRST
            )
            4 -> WordGroupConfig(
                setId = SetId.One(id),
                sorting = WordsSorting.WORST_ANSWERED_FIRST
            )
            else -> WordGroupConfig()
        }
    }
}
