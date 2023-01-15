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
import com.dingyi.unluactool.databinding.ItemEditorDrawerListItemBinding
import com.dingyi.unluactool.ui.editor.EditorFragmentData
import com.google.android.material.card.MaterialCardView

class EditorFileTabAdapter : RecyclerView.Adapter<EditorFileTabAdapter.ViewHolder>() {

    private val currentDataList = mutableListOf<EditorFragmentData>()

    private val currentListener = OnListChangedCallback()

    private lateinit var currentSelectData: EditorFragmentData
    private var oldSelectData: EditorFragmentData? = null

    var clickListener: (EditorFragmentData) -> Unit = {}


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
            if (this::currentSelectData.isInitialized && it != currentSelectData) {
                oldSelectData = currentSelectData
            }
            currentSelectData = it
            if (oldSelectData != currentSelectData) {
                notifyItemChanged(currentDataList.indexOf(oldSelectData))
            }
            notifyItemChanged(currentDataList.indexOf(currentSelectData))
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

        return ViewHolder(
            ItemEditorDrawerListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ).root
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentData = currentDataList[position]

        checkDataIsSelected(holder, position, currentData)

        val card =
            holder.itemView.findViewById<MaterialCardView>(R.id.editor_drawer_list_highlight_card)

        card.setOnClickListener {
            clickListener(currentData)
        }

        if (currentData.fileUri.isEmpty()) {
            return
        }

        val binding = ItemEditorDrawerListItemBinding.bind(holder.itemView)

        binding.title.text = currentData.functionName
        binding.path.text = currentData.fullFunctionName

    }


    override fun getItemViewType(position: Int): Int {
        val currentData = currentDataList[position]

        return if (currentData.fileUri.isNotEmpty()) 0 else 1
    }

    private fun checkDataIsSelected(holder: ViewHolder, position: Int, data: EditorFragmentData) {
        val itemViewType = getItemViewType(position)

        /*  if (data.fileUri == oldSelectData?.fileUri) {

              return
          }*/

        if (data.fileUri != currentSelectData.fileUri) {
            unSelectItem(itemViewType, holder)
            return
        }

        selectItem(itemViewType, holder)

    }


    private fun selectItem(itemType: Int, holder: ViewHolder) {
        val context = holder.itemView.context

        /* val textColorImportant = context.getAttributeColor(
             R.attr.textColorImportant
         )*/

        val colorOnSecondaryContainer =
            context.getAttributeColor(com.google.android.material.R.attr.colorOnSecondaryContainer)
        val colorOnSecondary =
            context.getAttributeColor(com.google.android.material.R.attr.colorSecondaryContainer)

        if (itemType == 1) {

            val binding = ItemEditorDrawerListHomeItemBinding.bind(holder.itemView)

            binding.editorDrawerListHighlightCard.setCardBackgroundColor(
                colorOnSecondary
            )


            binding.icon.imageTintList = ColorStateList.valueOf(colorOnSecondaryContainer)

            binding.title.setTextColor(colorOnSecondaryContainer)

            return
        }

        val binding = ItemEditorDrawerListItemBinding.bind(holder.itemView)

        binding.editorDrawerListHighlightCard.setCardBackgroundColor(
            colorOnSecondary
        )

        binding.icon.imageTintList = ColorStateList.valueOf(colorOnSecondaryContainer)

        binding.title.setTextColor(colorOnSecondaryContainer)

    }

    private fun unSelectItem(itemType: Int, holder: ViewHolder) {
        val context = holder.itemView.context


        val colorOnSurfaceVariant =
            context.getAttributeColor(com.google.android.material.R.attr.colorOnSurfaceVariant)

        // val colorPrimary = context.getAttributeColor(androidx.appcompat.R.attr.colorPrimary)

        if (itemType == 1) {
            val binding = ItemEditorDrawerListHomeItemBinding.bind(holder.itemView)

            binding.editorDrawerListHighlightCard.setCardBackgroundColor(
                0x0000000
            )

            binding.icon.imageTintList = ColorStateList.valueOf(colorOnSurfaceVariant)

            binding.title.setTextColor(colorOnSurfaceVariant)

            return

        }

        val binding = ItemEditorDrawerListItemBinding.bind(holder.itemView)

        binding.editorDrawerListHighlightCard.setCardBackgroundColor(
            0x0000000
        )

        binding.icon.imageTintList = ColorStateList.valueOf(colorOnSurfaceVariant)

        binding.title.setTextColor(colorOnSurfaceVariant)


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