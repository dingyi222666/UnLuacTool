package com.dingyi.unluactool.ui.editor

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.base.BaseActivity
import com.dingyi.unluactool.common.adapter.ViewPageDataFragmentAdapter
import com.dingyi.unluactool.common.ktx.getJavaClass
import com.dingyi.unluactool.databinding.EditorBinding
import com.dingyi.unluactool.databinding.IncludeToolbarBinding
import com.dingyi.unluactool.repository.EditorRepository
import com.dingyi.unluactool.ui.editor.decompile.DecompileFragment
import com.dingyi.unluactool.ui.editor.drawer.DrawerFragment
import com.dingyi.unluactool.ui.editor.edit.EditFragment
import com.dingyi.unluactool.ui.editor.event.MenuListener
import com.dingyi.unluactool.ui.editor.fileTab.OpenedFileTabData
import com.dingyi.unluactool.ui.editor.main.MainFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditorActivity : BaseActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        EditorBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<EditorViewModel>()


    private val toolbar by lazy(LazyThreadSafetyMode.NONE) {
        IncludeToolbarBinding.bind(binding.root).toolbar
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_UnLuacTool)

        setContentView(binding.root)

        toolbar.title = getString(R.string.editor_toolbar_title_loading)

        initViewModel()

    }


    override fun isNeedInterceptBackEvent() = true


    override fun onDestroy() {
        super.onDestroy()

        val editorMainViewAdapter =
            binding.editorMainViewpager.adapter as ViewPageDataFragmentAdapter<OpenedFileTabData>

        editorMainViewAdapter.removeObservable(viewModel.editorUIFileTabManager.openedFileList)

        viewModel.eventManager.apply {
            clearListener(MenuListener.menuListenerEventType)
        }

        MainApplication.instance.applicationScope.launch {
            saveAllOpenedFileTab()
        }

    }

    private suspend inline fun saveAllOpenedFileTab() = viewModel.saveAllOpenedFileTab()

    override fun onBackEvent() {
        val currentFragmentData =
            checkNotNull(viewModel.editorUIFileTabManager.currentSelectOpenedFileTabData.value)
        val index = viewModel.editorUIFileTabManager.indexOfDataIndex(currentFragmentData)

        if (index > 0) {
            viewModel.editorUIFileTabManager.removeData(currentFragmentData)
            return
        }

        //TODO: File Save Check
        finish()

    }


    private fun initViewModel() = lifecycleScope.launch(Dispatchers.Main) {

        EditorRepository.getEventManager().dispatchEventOnUiThread()

        viewModel.loadProject(intent.getStringExtra("path") ?: "")

        val (progressDialog, func) = viewModel.openProject(
            this@EditorActivity, this@EditorActivity
        )

        progressDialog.show()

        func()

        viewModel.bindCoroutineScope(MainApplication.instance.applicationScope)

        viewModel.initFileTabDataList()

        viewModel.queryAllOpenedFileTab()

        EditorRepository.getEventManager().dispatchEventOnThreadPool()

        initView()

    }


    private fun initView() {

        // toolbar set
        toolbar.apply {
            title = getString(R.string.editor_toolbar_title)
            val name = viewModel.project.value?.name
            subtitle = name.toString()

            setOnMenuItemClickListener {
                viewModel.dispatchMenuClickEvent(it)
                return@setOnMenuItemClickListener true
            }
        }

        val actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.root, toolbar, 0, 0)

        binding.root.apply {
            addDrawerListener(actionBarDrawerToggle)
        }

        actionBarDrawerToggle.apply {
            syncState()
        }


        binding.editorMainViewpager.apply {
            val adapter = ViewPageDataFragmentAdapter<OpenedFileTabData>(this@EditorActivity)

            adapter.observableSource(viewModel.editorUIFileTabManager.openedFileList)

            adapter.createFragmentFunc = this@EditorActivity::createPagerFragment

            setAdapter(adapter)

            isUserInputEnabled = false

        }

        val fragmentManager = supportFragmentManager

        fragmentManager.commit {
            add(R.id.editor_drawer_fragment_container, getJavaClass<DrawerFragment>(), Bundle())
        }

        viewModel.editorUIFileTabManager.currentSelectOpenedFileTabData.observe(this) {
            onSelectEditorFragmentDataChange(it)
        }

        onSelectEditorFragmentDataChange(checkNotNull(viewModel.editorUIFileTabManager.currentSelectOpenedFileTabData.value))

    }

    private fun onSelectEditorFragmentDataChange(new: OpenedFileTabData) {
        val currentIndex = viewModel.editorUIFileTabManager.indexOfDataIndex(new)

        binding.editorMainViewpager.setCurrentItem(currentIndex, true)

        binding.root.closeDrawers()

        viewModel.eventManager.syncPublisher(MenuListener.menuListenerEventType)
            .onReloadMenu(toolbar, new)

        MainApplication.instance.applicationScope.launch {
            saveAllOpenedFileTab()
        }
    }


    private fun createPagerFragment(editorFragmentData: OpenedFileTabData): Fragment {
        val isUriNotEmpty = editorFragmentData.fileUri.isNotEmpty()
        if (isUriNotEmpty && !editorFragmentData.fileUri.endsWith("_decompile")) {
            val fragment = EditFragment()
            fragment.arguments = Bundle().apply {
                putString("fileUri", editorFragmentData.fileUri)
            }
            return fragment
        } else if (isUriNotEmpty && editorFragmentData.fileUri.endsWith("_decompile")) {
            val fragment = DecompileFragment()
            fragment.arguments = Bundle().apply {
                putString("fileUri", editorFragmentData.fileUri)
            }
            return fragment
        }
        return MainFragment()
    }


}