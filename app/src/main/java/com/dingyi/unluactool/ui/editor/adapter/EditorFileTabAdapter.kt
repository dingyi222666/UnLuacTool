package com.dingyi.unluactool.ui.editor.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableList
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.common.ktx.setAlpha
import com.dingyi.unluactool.databinding.ItemEditorDrawerListHomeItemBinding
import com.dingyi.unluactool.ui.editor.EditorFragmentData

class EditorFileTabAdapter : RecyclerView.Adapter<EditorFileTabAdapter.ViewHolder>() {

    private val currentDataList = mutableListOf<EditorFragmentData>()

    private val currentListener = OnListChangedCallback()

    private lateinit var currentSelectData: EditorFragmentData
    private var oldSelectData: EditorFragmentData? = null

    lateinit var clickListener: (EditorFragmentData) -> Unit

    fun addData(data: EditorFragmentData) {
        currentDataList.add(data)
    }

    fun removeData(data: EditorFragmentData) {
        currentDataList.remove(data)
    }

    fun observableSource(source: ObservableList<EditorFragmentData>) {
        source.addOnListChangedCallback(currentListener)
        // add all data
        currentDataList.addAll(source)
    }

    fun observableCurrentSelectData(
        lifecycleOwner: LifecycleOwner,
        data: LiveData<EditorFragmentData>
    ) {
        data.observe(lifecycleOwner) {
            if (this::currentSelectData.isInitialized) {
                oldSelectData = currentSelectData
            }
            currentSelectData = it
        }
        currentSelectData = checkNotNull(data.value)
    }

    fun removeObservable(source: ObservableList<EditorFragmentData>) {
        source.removeOnListChangedCallback(currentListener)
    }


    override fun getItemCount(): Int {
        return currentDataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1) {
            return ViewHolder(
                ItemEditorDrawerListHomeItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ).root
            )
        }
        //TODO return file tab
        return ViewHolder(View(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentData = currentDataList[position]

        checkDataIsSelected(holder, position, currentData)

        if (currentData.fileUri.isEmpty()) {

            return
        }
    }


    override fun getItemViewType(position: Int): Int {
        val currentData = currentDataList[position]

        return if (currentData.fileUri.isNotEmpty()) 0 else 1
    }

    private fun checkDataIsSelected(holder: ViewHolder, position: Int, data: EditorFragmentData) {
        val itemViewType = getItemViewType(position)
        if (data.fileUri == oldSelectData?.fileUri) {

            if (itemViewType == 1) {
                unSelectItem(itemViewType, holder)
            }

            return
        }

        if (data.fileUri != currentSelectData.fileUri) {
            return
        }

        if (itemViewType == 1) {
            selectItem(itemViewType, holder)
        }
    }


    private fun selectItem(itemType: Int, holder: ViewHolder) {
        val context = holder.itemView.context

        /* val textColorImportant = context.getAttributeColor(
             R.attr.textColorImportant
         )*/

        val colorPrimary = context.getAttributeColor(androidx.appcompat.R.attr.colorPrimary)

        if (itemType == 1) {
            val binding = ItemEditorDrawerListHomeItemBinding.bind(holder.itemView)

            binding.icon.imageTintList = ColorStateList.valueOf(colorPrimary)

            binding.editorDrawerListHighlightCard.setCardBackgroundColor(
                Color.valueOf(colorPrimary)
                    .setAlpha(0.18f)
                    .toArgb()
            )

            binding.title.setTextColor(colorPrimary)

        }
    }

    private fun unSelectItem(itemType: Int, holder: ViewHolder) {
        val context = holder.itemView.context

        val textColorImportant = context.getAttributeColor(
            R.attr.textColorImportant
        )

        // val colorPrimary = context.getAttributeColor(androidx.appcompat.R.attr.colorPrimary)

        if (itemType == 1) {
            val binding = ItemEditorDrawerListHomeItemBinding.bind(holder.itemView)

            binding.icon.imageTintList = ColorStateList.valueOf(textColorImportant)

            binding.editorDrawerListHighlightCard.setCardBackgroundColor(
                0x0000000
            )

            binding.title.setTextColor(textColorImportant)

        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    inner class OnListChangedCallback :
        ObservableList.OnListChangedCallback<ObservableList<EditorFragmentData>>() {
        override fun onChanged(sender: ObservableList<EditorFragmentData>) {
            // ?
        }

        override fun onItemRangeChanged(
            sender: ObservableList<EditorFragmentData>,
            positionStart: Int,
            itemCount: Int
        ) {

            for (i in positionStart until positionStart + itemCount) {
                currentDataList[i] = sender.get(i)
            }

            notifyItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeInserted(
            sender: ObservableList<EditorFragmentData>,
            positionStart: Int,
            itemCount: Int
        ) {
            for (i in positionStart until positionStart + itemCount) {
                currentDataList.add(i, sender[i])
            }

            notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeMoved(
            sender: ObservableList<EditorFragmentData>,
            fromPosition: Int,
            toPosition: Int,
            itemCount: Int
        ) {

            val fromObj = currentDataList[fromPosition]
            val toObj = currentDataList[toPosition]

            currentDataList[toPosition] = fromObj
            currentDataList[fromPosition] = toObj

            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onItemRangeRemoved(
            sender: ObservableList<EditorFragmentData>,
            positionStart: Int,
            itemCount: Int
        ) {
            for (i in positionStart until positionStart + itemCount) {
                currentDataList.removeAt(i)
            }

            notifyItemRangeRemoved(positionStart, itemCount)
        }

    }
}