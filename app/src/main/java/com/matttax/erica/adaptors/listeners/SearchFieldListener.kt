package com.matttax.erica.adaptors.listeners

import androidx.appcompat.widget.SearchView

class SearchFieldListener(
    private val onSearch: (String) -> Unit
): SearchView.OnQueryTextListener {

    override fun onQueryTextSubmit(query: String?): Boolean = doSearch(query)
    override fun onQueryTextChange(newText: String?): Boolean = doSearch(newText)

    private fun doSearch(text: String?): Boolean {
        onSearch(text ?: "")
        return true
    }

    companion object {
        fun SearchView.setSearchListener(listener: (String) -> Unit) {
            setOnQueryTextListener(SearchFieldListener(listener))
        }
    }
}
