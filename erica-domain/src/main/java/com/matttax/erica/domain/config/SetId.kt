package com.matttax.erica.domain.config

sealed class SetId {
    object None: SetId()
    data class One(val id: Int): SetId()
    class Many(vararg val ids: Int): SetId()
}