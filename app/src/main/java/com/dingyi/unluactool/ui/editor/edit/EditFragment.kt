package com.dingyi.unluactool.ui.editor.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dingyi.unluactool.R
import com.dingyi.unluactool.base.BaseFragment
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.databinding.FragmentEditorEditBinding
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

class EditFragment : BaseFragment<FragmentEditorEditBinding>() {


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditorEditBinding {
        return FragmentEditorEditBinding.inflate(inflater, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editor = binding.editor
        val fileUri = arguments?.getString("fileUri") ?: ""

        editor.colorScheme.apply {
            setColor(
                EditorColorScheme.WHOLE_BACKGROUND,
                getAttributeColor(android.R.attr.colorBackground)
            )
            editor.colorScheme = this
        }

        openFile(fileUri)
    }

    private fun openFile(fileUri: String) {

    }


}