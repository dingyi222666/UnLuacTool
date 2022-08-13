package com.dingyi.unluactool.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T:ViewBinding>:Fragment() {

    protected lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (!this::binding.isInitialized) {
            binding = getViewBinding(inflater, container)
        }

        return binding.root
    }


    /**
     * @return T 返回ViewBinding
     *
     */
    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

}