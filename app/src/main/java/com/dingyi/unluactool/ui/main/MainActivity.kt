package com.dingyi.unluactool.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.ActivityChooserView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.R
import com.dingyi.unluactool.databinding.IncludeToolbarBinding
import com.dingyi.unluactool.databinding.MainBinding
import com.dingyi.unluactool.databinding.MainNavigationHeadBinding
import com.dingyi.unluactool.ktx.getAttributeColor
import com.dingyi.unluactool.ktx.getJavaClass
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
         /*   addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    super.onDrawerSlide(drawerView, slideOffset)
                    binding.main.root.translationX =
                        binding.mainNavigationView.width.toFloat() * slideOffset
                }
            })
            setDrawerShadow(null, 0)
            setScrimColor(0)*/
        }


        val homePagerAdapter = MainViewPagerAdapter(this)
            .apply {
                addFragments(getPagerFragments())
            }

        binding.main.homePager.apply {
            adapter = homePagerAdapter
            isUserInputEnabled = false
        }


        binding.mainNavigationView.setNavigationItemSelectedListener { it ->
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
            binding.root.closeDrawers()
            return@setNavigationItemSelectedListener true
        }




        actionBarDrawerToggle.apply {
            syncState()
            drawerArrowDrawable.color =
                getAttributeColor(com.google.android.material.R.attr.colorOnPrimary)
        }



    }

    private fun getPagerFragments(): List<Class<*>> {
        return listOf(getJavaClass<MainFragment>())
    }
}