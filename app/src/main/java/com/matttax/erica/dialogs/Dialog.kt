package com.matttax.erica.dialogs

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.google.android.material.button.MaterialButton

abstract class Dialog(context: Context, resource: Int) {

    val dialog: AlertDialog
    protected val dialogView: View

    lateinit var dismissButton: MaterialButton

    init {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogView = (context as Activity).layoutInflater.inflate(resource, null)
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
