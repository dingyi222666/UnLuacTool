package com.dingyi.unluactool.common.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPageFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragmentClasses = mutableListOf<Class<*>>()

    fun addFragments(fragmentList:List<Class<*>>) {
        fragmentClasses.addAll(fragmentList)
    }

    override fun getItemCount(): Int {
        return fragmentClasses.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentClasses[position].newInstance() as Fragment
    }
}