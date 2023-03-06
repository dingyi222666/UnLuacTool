package com.dingyi.unluactool.ui.dialog.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dingyi.unluactool.databinding.ItemEditorNavigationListBinding
import com.dingyi.unluactool.engine.suggest.CodeNavigation

class NavigationAdapter:ListAdapter<CodeNavigation,NavigationAdapter.ViewHolder>(DiffItemCallback) {

    private lateinit var listener: OnItemClickListener

    companion object DiffItemCallback : DiffUtil.ItemCallback<CodeNavigation>() {
        override fun areItemsTheSame(
            oldItem: CodeNavigation,
            newItem: CodeNavigation
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: CodeNavigation,
            newItem: CodeNavigation
        ): Boolean {
            return oldItem == newItem

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemEditorNavigationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            val item = getItem(position)
            title.text = item.name
            root.setOnClickListener {
                listener.onItemClick(item)
            }
        }
    }

    fun onItemClick(listener: OnItemClickListener) {
        this.listener = listener
    }

    data class ViewHolder(
        val binding: ItemEditorNavigationListBinding
    ) : RecyclerView.ViewHolder(binding.root)

    fun interface OnItemClickListener {
        fun onItemClick(item: CodeNavigation)
    }
}