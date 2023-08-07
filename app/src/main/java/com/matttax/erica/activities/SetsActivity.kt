package com.matttax.erica.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.R
import com.matttax.erica.adaptors.SetAdaptor
import com.matttax.erica.WordSet
import com.matttax.erica.WordDBHelper
import com.matttax.erica.databinding.ActivityLearnBinding
import com.matttax.erica.databinding.ActivitySetsBinding
import com.matttax.erica.dialogs.CreateSetDialog


class SetsActivity : AppCompatActivity() {

    var sets = mutableListOf<WordSet>()
    val db = WordDBHelper(this)
    lateinit var binding: ActivitySetsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.addNewSet.setOnClickListener {
            CreateSetDialog(this, R.layout.create_set_dialog).showDialog()
        }

    }

    override fun onStart() {
        super.onStart()
        loadSets()
    }

    fun loadSets() {
        sets = db.getSets()
        binding.setsListRecyclerView.adapter = SetAdaptor(this, sets)
        binding.setsListRecyclerView.layoutManager = LinearLayoutManager(this)
    }

}