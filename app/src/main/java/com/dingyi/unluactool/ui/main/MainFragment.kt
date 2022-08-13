package com.dingyi.unluactool.ui.main

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.R
import com.dingyi.unluactool.base.BaseFragment
import com.dingyi.unluactool.core.project.ProjectCreator
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.databinding.FragmentMainBinding
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.common.ktx.showSnackBar
import com.dingyi.unluactool.common.ktx.startActivity
import com.dingyi.unluactool.ui.editor.EditorActivity
import com.dingyi.unluactool.ui.main.adapter.ProjectListAdapter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainFragment : BaseFragment<FragmentMainBinding>() {

    private lateinit var fileSelectLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var adapter: ProjectListAdapter

    private val viewModel by activityViewModels<MainViewModel>()

    private val coroutineHandler = CoroutineExceptionHandler { _, exception ->
        exception.message?.showSnackBar(binding.root)
        Log.e(javaClass.simpleName, "error for create project", exception)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMainBinding {
        return FragmentMainBinding.inflate(inflater, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fileSelectLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) {
            it?.let { uri ->
                doRefresh(Dispatchers.Main + coroutineHandler) {

                    MainApplication
                        .instance
                        .globalServiceRegistry
                        .get<ProjectCreator>()
                        .createProject(requireContext().contentResolver, uri)

                    refreshProject()
                }
            }
        }

        adapter = ProjectListAdapter()
            .apply {
                listClickEvent = {
                    startActivity<EditorActivity> {
                        putExtra("path",it.projectPath.name.uri)
                    }
                }
            }

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

        }

        observeLiveData()

        refreshProject()
    }

    private fun observeLiveData() {
        viewModel.projectList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun doRefresh(context: CoroutineContext = Dispatchers.Main, block: suspend () -> Unit) {
        lifecycleScope.launch(context) {
            binding.refresh.isRefreshing = true
            block.invoke()
            binding.refresh.isRefreshing = false
        }
    }

    private fun refreshProject() {

        doRefresh(block = viewModel::refreshProjectList)

    }


    override fun onDestroy() {
        super.onDestroy()
        fileSelectLauncher.unregister()
    }


}