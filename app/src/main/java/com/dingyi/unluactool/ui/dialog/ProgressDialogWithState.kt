package com.dingyi.unluactool.ui.dialog

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.dingyi.unluactool.core.progress.ProgressState
import com.techiness.progressdialoglibrary.ProgressDialog

fun progressDialogWithState(
    modeConstant: Int = ProgressDialog.MODE_DETERMINATE,
    context: Context,
    themeConstant: Int =
        ProgressDialog.THEME_FOLLOW_SYSTEM,
    lifecycleOwner: LifecycleOwner = context as LifecycleOwner
): Pair<ProgressDialog, ProgressState> {

    val dialog = ProgressDialog(modeConstant, context, themeConstant)
    val state = ProgressState()
    state.observe(lifecycleOwner) {

           val (text, progress) = it
           dialog.setMessage(text)
           dialog.progress = progress

    }

    return dialog to state


}