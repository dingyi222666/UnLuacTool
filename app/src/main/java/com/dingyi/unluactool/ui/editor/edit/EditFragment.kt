package com.dingyi.unluactool.ui.editor.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.base.BaseFragment
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.databinding.FragmentEditorEditBinding
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.ui.editor.EditorViewModel
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.launch
import org.apache.commons.vfs2.impl.DefaultFileMonitor

class EditFragment : BaseFragment<FragmentEditorEditBinding>() {


    private val viewModel by activityViewModels<EditorViewModel>()

    private val vfsManager by lazy(LazyThreadSafetyMode.NONE) {
        viewModel.vfsManager
    }

    private lateinit var currentOpenFileObject: UnLuaCFileObject

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

        currentOpenFileObject = vfsManager.resolveFile(fileUri) as UnLuaCFileObject

        editor.colorScheme.apply {
            setColor(
                EditorColorScheme.WHOLE_BACKGROUND,
                getAttributeColor(android.R.attr.colorBackground)
            )
            editor.colorScheme = this
        }

        openFile()
    }

    private fun openFile() {

        lifecycleScope.launch {

            binding.editor.isVisible = false
            binding.editorEditFragmentProgressBar.isVisible = true

            binding.editor.setText(viewModel.openFile(currentOpenFileObject))

            binding.editor.isVisible = true
            binding.editorEditFragmentProgressBar.isVisible = false

        }

    }


}