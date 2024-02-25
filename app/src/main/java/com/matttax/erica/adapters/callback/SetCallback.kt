package com.matttax.erica.adapters.callback

interface SetCallback {
    fun onClick(position: Int) {}
    fun onLearnClick(position: Int) {}
    fun onEditClick(position: Int) {}
    fun onDeleteClick(position: Int) {}
}