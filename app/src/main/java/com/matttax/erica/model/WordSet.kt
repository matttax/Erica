package com.matttax.erica.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WordSet(
    val id: Long,
    val name: String,
    val description: String,
    val wordsCount: Int,
) : Parcelable
