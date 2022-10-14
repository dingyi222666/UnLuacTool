package com.dingyi.unluactool.ui.editor

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.R
import com.dingyi.unluactool.databinding.EditorBinding
import com.dingyi.unluactool.databinding.IncludeToolbarBinding
import com.dingyi.unluactool.databinding.MainNavigationHeadBinding
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.ui.main.MainViewPagerAdapter
import kotlinx.coroutines.launch

class EditorActivity : AppCompatActivity() {


    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        EditorBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<EditorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_UnLuacTool)

        setContentView(binding.root)

        setSupportActionBar(getToolBar())


        //toolbar set
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(true)
        }


        initViewModel()

        initView()


    }


    private fun initViewModel() {

        lifecycleScope.launch {
            viewModel.loadProject(intent.getStringExtra("path") ?: "")
            val (progressDialog,func) = viewModel.openProject(this@EditorActivity, this@EditorActivity)
            progressDialog.show()
            func()
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


    }


}