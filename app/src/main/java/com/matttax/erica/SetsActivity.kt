package com.matttax.erica

import android.R.attr.data
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText


class SetsActivity : AppCompatActivity() {

    var sets = mutableListOf<SetOfWords>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sets)

        val mainlayout: LinearLayout = findViewById(R.id.sets_table)
        val rv: RecyclerView = findViewById(R.id.list)

        getSets()
        val adpt = SetAdaptor(this, sets)
        rv.adapter = adpt
        rv.layoutManager = LinearLayoutManager(this)

        val add: CardView = findViewById(R.id.addNewSet)
        add.setOnClickListener {
            val bld = AlertDialog.Builder(this)
            val vwy = layoutInflater.inflate(R.layout.create_set_dialog, null)
            bld.setView(vwy)
            val dlg = bld.create()
            dlg.show()

            val nm: TextInputEditText = vwy.findViewById(R.id.setNameField)
            val dr: TextInputEditText = vwy.findViewById(R.id.setDescriptionField)
            val ad: MaterialButton = vwy.findViewById(R.id.addSet)
            val dm: MaterialButton = vwy.findViewById(R.id.dismissSet)
            ad.setOnClickListener {
                if (nm.text!!.isNotEmpty()) {
                    val db = WordDBHelper(this)
                    db.addSet(nm.text.toString(), dr.text.toString())
                    dlg.dismiss()
                } else Toast.makeText(this, "Input name", Toast.LENGTH_LONG).show()
            }
            dm.setOnClickListener {
                dlg.dismiss()
            }
        }

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