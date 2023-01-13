package com.dingyi.unluactool.ui.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showMessageDialog(
    context: Context,
    message: String,
): AlertDialog = MaterialAlertDialogBuilder(context)
    .apply {
        setMessage(message)
        setNegativeButton(android.R.string.cancel) { _, _ -> }
        setPositiveButton(android.R.string.ok) { _, _ -> }
    }
    .show()
