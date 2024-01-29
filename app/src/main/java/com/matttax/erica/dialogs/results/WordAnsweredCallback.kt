package com.matttax.erica.dialogs.results

interface WordAnsweredCallback {
    fun onOk()
    fun onNotIncorrect()
    fun onShowHint()
}
