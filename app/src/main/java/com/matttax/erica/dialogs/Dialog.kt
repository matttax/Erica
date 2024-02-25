package com.matttax.erica.dialogs

import androidx.appcompat.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import androidx.viewbinding.ViewBinding

abstract class Dialog<VB: ViewBinding>(
    protected val binding: VB
) {

    protected val dialogView: View = binding.root
    protected val dialog: AlertDialog = AlertDialog.Builder(binding.root.context).setView(dialogView).create()
        .also { it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) }

    lateinit var dismissButton: Button

    protected fun initDismissButton(button: Button) {
        dismissButton = button
        dismissButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun showDialog() {
        dialog.show()
    }
}
