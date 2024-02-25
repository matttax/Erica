package com.matttax.erica.dialogs.selection

import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import com.matttax.erica.R
import com.matttax.erica.databinding.MoveDialogBinding
import com.matttax.erica.dialogs.ActionDialog

class MoveDialog(
    context: Context,
    sets: Collection<String>,
    onAction: (Int) -> Unit
): ActionDialog<MoveDialogBinding>(
    MoveDialogBinding.inflate(LayoutInflater.from(context))
) {

    init {
        binding.setsToMove.adapter = ArrayAdapter(context, R.layout.sets_spinner_item, sets.toList())
        initDismissButton(binding.noMove)
        initActionButton(binding.yesMove) {
            onAction(binding.setsToMove.selectedItemPosition)
            dialog.dismiss()
        }
    }
}
