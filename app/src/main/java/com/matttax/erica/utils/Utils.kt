package com.matttax.erica.utils

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.matttax.erica.domain.config.SetId
import com.matttax.erica.domain.config.WordGroupConfig
import com.matttax.erica.domain.config.WordsSorting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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

    fun Activity.getScope() = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun Activity.launchSuspend(action: suspend () -> Unit) = getScope().launch { action() }

    fun Fragment.launchSuspend(action: suspend () -> Unit) =
        viewLifecycleOwner.lifecycleScope.launch { action() }
}
