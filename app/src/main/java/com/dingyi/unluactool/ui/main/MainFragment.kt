package com.dingyi.unluactool.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.dingyi.unluactool.base.BaseFragment
import com.dingyi.unluactool.databinding.FragmentMainBinding

class MainFragment: BaseFragment<FragmentMainBinding>() {

    private lateinit var fileSelectLauncher: ActivityResultLauncher<Array<String>>


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

        binding.btnSelectFile.setOnClickListener {
            fileSelectLauncher.launch(arrayOf("*/*"))
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        fileSelectLauncher.unregister()
    }


}