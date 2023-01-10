package com.dingyi.unluactool.ui.editor

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.adapter.ViewPageDataFragmentAdapter
import com.dingyi.unluactool.databinding.EditorBinding
import com.dingyi.unluactool.databinding.IncludeToolbarBinding
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.core.file.OpenedFileManager
import com.dingyi.unluactool.core.service.get
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


    private fun initViewModel() {

        lifecycleScope.launch {
            val project = viewModel.loadProject(intent.getStringExtra("path") ?: "")
            val (progressDialog, func) = viewModel.openProject(
                this@EditorActivity,
                this@EditorActivity
            )
            progressDialog.show()
            func()

            val openedFileManager = globalServiceRegistry.get<OpenedFileManager>()

            openedFileManager.queryAllOpenedFile(project)
                .map { EditorFragmentData(it.publicURIString) }
                .let {
                    viewModel.fragmentDataList.addAll(it)
                }


            initView()
        }
    }

    private fun getToolBar(): Toolbar {
        return IncludeToolbarBinding.bind(binding.root).toolbar
    }

    private fun initView() {

        val actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.root, getToolBar(), 0, 0)

        binding.root.apply {
            addDrawerListener(actionBarDrawerToggle)

        }

        actionBarDrawerToggle.apply {
            syncState()
            drawerArrowDrawable.color =
                getAttributeColor(com.google.android.material.R.attr.colorOnPrimary)
        }

        // main page
        viewModel.fragmentDataList.add(0, EditorFragmentData(null))

        binding.editorMainViewpager.apply {
            val adapter = ViewPageDataFragmentAdapter<EditorFragmentData>(this@EditorActivity)
            adapter.observableSource(viewModel.fragmentDataList)

            adapter.createFragmentFunc = this@EditorActivity::createPagerFragment

            setAdapter(adapter)

            isUserInputEnabled = false


        }


        //toolbar set
        supportActionBar?.apply {
            title = getString(R.string.editor_toolbar_title)
            val name = viewModel.project.value?.name
            subtitle = name.toString()
            setDisplayHomeAsUpEnabled(true)
        }
    }


    private fun createPagerFragment(editorFragmentData: EditorFragmentData): Fragment {
        if (editorFragmentData.fileUri != null) {
            //TODO
        }
        return MainFragment()
    }


}