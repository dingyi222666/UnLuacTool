package com.dingyi.unluactool.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dingyi.unluactool.R
import com.dingyi.unluactool.base.BaseFragment
import com.dingyi.unluactool.databinding.FragmentMainBinding
import com.dingyi.unluactool.ktx.getAttributeColor
import com.dingyi.unluactool.ui.main.adapter.ProjectListAdapter
import kotlinx.coroutines.launch

class MainFragment: BaseFragment<FragmentMainBinding>() {

    private lateinit var fileSelectLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var adapter: ProjectListAdapter

    private val viewModel: MainViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMainBinding {
        return FragmentMainBinding.inflate(inflater, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fileSelectLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument(),
            FileSelectCallBack(this)
        )

        adapter = ProjectListAdapter()

        binding.apply {
            btnSelectFile.setOnClickListener {
                fileSelectLauncher.launch(arrayOf("*/*"))
            }
            projectList.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = this@MainFragment.adapter
            }

            refresh.apply {
                setOnRefreshListener {
                    refreshProject()
                }
                setColorSchemeColors(requireActivity().getAttributeColor(androidx.appcompat.R.attr.colorPrimary))
            }
            refresh.isRefreshing = true

        }

        observeLiveData()


        refreshProject()
    }

    private fun observeLiveData() {
        viewModel.projectList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun refreshProject() {

        lifecycleScope.launch {
            viewModel.refreshProjectList()
            binding.refresh.isRefreshing = false
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        fileSelectLauncher.unregister()
    }


}