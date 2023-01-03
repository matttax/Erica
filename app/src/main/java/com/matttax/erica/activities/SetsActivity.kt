package com.matttax.erica.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matttax.erica.R
import com.matttax.erica.adaptors.SetAdaptor
import com.matttax.erica.WordSet
import com.matttax.erica.WordDBHelper
import com.matttax.erica.dialogs.CreateSetDialog


class SetsActivity : AppCompatActivity() {

    var sets = mutableListOf<WordSet>()
    val db = WordDBHelper(this)

    private lateinit var setsListRecyclerView: RecyclerView
    lateinit var add: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sets)

        setsListRecyclerView = findViewById(R.id.list)
        add = findViewById(R.id.addNewSet)
        add.setOnClickListener {
            CreateSetDialog(this, R.layout.create_set_dialog).showDialog()
        }

    }

    override fun onResume() {
        super.onResume()
        loadSets()
    }

    fun loadSets() {
        sets = db.getSets()
        setsListRecyclerView.adapter = SetAdaptor(this, sets)
        setsListRecyclerView.layoutManager = LinearLayoutManager(this)
    }

}