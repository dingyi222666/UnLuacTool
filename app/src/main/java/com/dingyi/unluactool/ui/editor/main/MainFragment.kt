package com.dingyi.unluactool.ui.editor.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dingyi.unluactool.common.base.BaseFragment
import com.dingyi.unluactool.common.adapter.ViewPageFragmentAdapter
import com.dingyi.unluactool.common.ktx.getJavaClass
import com.dingyi.unluactool.databinding.FragmentEditorMainViewerBinding

class MainFragment : BaseFragment<FragmentEditorMainViewerBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditorMainViewerBinding {
        return FragmentEditorMainViewerBinding.inflate(inflater, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homePagerAdapter = ViewPageFragmentAdapter(requireActivity())
            .apply {
                addFragments(getPagerFragments())
            }

        binding.editorMainViewerFragmentPager.apply {
            adapter = homePagerAdapter
        }

    }

    private fun getPagerFragments(): List <Class<*>> {
        return arrayListOf(
            getJavaClass<FileViewerFragment>()
        )
    }


}