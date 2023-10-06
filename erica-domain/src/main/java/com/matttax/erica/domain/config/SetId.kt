package com.matttax.erica.domain.config

sealed class SetId {
    object None: SetId()
    data class One(val id: Long): SetId()
    class Many(vararg val ids: Long): SetId()
}