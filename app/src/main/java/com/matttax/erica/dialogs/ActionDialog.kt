package com.matttax.erica.dialogs

import android.content.Context
import android.service.autofill.OnClickAction
import android.view.View
import com.google.android.material.button.MaterialButton

abstract class ActionDialog(context: Context, resource: Int): Dialog(context, resource) {
    lateinit var actionButton: MaterialButton

    fun initActionButton(buttonResource: Int, onClick: View.OnClickListener) {
        actionButton = dialogView.findViewById(buttonResource)
        actionButton.setOnClickListener(onClick)
    }
}