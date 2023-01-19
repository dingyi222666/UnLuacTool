package com.dingyi.unluactool.ui.editor.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.base.BaseFragment
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.common.ktx.showSnackBar
import com.dingyi.unluactool.databinding.FragmentEditorEditBinding
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.ui.editor.EditorViewModel
import com.dingyi.unluactool.ui.editor.event.MenuEvent
import com.dingyi.unluactool.ui.editor.event.MenuListener
import com.dingyi.unluactool.ui.editor.fileTab.OpenedFileTabData
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.EventReceiver
import io.github.rosemoe.sora.event.SubscriptionReceipt
import io.github.rosemoe.sora.event.Unsubscribe
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.widget.subscribeEvent
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class EditFragment : BaseFragment<FragmentEditorEditBinding>(), MenuListener, MenuEvent {

    private val viewModel by activityViewModels<EditorViewModel>()

    private val vfsManager by lazy(LazyThreadSafetyMode.NONE) {
        viewModel.vfsManager
    }

    private val eventManager by lazy(LazyThreadSafetyMode.NONE) {
        viewModel.eventManager
    }

    private val editorChangeEventReceiver = EditorChangeEventReceiver()

    private var toolbar = WeakReference<Toolbar>(null)

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

        val fileUri = arguments?.getString("fileUri") ?: ""

        currentOpenFileObject = vfsManager.resolveFile(fileUri) as UnLuaCFileObject

        initEditor()

        openFile()

    }

    private fun listenerEditorContentChange() {
        viewModel.queryOpenedFileTab(currentOpenFileObject)
            .isNotSaveEditContent
            .observe(viewLifecycleOwner) { isNotSaveEdit ->
                toolbar.get()?.let { toolbarTarget ->
                    toolbarTarget.menu.let { menu ->
                        val saveItem = menu.findItem(R.id.editor_menu_save)
                        saveItem.isEnabled = isNotSaveEdit
                    }
                    toolbarTarget.invalidateMenu()
                }
            }
    }


    private fun updateMenuState() {
        val editor = binding.editor
        toolbar.get()?.let { toolbarTarget ->
            toolbarTarget.menu.let { menu ->
                val undoItem = menu.findItem(R.id.editor_menu_code_undo)
                val redoItem = menu.findItem(R.id.editor_menu_code_redo)
                undoItem.isEnabled = editor.canUndo()
                redoItem.isEnabled = editor.canRedo()
            }
            toolbarTarget.invalidateMenu()
        }
    }

    private fun initEditor() {

        val editor = binding.editor

        editor.colorScheme.apply {
            setColor(
                EditorColorScheme.WHOLE_BACKGROUND,
                getAttributeColor(android.R.attr.colorBackground)
            )
            editor.colorScheme = this
        }

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

    override fun onReloadMenu(toolbar: Toolbar, currentFragmentData: OpenedFileTabData) {
        if (currentFragmentData.fileUri != currentOpenFileObject.name.friendlyURI || isDetached) {
            return
        }

        val menu = toolbar.menu

        menu.clear()

        toolbar.apply {
            title = ""
            subtitle = ""

            this@EditFragment.toolbar = WeakReference(this)
        }

        requireActivity().menuInflater.inflate(R.menu.editor_edit, menu)
    }

    override fun onClick(menuItem: MenuItem) {
        if (viewModel.editorUIFileTabManager.currentSelectOpenedFileTabData.value?.fileUri != currentOpenFileObject.name.friendlyURI || isDetached) {
            return
        }

        when (menuItem.itemId) {
            R.id.editor_menu_save -> {
                lifecycleScope.launch {
                    viewModel.saveFile(currentOpenFileObject)
                    viewModel.contentChangeFile(binding.editor, currentOpenFileObject)
                    getString(R.string.editor_save_successful).showSnackBar(
                        binding.root
                    )
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        eventManager.unsubscribe(MenuListener.menuListenerEventType, this)
        eventManager.unsubscribe(MenuEvent.eventType, this)
        kotlin.runCatching {
            subscriptionReceipt?.unsubscribe()
        }
    }

    override fun onResume() {
        super.onResume()
        listenerEditorContentChange()
        eventManager.subscribe(MenuListener.menuListenerEventType, this)
        eventManager.subscribe(MenuEvent.eventType, this)
        kotlin.runCatching {
            subscriptionReceipt = binding.editor.subscribeEvent(editorChangeEventReceiver)
        }
    }

    inner class EditorChangeEventReceiver : EventReceiver<ContentChangeEvent> {
        override fun onReceive(event: ContentChangeEvent, unsubscribe: Unsubscribe) {
            viewModel.contentChangeFile(event.editor, currentOpenFileObject)
            updateMenuState()
        }

    }


}