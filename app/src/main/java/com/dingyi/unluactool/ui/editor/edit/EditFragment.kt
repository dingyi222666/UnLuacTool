package com.dingyi.unluactool.ui.editor.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.base.BaseFragment
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.databinding.FragmentEditorEditBinding
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.repository.EditorRepository
import com.dingyi.unluactool.ui.editor.EditorViewModel
import com.dingyi.unluactool.ui.editor.event.MenuListener
import com.dingyi.unluactool.ui.editor.fileTab.OpenedFileTabData
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.EventReceiver
import io.github.rosemoe.sora.event.SubscriptionReceipt
import io.github.rosemoe.sora.event.Unsubscribe
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.widget.subscribeEvent
import kotlinx.coroutines.launch

class EditFragment : BaseFragment<FragmentEditorEditBinding>(), MenuListener {

    private val viewModel by activityViewModels<EditorViewModel>()

    private val vfsManager by lazy(LazyThreadSafetyMode.NONE) {
        viewModel.vfsManager
    }

    private val eventManager by lazy(LazyThreadSafetyMode.NONE) {
        viewModel.eventManager
    }

    private val editorChangeEventReceiver = EditorChangeEventReceiver()

    private var subscriptionReceipt: SubscriptionReceipt<ContentChangeEvent>? = null

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

        subscriptionReceipt = editor.subscribeEvent(editorChangeEventReceiver)

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

    override fun onReload(toolbar: Toolbar, currentFragmentData: OpenedFileTabData) {
        if (currentFragmentData.fileUri != currentOpenFileObject.name.friendlyURI || isDetached) {
            return
        }

        val menu = toolbar.menu
        menu.clear()

        toolbar.apply {
            title = ""
            subtitle = ""
        }

        requireActivity().menuInflater.inflate(R.menu.editor_edit, menu)
    }


    override fun onPause() {
        super.onPause()
        eventManager.unsubscribe(MenuListener.menuListenerEventType, this)
        kotlin.runCatching {
            subscriptionReceipt?.unsubscribe()
        }
    }

    override fun onResume() {
        super.onResume()
        eventManager.subscribe(MenuListener.menuListenerEventType, this)
        kotlin.runCatching {
            subscriptionReceipt = binding.editor.subscribeEvent(editorChangeEventReceiver)
        }
    }

    inner class EditorChangeEventReceiver : EventReceiver<ContentChangeEvent> {
        override fun onReceive(event: ContentChangeEvent, unsubscribe: Unsubscribe) {
            viewModel.contentChangeFile(event, currentOpenFileObject)
        }

    }

}