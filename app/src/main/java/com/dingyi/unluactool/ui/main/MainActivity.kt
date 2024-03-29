package com.dingyi.unluactool.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.adapter.ViewPageFragmentAdapter
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.common.ktx.getJavaClass
import com.dingyi.unluactool.databinding.IncludeToolbarBinding
import com.dingyi.unluactool.databinding.MainBinding
import com.dingyi.unluactool.databinding.MainNavigationHeadBinding
import com.google.android.material.elevation.SurfaceColors
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        MainBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<MainViewModel>()



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_UnLuacTool)

        setContentView(binding.root)

        setSupportActionBar(getToolBar())

        //toolbar set
        supportActionBar?.apply {
            title = getString(R.string.app_name)
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

        val rootView = binding.root

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        rootView.apply {

            val actionBarDrawerToggle =
                ActionBarDrawerToggle(this@MainActivity, this, getToolBar(), 0, 0)

            addDrawerListener(actionBarDrawerToggle)
            /*   addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                binding.main.root.translationX =
                    binding.mainNavigationView.width.toFloat() * slideOffset
            }
        })
        setDrawerShadow(null, 0)
        setScrimColor(0)*/

            actionBarDrawerToggle.apply {
                syncState()
            }
        }


        val homePagerAdapter = ViewPageFragmentAdapter(this)
            .apply {
                addFragments(getPagerFragments())
            }

        binding.main.homePager.apply {
            adapter = homePagerAdapter
            isUserInputEnabled = false
        }


        binding.mainNavigationView.setNavigationItemSelectedListener {
            if (it.groupId == R.id.navigation_default) {
                it.isChecked = true
                it.isCheckable = true

                val index = when (it.itemId) {
                    R.id.main -> 0
                    /*   R.id.settings -> 1
                       R.id.about -> 2*/
                    else -> 0
                }
                binding.main.homePager.setCurrentItem(index, true)
            }

            rootView.closeDrawers()
            return@setNavigationItemSelectedListener true
        }

    }

    private fun getPagerFragments(): List<Class<*>> {
        return listOf(getJavaClass<MainFragment>())
    }
}