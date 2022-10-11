package com.dingyi.unluactool.ui.main

import android.app.ProgressDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.common.ktx.getJavaClass
import com.dingyi.unluactool.common.ktx.getStatusBarHeight
import com.dingyi.unluactool.common.util.ScreenAdapter
import com.dingyi.unluactool.databinding.IncludeToolbarBinding
import com.dingyi.unluactool.databinding.MainBinding
import com.dingyi.unluactool.databinding.MainNavigationHeadBinding
import dev.chrisbanes.insetter.applyInsetter
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
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
                drawerArrowDrawable.color =
                    getAttributeColor(com.google.android.material.R.attr.colorOnPrimary)
            }

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





            if (rootView is DrawerLayout) {
                rootView.closeDrawers()
            }
            return@setNavigationItemSelectedListener true
        }


    }

    private fun getPagerFragments(): List<Class<*>> {
        return listOf(getJavaClass<MainFragment>())
    }
}