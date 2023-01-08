package com.dingyi.unluactool.ui.editor.main

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dingyi.unluactool.base.BaseFragment
import com.dingyi.unluactool.databinding.FragmentEditorMainViewerBinding

class MainFragment : BaseFragment<FragmentEditorMainViewerBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditorMainViewerBinding {
        return FragmentEditorMainViewerBinding.inflate(inflater, container, false)
    }


}