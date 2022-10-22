package com.matttax.erica

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SetsActivity : AppCompatActivity() {

    var sets = mutableListOf<SetOfWords>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sets)

        val mainlayout: LinearLayout = findViewById(R.id.sets_table)
        val rv: RecyclerView = findViewById(R.id.list)

        val button: Button = findViewById(R.id.fy)
        button.setOnClickListener {
            val db = WordDBHelper(this)
            db.deleteAll()
        }

        getSets()
        val adpt = SetAdaptor(this, sets)
        rv.adapter = adpt
        rv.layoutManager = LinearLayoutManager(this)

    }

    fun getSets() {
        val db = WordDBHelper(this)
        val cursor = db.getSet()
        if (cursor != null) {
            if (cursor.count != 0) {
                while (cursor.moveToNext()) {
                    sets += SetOfWords(cursor.getInt(0), cursor.getString(1), cursor.getString(3), cursor.getInt(2))
                }
            }
        }
    }


}