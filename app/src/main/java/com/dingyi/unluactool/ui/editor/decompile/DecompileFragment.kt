package com.dingyi.unluactool.ui.editor.decompile

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
import com.dingyi.unluactool.core.editor.EditorConfigManager
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.databinding.FragmentEditorDecompileBinding
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.ui.dialog.gotoDialog
import com.dingyi.unluactool.ui.editor.EditorViewModel
import com.dingyi.unluactool.ui.editor.event.MenuEvent
import com.dingyi.unluactool.ui.editor.event.MenuListener
import com.dingyi.unluactool.ui.editor.fileTab.OpenedFileTabData
import io.github.rosemoe.sora.event.PublishSearchResultEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.event.subscribeEvent
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class  DecompileFragment : BaseFragment<FragmentEditorDecompileBinding>(), MenuListener, MenuEvent {

    private val viewModel by activityViewModels<EditorViewModel>()

    private val vfsManager by lazy(LazyThreadSafetyMode.NONE) {
        viewModel.vfsManager
    }

    private val eventManager by lazy(LazyThreadSafetyMode.NONE) {
        viewModel.eventManager
    }

    private val subEditorEventManager by lazy(LazyThreadSafetyMode.NONE) {
        binding.editor.createSubEventManager()
    }


    private val globalServiceRegistry by lazy(LazyThreadSafetyMode.NONE) {
        MainApplication.instance.globalServiceRegistry
    }

    private var toolbar = WeakReference<Toolbar>(null)

    private lateinit var currentOpenFileObject: UnLuaCFileObject

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditorDecompileBinding {
        return FragmentEditorDecompileBinding.inflate(inflater, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fileUri = arguments?.getString("fileUri") ?: ""

        currentOpenFileObject = vfsManager.resolveFile(fileUri) as UnLuaCFileObject

        initView()

        openFile()

    }

    private fun initView() {

        binding.apply {
            editorEditFunctionName.text = currentOpenFileObject.getFunctionFullName() ?: ""
        }

        initEditor()
    }


    private fun initEditor() {

        val editor = binding.editor

        val editorConfigManager = globalServiceRegistry.get<EditorConfigManager>()

        val fontData = editorConfigManager.font
        val font = fontData.value ?: Typeface.MONOSPACE

        editor.typefaceText = font
        editor.typefaceLineNumber = editor.typefaceText


        subEditorEventManager.apply {
            subscribeEvent<SelectionChangeEvent> { _, _ -> updatePositionText() }
            subscribeEvent<PublishSearchResultEvent> { _, _ -> updatePositionText() }
        }

        updatePositionText()

        if (fontData.value != font) {
            fontData.observe(this@DecompileFragment.viewLifecycleOwner) {
                editor.typefaceText = it
                editor.typefaceLineNumber = editor.typefaceText
                editor.setEditorLanguage(editorConfigManager.getLanguage(currentOpenFileObject))
                editor.colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            }
        } else {
            editor.setEditorLanguage(editorConfigManager.getLanguage(currentOpenFileObject))
            editor.colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())

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
          //  binding.editor.editable = false
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
            title = currentOpenFileObject.getFunctionName()
            subtitle = ""
        }

        requireActivity().menuInflater.inflate(R.menu.editor_decompile, menu)
    }

    override fun onClick(menuItem: MenuItem) {
        if (viewModel.editorUIFileTabManager.currentSelectOpenedFileTabData.value?.fileUri != currentOpenFileObject.name.friendlyURI || isDetached) {
            return
        }

        when (menuItem.itemId) {
            R.id.editor_menu_code_redo -> binding.editor.redo()
            R.id.editor_menu_code_undo -> binding.editor.undo()
            R.id.editor_menu_code_goto -> gotoDialog(requireActivity(), binding.editor)
            R.id.editor_menu_edit_fragment_close -> {
                val fileTabManager = viewModel.editorUIFileTabManager
                fileTabManager.removeData(fileTabManager.queryOpenedFileTab(currentOpenFileObject))
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
        eventManager.subscribe(MenuListener.menuListenerEventType, this)
        eventManager.subscribe(MenuEvent.eventType, this, stickyEvent = false)
        subEditorEventManager.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        subEditorEventManager.detach()
    }

}