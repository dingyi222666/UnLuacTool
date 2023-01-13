package com.dingyi.unluactool.ui.editor

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Space
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStateAtLeast
import androidx.recyclerview.widget.LinearLayoutManager
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.adapter.ViewPageDataFragmentAdapter
import com.dingyi.unluactool.common.ktx.dp
import com.dingyi.unluactool.databinding.EditorBinding
import com.dingyi.unluactool.databinding.IncludeToolbarBinding
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.common.ktx.getStatusBarHeight
import com.dingyi.unluactool.core.file.OpenedFileManager
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.databinding.EditorDrawerShipBinding
import com.dingyi.unluactool.ui.editor.adapter.EditorFileTabAdapter
import com.dingyi.unluactool.ui.editor.main.MainFragment
import kotlinx.coroutines.launch

class EditorActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        EditorBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<EditorViewModel>()

    private val globalServiceRegistry by lazy(LazyThreadSafetyMode.NONE) {
        MainApplication.instance.globalServiceRegistry
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_UnLuacTool)

        setContentView(binding.root)

        setSupportActionBar(getToolBar())
        supportActionBar?.apply {
            title = getString(R.string.editor_toolbar_title_loading)
        }

        initViewModel()

    }


    override fun onDestroy() {
        super.onDestroy()

        val editorMainViewAdapter =
            binding.editorMainViewpager.adapter as ViewPageDataFragmentAdapter<EditorFragmentData>

        editorMainViewAdapter.removeObservable(viewModel.fragmentDataList)

        val editorShipBinding = EditorDrawerShipBinding.bind(binding.root)

        val editorDrawerListAdapter =
            editorShipBinding.editorDrawerList.adapter as EditorFileTabAdapter

        editorDrawerListAdapter.removeObservable(viewModel.fragmentDataList)
    }

    private fun initViewModel() {

        lifecycleScope.launch {
            val project = viewModel.loadProject(intent.getStringExtra("path") ?: "")
            val (progressDialog, func) = viewModel.openProject(
                this@EditorActivity, this@EditorActivity
            )
            progressDialog.show()
            func()

            val openedFileManager = globalServiceRegistry.get<OpenedFileManager>()

            openedFileManager.queryAllOpenedFile(project)
                .map { EditorFragmentData(it.publicURIString) }.let {
                    viewModel.fragmentDataList.addAll(it)
                }

            viewModel.initFragmentDataList()

            initView()
        }
    }

    private fun getToolBar(): Toolbar {
        return IncludeToolbarBinding.bind(binding.root).toolbar
    }

    private fun initView() {

        val editorShipBinding = EditorDrawerShipBinding.bind(binding.root)

        //toolbar set
        supportActionBar?.apply {
            title = getString(R.string.editor_toolbar_title)
            val name = viewModel.project.value?.name
            subtitle = name.toString()
            setDisplayHomeAsUpEnabled(true)

            editorShipBinding.editorDrawerToolbarTitle.text = name
        }

        val actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.root, getToolBar(), 0, 0)

        binding.root.apply {
            addDrawerListener(actionBarDrawerToggle)
        }

        actionBarDrawerToggle.apply {
            syncState()
        }

        binding.editorMainViewpager.apply {
            val adapter = ViewPageDataFragmentAdapter<EditorFragmentData>(this@EditorActivity)

            adapter.observableSource(viewModel.fragmentDataList)

            adapter.createFragmentFunc = this@EditorActivity::createPagerFragment

            setAdapter(adapter)

        }

        editorShipBinding.apply {
            var isSetHeight = false
            editorDrawerInsets.setOnInsetsCallback {
                editorDrawerStatusBar.updateLayoutParams<ViewGroup.LayoutParams> {
                    if (isSetHeight) {
                        return@updateLayoutParams
                    }
                    height = it.top
                    isSetHeight = true
                }
            }

            editorDrawerList.apply {
                val adapter = EditorFileTabAdapter()
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter.apply {
                    observableSource(viewModel.fragmentDataList)
                    observableCurrentSelectData(this@EditorActivity,viewModel.currentSelectEditorFragmentData)
                }
                setAdapter(adapter)
            }
        }

    }


    private fun initEditorTabChangeListener() {
        TODO("TODO")
    }


    private fun createPagerFragment(editorFragmentData: EditorFragmentData): Fragment {
        if (editorFragmentData.fileUri != null) {
            //TODO
        }
        return MainFragment()
    }


}