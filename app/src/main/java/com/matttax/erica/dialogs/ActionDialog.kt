package com.matttax.erica.dialogs

import android.view.View
import android.widget.Button
import androidx.viewbinding.ViewBinding

abstract class ActionDialog<VB: ViewBinding>(
    binding: VB
): Dialog<VB>(binding) {

    private lateinit var actionButton: Button

    fun initActionButton(button: Button, onClick: View.OnClickListener) {
        actionButton = button.apply {
            setOnClickListener(onClick)
        }
    }
}
