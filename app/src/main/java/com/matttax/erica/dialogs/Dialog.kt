package com.matttax.erica.dialogs

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.google.android.material.button.MaterialButton
import com.matttax.erica.WordDBHelper

abstract class Dialog(activity: Context, resource: Int) {

    val dialog: AlertDialog
    protected val dialogView: View
    protected val db: WordDBHelper

    lateinit var dismissButton: MaterialButton

    init {
        db = WordDBHelper(activity)

        val dialogBuilder = AlertDialog.Builder(activity)
        dialogView = (activity as Activity).layoutInflater.inflate(resource, null)
        dialogBuilder.setView(dialogView)
        dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun initDismissButton(buttonResource: Int) {
        dismissButton = dialogView.findViewById(buttonResource)
        dismissButton.setOnClickListener {
            dialog.dismiss()
        }

    }

    fun showDialog() {
        dialog.show()
    }
}
