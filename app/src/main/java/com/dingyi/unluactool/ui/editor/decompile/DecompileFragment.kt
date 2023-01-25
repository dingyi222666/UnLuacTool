package com.dingyi.unluactool.ui.editor.decompile

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dingyi.unluactool.common.base.BaseFragment
import com.dingyi.unluactool.databinding.FragmentEditorDecompileBinding

class DecompileFragment : BaseFragment<FragmentEditorDecompileBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditorDecompileBinding {
        return FragmentEditorDecompileBinding.inflate(inflater, container, false)
    }
}