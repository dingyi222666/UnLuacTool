package com.dingyi.unluactool.ui.editor.edit

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.base.BaseFragment
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.common.ktx.showSnackBar
import com.dingyi.unluactool.core.editor.EditorConfigManager
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.databinding.FragmentEditorEditBinding
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.ui.editor.EditorViewModel
import com.dingyi.unluactool.ui.editor.event.MenuEvent
import com.dingyi.unluactool.ui.editor.event.MenuListener
import com.dingyi.unluactool.ui.editor.fileTab.OpenedFileTabData
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.EventReceiver
import io.github.rosemoe.sora.event.PublishSearchResultEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.event.SubscriptionReceipt
import io.github.rosemoe.sora.event.Unsubscribe
import io.github.rosemoe.sora.event.subscribeEvent
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.widget.subscribeEvent
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.lang.ref.WeakReference

class EditFragment : BaseFragment<FragmentEditorEditBinding>(), MenuListener, MenuEvent {

    private val viewModel by activityViewModels<EditorViewModel>()

    private val vfsManager by lazy(LazyThreadSafetyMode.NONE) {
        viewModel.vfsManager
    }

    private val globalServiceRegistry by lazy(LazyThreadSafetyMode.NONE) {
        MainApplication.instance.globalServiceRegistry
    }

    private val eventManager by lazy(LazyThreadSafetyMode.NONE) {
        viewModel.eventManager
    }

    private val subEditorEventManager by lazy(LazyThreadSafetyMode.NONE) {
        binding.editor.createSubEventManager()
    }

    private var toolbar = WeakReference<Toolbar>(null)

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

        initView()

        openFile()

    }

    private fun listenerEditorContentChange() {
        viewModel.queryOpenedFileTab(currentOpenFileObject)
            .isNotSaveEditContent
            .observe(viewLifecycleOwner) { isNotSaveEdit ->
                toolbar.get()?.let { toolbarTarget ->
                    toolbarTarget.menu.let { menu ->
                        val saveItem: MenuItem? = menu.findItem(R.id.editor_menu_save)
                        saveItem?.isEnabled = isNotSaveEdit
                    }
                    toolbarTarget.invalidateMenu()
                }
            }
    }

    private fun initView() {

        binding.apply {
            editorEditFunctionName.text = currentOpenFileObject.getFunctionFullName() ?: ""
        }

        initEditor()
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

        val newColorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())

        val editorConfigManager = globalServiceRegistry.get<EditorConfigManager>()

        val fontData = editorConfigManager.font
        val font = fontData.value ?: Typeface.MONOSPACE

        newColorScheme.setColor(
            EditorColorScheme.WHOLE_BACKGROUND,
            getAttributeColor(android.R.attr.colorBackground)
        )
        editor.colorScheme = newColorScheme
        editor.typefaceText = font
        editor.typefaceLineNumber = editor.typefaceText


        subEditorEventManager.apply {
            subscribeEvent<ContentChangeEvent> { event, _ ->
                viewModel.contentChangeFile(event.editor, currentOpenFileObject)
                updateMenuState()
            }
            subscribeEvent<SelectionChangeEvent> { _, _ -> updatePositionText() }
            subscribeEvent<PublishSearchResultEvent> { _, _ -> updatePositionText() }
        }

        updatePositionText()

        if (fontData.value != font) {
            fontData.observe(this@EditFragment.viewLifecycleOwner) {
                editor.typefaceText = it
                editor.typefaceLineNumber = editor.typefaceText
                editor.setEditorLanguage(editorConfigManager.getLanguage(currentOpenFileObject))
            }
        } else {
            editor.setEditorLanguage(editorConfigManager.getLanguage(currentOpenFileObject))

        }


    }

    private fun updatePositionText() {
        val cursor = binding.editor.cursor
        val leftCharPosition = cursor.left()

        val searcher = binding.editor.searcher
        val searcherHasQuery = searcher.hasQuery()
        val text = when {
            searcherHasQuery && searcher.matchedPositionCount == 0 -> {
                getString(
                    R.string.editor_edit_cursor_position_search_format_empty,
                    leftCharPosition.line + 1, leftCharPosition.column, leftCharPosition.index
                )
            }

            searcherHasQuery && searcher.matchedPositionCount == 1 -> {
                getString(
                    R.string.editor_edit_cursor_position_search_format_default,
                    leftCharPosition.line + 1, leftCharPosition.column, leftCharPosition.index,
                    searcher.currentMatchedPositionIndex + 1, searcher.matchedPositionCount
                )
            }

            searcherHasQuery && searcher.matchedPositionCount > 1 -> {
                getString(
                    R.string.editor_edit_cursor_position_search_format_single,
                    leftCharPosition.line + 1, leftCharPosition.column, leftCharPosition.index,
                )
            }

            cursor.isSelected ->
                getString(
                    R.string.editor_edit_cursor_position_select_format,
                    leftCharPosition.line + 1, leftCharPosition.column, leftCharPosition.index,
                    cursor.right - cursor.left
                )

            else -> {
                getString(
                    R.string.editor_edit_cursor_position_format,
                    leftCharPosition.line + 1,
                    leftCharPosition.column,
                    leftCharPosition.index,
                )
            }
        }


        binding.editorEditCursorPosition.text = text
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

    private fun openFileObject(fileObject: UnLuaCFileObject) {
        viewModel.openFileObject(fileObject)
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

            R.id.editor_menu_code_as_lua -> {
                openFileObject(currentOpenFileObject.resolveFile("_decompile") as UnLuaCFileObject)
            }
        }

    }

    override fun onPause() {
        super.onPause()
        eventManager.unsubscribe(MenuListener.menuListenerEventType, this)
        eventManager.unsubscribe(MenuEvent.eventType, this)
        subEditorEventManager.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        listenerEditorContentChange()
        eventManager.subscribe(MenuListener.menuListenerEventType, this)
        eventManager.subscribe(MenuEvent.eventType, this, stickyEvent = false)
        subEditorEventManager.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        subEditorEventManager.detach()
    }


}