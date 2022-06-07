package com.dingyi.unluactool.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.R
import com.dingyi.unluactool.databinding.IncludeToolbarBinding
import com.dingyi.unluactool.databinding.MainBinding
import com.dingyi.unluactool.databinding.MainNavigationHeadBinding
import com.dingyi.unluactool.ui.ktx.getAttributeColor
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        MainBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(getToolBar())


        //toolbar set
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(true)
        }



        initViewModel()

        initView()

        lifecycleScope.launch {
            refreshData()
        }
    }


    private suspend fun refreshData() {
        viewModel.refreshHitokoto()
    }

    private fun initViewModel() {
        val navigationHeadBinding = MainNavigationHeadBinding
            .bind(binding.mainNavigationView.getHeaderView(0))
        viewModel.hitokoto.observe(this) {
            navigationHeadBinding.userEmail.text = it
        }
    }


    private fun getToolBar(): Toolbar {
        return IncludeToolbarBinding.bind(binding.main.root).toolbar
    }

    private fun initView() {

        val actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.root, getToolBar(), 0, 0)

        binding.root.apply {
            addDrawerListener(actionBarDrawerToggle)
            addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    super.onDrawerSlide(drawerView, slideOffset)
                    binding.main.root.translationX =
                        binding.mainNavigationView.width.toFloat() * slideOffset
                }
            })
            setDrawerShadow(null, 0)
            setScrimColor(0)
        }

        binding.main.homePager.apply {
            /*adapter = homePagerAdapter*/
            isUserInputEnabled = false

        }

        actionBarDrawerToggle.apply {
            syncState()
            drawerArrowDrawable.color = getAttributeColor(com.google.android.material.R.attr.colorOnPrimary)
        }

    }
}