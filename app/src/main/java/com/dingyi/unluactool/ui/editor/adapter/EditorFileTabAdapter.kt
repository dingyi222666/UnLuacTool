package com.dingyi.unluactool.ui.editor.adapter

import android.annotation.SuppressLint
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
import com.dingyi.unluactool.ui.editor.fileTab.OpenedFileTabData
import com.google.android.material.card.MaterialCardView

class EditorFileTabAdapter : RecyclerView.Adapter<EditorFileTabAdapter.ViewHolder>() {

    private val currentDataList = mutableListOf<OpenedFileTabData>()

    private val currentListener = OnListChangedCallback()

    private lateinit var currentSelectData: OpenedFileTabData
    private var oldSelectData: OpenedFileTabData? = null

    var clickListener: (OpenedFileTabData) -> Unit = {}
    var onContentChangeListener: (OpenedFileTabData, ItemEditorDrawerListItemBinding) -> Unit =
        { _, _ -> }


    fun addData(data: OpenedFileTabData) {
        currentDataList.add(data)
    }

    fun removeData(data: OpenedFileTabData) {
        currentDataList.remove(data)
    }

    fun observableSource(source: ObservableList<OpenedFileTabData>) {
        source.addOnListChangedCallback(currentListener)
        // add all data
        currentDataList.addAll(source)
    }

    fun observableCurrentSelectData(
        lifecycleOwner: LifecycleOwner,
        data: LiveData<OpenedFileTabData>
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

    fun removeObservable(source: ObservableList<OpenedFileTabData>) {
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

    @SuppressLint("SetTextI18n")
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

        val isNotSaveEditContent = currentData.isNotSaveEditContent
        val isNotSaveEditContentValue = isNotSaveEditContent.value ?: false

        binding.title.text = (if (isNotSaveEditContentValue) "*" else "") + currentData.functionName
        binding.path.text = currentData.fullFunctionName

        onContentChangeListener(currentData, binding)

    }


    override fun getItemViewType(position: Int): Int {
        val currentData = currentDataList[position]

        return if (currentData.fileUri.isNotEmpty()) 0 else 1
    }

    private fun checkDataIsSelected(holder: ViewHolder, position: Int, data: OpenedFileTabData) {
        val itemViewType = getItemViewType(position)

        /*  if (data.fileUri == oldSelectData?.fileUri) {

              return
          }*/

        if (data.fileUri != currentSelectData.fileUri) {
            holder.itemView.post {
                unSelectItem(itemViewType, holder)
            }
            return
        }
        holder.itemView.post {
            selectItem(itemViewType, holder)
        }

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

        val colorSurface =
            context.getAttributeColor(android.R.attr.colorBackground)

        // val colorPrimary = context.getAttributeColor(androidx.appcompat.R.attr.colorPrimary)

        if (itemType == 1) {
            val binding = ItemEditorDrawerListHomeItemBinding.bind(holder.itemView)

            binding.editorDrawerListHighlightCard.setCardBackgroundColor(
                0x00000000
            )

            binding.icon.imageTintList = ColorStateList.valueOf(colorOnSurfaceVariant)

            binding.title.setTextColor(colorOnSurfaceVariant)

            return

        }

        val binding = ItemEditorDrawerListItemBinding.bind(holder.itemView)

        binding.editorDrawerListHighlightCard.setCardBackgroundColor(
            0x00000000
        )

        binding.icon.imageTintList = ColorStateList.valueOf(colorOnSurfaceVariant)

        binding.title.setTextColor(colorOnSurfaceVariant)


    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    inner class OnListChangedCallback :
        ObservableList.OnListChangedCallback<ObservableList<OpenedFileTabData>>() {
        override fun onChanged(sender: ObservableList<OpenedFileTabData>) {
            // ?
        }

        override fun onItemRangeChanged(
            sender: ObservableList<OpenedFileTabData>,
            positionStart: Int,
            itemCount: Int
        ) {

            for (i in positionStart until positionStart + itemCount) {
                currentDataList[i] = sender.get(i)
            }

            notifyItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeInserted(
            sender: ObservableList<OpenedFileTabData>,
            positionStart: Int,
            itemCount: Int
        ) {
            for (i in positionStart until positionStart + itemCount) {
                currentDataList.add(i, sender[i])
            }

            notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeMoved(
            sender: ObservableList<OpenedFileTabData>,
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
            sender: ObservableList<OpenedFileTabData>,
            positionStart: Int,
            itemCount: Int
        ) {
            for (i in positionStart until positionStart + itemCount) {
                currentDataList.removeAt(positionStart)
            }

            notifyItemRangeRemoved(positionStart, itemCount)
        }

    }
}