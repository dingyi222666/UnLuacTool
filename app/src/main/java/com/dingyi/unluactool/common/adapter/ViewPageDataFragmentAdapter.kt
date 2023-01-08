package com.dingyi.unluactool.common.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPageDataFragmentAdapter<T : Any>(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragmentDataList = mutableListOf<T>()

    lateinit var createFragmentFunc: (T) -> Fragment

    fun addData(data: T) {
        fragmentDataList.add(data)
    }

    fun removeData(data: T) {
        fragmentDataList.remove(data)
    }

    override fun getItemCount(): Int {
        return fragmentDataList.size
    }

    override fun createFragment(position: Int): Fragment {
        return createFragmentFunc(fragmentDataList[position])
    }
}