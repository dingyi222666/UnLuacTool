package com.dingyi.unluactool.common.adapter

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPageDataFragmentAdapter<T : Any>(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {


    private val currentFragmentDataList = mutableListOf<T>()

    lateinit var createFragmentFunc: (T) -> Fragment

    private val currentListener = OnListChangedCallback()

    fun addData(data: T) {
        currentFragmentDataList.add(data)
    }

    fun removeData(data: T) {
        currentFragmentDataList.remove(data)
    }

    override fun getItemId(position: Int): Long {
        return currentFragmentDataList[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return currentFragmentDataList.find { it.hashCode().toLong() == itemId } != null
    }

    fun observableSource(source: ObservableList<T>) {
        source.addOnListChangedCallback(currentListener)
        // add all data
        currentFragmentDataList.addAll(source)
    }

    fun removeObservable(source: ObservableList<T>) {
        source.removeOnListChangedCallback(currentListener)
    }


    override fun getItemCount(): Int {
        return currentFragmentDataList.size
    }

    override fun createFragment(position: Int): Fragment {
        return createFragmentFunc(currentFragmentDataList[position])
    }


    inner class OnListChangedCallback : ObservableList.OnListChangedCallback<ObservableList<T>>() {
        override fun onChanged(sender: ObservableList<T>) {
            // ?
        }

        override fun onItemRangeChanged(
            sender: ObservableList<T>,
            positionStart: Int,
            itemCount: Int
        ) {

            for (i in positionStart until positionStart + itemCount) {
                currentFragmentDataList[i] = sender[i]
            }

            notifyItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeInserted(
            sender: ObservableList<T>,
            positionStart: Int,
            itemCount: Int
        ) {
            for (i in positionStart until positionStart + itemCount) {
                currentFragmentDataList.add(i, sender[i])
            }

            notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeMoved(
            sender: ObservableList<T>,
            fromPosition: Int,
            toPosition: Int,
            itemCount: Int
        ) {

            val fromObj = currentFragmentDataList[fromPosition]
            val toObj = currentFragmentDataList[toPosition]

            currentFragmentDataList[toPosition] = fromObj
            currentFragmentDataList[fromPosition] = toObj

            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onItemRangeRemoved(
            sender: ObservableList<T>,
            positionStart: Int,
            itemCount: Int
        ) {
            for (i in positionStart until positionStart + itemCount) {
                currentFragmentDataList.removeAt(positionStart)
            }

            notifyItemRangeRemoved(positionStart, itemCount)
        }

    }

}