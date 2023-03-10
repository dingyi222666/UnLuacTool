package com.dingyi.unluactool.ui.dialog

import android.app.Activity
import android.app.Dialog
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.ktx.getString
import com.dingyi.unluactool.databinding.DialogEditorGotoBinding
import com.dingyi.unluactool.databinding.DialogNavigationBinding
import com.dingyi.unluactool.engine.suggest.CodeNavigation
import com.dingyi.unluactool.ui.dialog.adapter.NavigationAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.rosemoe.sora.widget.CodeEditor
import kotlin.math.max

fun gotoDialog(context: Activity, editor: CodeEditor) {
    val binding = DialogEditorGotoBinding.inflate(context.layoutInflater)
    val maxLine = editor.lineCount
    val dialog = MaterialAlertDialogBuilder(context)
        .apply {
            setTitle(R.string.editor_menu_code_goto)
            setView(binding.root)
            setNegativeButton(android.R.string.cancel) { _, _ ->
            }
            setPositiveButton(android.R.string.ok) { _, _ -> }
        }
        .show()

    val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)

    positiveButton.setOnClickListener {
        val text = binding.gotoEdit.text.toString()
        if (text.isBlank()) {
            return@setOnClickListener
        }
        val targetLine = runCatching { text.toInt() }.getOrNull() ?: (maxLine + 1)
        if (targetLine > maxLine) {
            binding.gotoEditGroup.error = getString(R.string.editor_edit_dialog_goto_error)
        } else {
            editor.jumpToLine(max(0, targetLine - 1))
            dialog.dismiss()
        }
    }

    binding.gotoEdit.addTextChangedListener(
        onTextChanged = { text, _, _, _ ->
            positiveButton.isEnabled = text.isNullOrBlank().not()
        }
    )

    binding.gotoEdit.setText("")

    binding.gotoEditGroup.hint =
        getString(R.string.editor_edit_dialog_goto_hint, 1, maxLine)
}


fun navigationDialog(context: Activity, editor: CodeEditor,navigationList: List<CodeNavigation>) {
    val binding = DialogNavigationBinding.inflate(context.layoutInflater)
    val dialog = MaterialAlertDialogBuilder(context)
        .apply {
            setTitle(R.string.editor_menu_code_explore)
            setView(binding.root)
            setNegativeButton(android.R.string.cancel) { _, _ ->
            }
            setPositiveButton(android.R.string.ok) { _, _ -> }
        }
        .show()

    binding.list.apply {
        layoutManager = LinearLayoutManager(context)
        adapter = NavigationAdapter().apply {
            submitList(navigationList)
            onItemClick {
                editor.jumpToLine(it.position.line)
                dialog.dismiss()
            }
        }
    }

    dialog.show()
}
