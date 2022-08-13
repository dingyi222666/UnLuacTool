package com.dingyi.unluactool.ui.main.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dingyi.unluactool.R
import com.dingyi.unluactool.core.project.Project

import com.dingyi.unluactool.databinding.ItemMainFragmentListBinding
import com.dingyi.unluactool.common.ktx.getString
import com.dingyi.unluactool.common.ktx.toFile

class ProjectListAdapter :
    ListAdapter<Project, ProjectListAdapter.ViewHolder>(DiffItemCallback) {


    var listClickEvent = { _: Project -> }

    data class ViewHolder(
        val binding: ItemMainFragmentListBinding
    ) : RecyclerView.ViewHolder(binding.root)


    companion object DiffItemCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(
            oldItem: Project,
            newItem: Project
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Project,
            newItem: Project
        ): Boolean {
            return oldItem.projectPath.publicURIString == newItem.projectPath.publicURIString

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
                item.fileCount
            )
            item.projectIconPath?.let {
                image.setImageURI(Uri.fromFile(it.toFile()))
            }

            root.setOnClickListener {
                listClickEvent(item)
            }

        }
    }
}