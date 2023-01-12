package com.dingyi.unluactool.ui.editor.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dingyi.unluactool.base.BaseFragment
import com.dingyi.unluactool.databinding.FragmentEditorFileViewerBinding
import com.dingyi.unluactool.ui.editor.EditorViewModel

class FileViewerFragment : BaseFragment<FragmentEditorFileViewerBinding>() {

    private val viewModel by viewModels<EditorViewModel>()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditorFileViewerBinding {
        return FragmentEditorFileViewerBinding.inflate(inflater, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



    }
}