package com.dingyi.unluactool.ui.main.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dingyi.unluactool.R
import com.dingyi.unluactool.beans.ProjectInfoBean

import com.dingyi.unluactool.databinding.ItemMainFragmentListBinding
import com.dingyi.unluactool.ktx.getString
import com.dingyi.unluactool.ktx.toFile

class ProjectListAdapter :
    ListAdapter<ProjectInfoBean, ProjectListAdapter.ViewHolder>(DiffItemCallback) {


    data class ViewHolder(
        val binding: ItemMainFragmentListBinding
    ) : RecyclerView.ViewHolder(binding.root)


    companion object DiffItemCallback : DiffUtil.ItemCallback<ProjectInfoBean>() {
        override fun areItemsTheSame(
            oldItem: ProjectInfoBean,
            newItem: ProjectInfoBean
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ProjectInfoBean,
            newItem: ProjectInfoBean
        ): Boolean {
            return oldItem.path == newItem.path
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMainFragmentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            val item = getItem(position)
            title.text = item.name
            size.text = getString(
                R.string.main_project_lua_file_count,
                item.fileCountOfLuaFile
            )
            item.icon?.let {
                image.setImageURI(Uri.fromFile(it.toFile()))
            }
        }
    }
}