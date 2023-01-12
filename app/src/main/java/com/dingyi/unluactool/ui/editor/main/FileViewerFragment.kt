package com.dingyi.unluactool.ui.editor.main

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.R
import com.dingyi.unluactool.base.BaseFragment
import com.dingyi.unluactool.common.ktx.dp
import com.dingyi.unluactool.databinding.FragmentEditorFileViewerBinding
import com.dingyi.unluactool.databinding.ItemEditorFileViewerListBinding
import com.dingyi.unluactool.engine.filesystem.FileObjectType
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.ui.editor.EditorViewModel
import io.github.dingyi222666.view.treeview.Tree
import io.github.dingyi222666.view.treeview.TreeNode
import io.github.dingyi222666.view.treeview.TreeNodeGenerator
import io.github.dingyi222666.view.treeview.TreeNodeListener
import io.github.dingyi222666.view.treeview.TreeView
import io.github.dingyi222666.view.treeview.TreeViewBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.VFS

class FileViewerFragment : BaseFragment<FragmentEditorFileViewerBinding>() {

    private val viewModel by activityViewModels<EditorViewModel>()

    private val treeViewData = Tree.createTree<UnLuaCFileObject>()

    private val fsManager by lazy(LazyThreadSafetyMode.NONE) {
        VFS.getManager()
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditorFileViewerBinding {
        return FragmentEditorFileViewerBinding.inflate(inflater, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        treeViewData.apply {
            generator = FileDataGenerator()
            initTree()
        }

        binding.treeView.apply {
            binder = FileNodeBinder() as TreeViewBinder<Any>
            tree = treeViewData as Tree<Any>
        }

        lifecycleScope.launch {
            binding.treeView.refresh()
        }

    }

    inner class FileNodeBinder : TreeViewBinder<UnLuaCFileObject>(),
        TreeNodeListener<UnLuaCFileObject> {
        override fun areContentsTheSame(
            oldItem: TreeNode<UnLuaCFileObject>,
            newItem: TreeNode<UnLuaCFileObject>
        ): Boolean {
            return oldItem.extra?.publicURIString == newItem.extra?.publicURIString
        }

        override fun areItemsTheSame(
            oldItem: TreeNode<UnLuaCFileObject>,
            newItem: TreeNode<UnLuaCFileObject>
        ): Boolean {
            return oldItem.extra?.publicURIString == newItem.extra?.publicURIString
        }

        override fun bindView(
            holder: TreeView.ViewHolder,
            node: TreeNode<UnLuaCFileObject>,
            listener: TreeNodeListener<UnLuaCFileObject>
        ) {
            val itemView = holder.itemView
            val extra = checkNotNull(node.extra)
            val binding = ItemEditorFileViewerListBinding.bind(holder.itemView)

            itemView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = node.level * 7.dp
            }

            binding.arrow.isInvisible = extra.isFile

            val fileType = extra.getFileType()

            if (fileType == FileObjectType.FUNCTION) {
                binding.title.text = node.name
            } else {
                applyDir(binding, extra, node)
            }

            when (fileType) {
                FileObjectType.DIR -> {
                    binding.image.setImageResource(R.drawable.ic_baseline_folder_open_24)
                }

                FileObjectType.FILE -> {
                    binding.image.setImageDrawable(
                        CircleDrawable(
                            "L", resources.getColor(R.color.blue_small_icon, null)
                        )
                    )
                }

                FileObjectType.FUNCTION, FileObjectType.FUNCTION_WITH_CHILD -> {
                    binding.image.setImageDrawable(
                        CircleDrawable(
                            "F", resources.getColor(R.color.blue_small_icon, null)
                        )
                    )
                }
            }

        }


        private fun applyDir(
            binding: ItemEditorFileViewerListBinding,
            extra: UnLuaCFileObject,
            node: TreeNode<UnLuaCFileObject>
        ) {
            binding.title.text = node.name

            binding
                .arrow
                .animate()
                .rotation(if (node.expand) 90f else 0f)
                .setDuration(0)
                .start()
        }


        override fun onClick(node: TreeNode<UnLuaCFileObject>, holder: TreeView.ViewHolder) {
            val extra = checkNotNull(node.extra)
            val binding = ItemEditorFileViewerListBinding.bind(holder.itemView)

            if (extra.isFolder && !node.hasChild) {
                applyDir(binding, extra, node)
            }
        }

        override fun onToggle(
            node: TreeNode<UnLuaCFileObject>,
            isExpand: Boolean,
            holder: TreeView.ViewHolder
        ) {
            val extra = checkNotNull(node.extra)
            val binding = ItemEditorFileViewerListBinding.bind(holder.itemView)

            if (extra.isFolder) {
                applyDir(binding, extra, node)
            }
        }

        override fun createView(parent: ViewGroup, viewType: Int): View {
            return ItemEditorFileViewerListBinding.inflate(
                layoutInflater, parent, false
            ).root
        }

        override fun getItemViewType(node: TreeNode<UnLuaCFileObject>): Int {
            return 0
        }

    }

    inner class FileDataGenerator : TreeNodeGenerator<UnLuaCFileObject> {
        override suspend fun refreshNode(
            targetNode: TreeNode<UnLuaCFileObject>,
            oldNodeSet: Set<Int>,
            withChild: Boolean,
            tree: Tree<UnLuaCFileObject>
        ): List<TreeNode<UnLuaCFileObject>> {

            val targetNodeExtra = checkNotNull(targetNode.extra)

            if (targetNodeExtra.isFile) {
                targetNode.hasChild = false
                return listOf()
            }

            val oldNodes = tree.getNodes(oldNodeSet)

            val child =
                withContext(Dispatchers.IO) { checkNotNull(targetNodeExtra.children).toMutableList() }

            val result = mutableListOf<TreeNode<UnLuaCFileObject>>()

            oldNodes.forEach { node ->
                val virtualFile = child.find { it.name == node.extra?.name }
                if (virtualFile != null) {
                    result.add(node)
                }
                child.remove(virtualFile)
            }

            if (child.isEmpty()) {
                return result
            }

            child.forEach {
                val unLuaCFileObject = it as UnLuaCFileObject
                val hasChild = if (unLuaCFileObject.isFile) false else {
                    withContext(Dispatchers.IO) { unLuaCFileObject.children }.isEmpty()
                }
                result.add(
                    TreeNode(
                        unLuaCFileObject,
                        targetNode.level + 1,
                        unLuaCFileObject.name.baseName,
                        tree.generateId(),
                        unLuaCFileObject.isFolder && hasChild,
                        false
                    )
                )
            }

            return result
        }

        override fun createRootNode(): TreeNode<UnLuaCFileObject> {
            val project = checkNotNull(viewModel.project.value)
            val rootFileObject = fsManager.resolveFile("unluac://${project.name}")
            return TreeNode(
                extra = rootFileObject as UnLuaCFileObject,
                level = 0,
                name = project.name,
                id = 0,
                hasChild = true,
                expand = true
            )
        }

    }

    internal class CircleDrawable(
        private val mDrawText: String,
        private val mDrawColor: Int
    ) :
        Drawable() {
        private val mPaint = Paint().apply {
            isAntiAlias = true
            color = mDrawColor
            strokeWidth = Resources.getSystem()
                .displayMetrics.density * 2
            strokeJoin = Paint.Join.ROUND
            style = Paint.Style.STROKE
        }
        private val mTextPaint = Paint().apply {
            color = -0x1
            isAntiAlias = true
            textSize = Resources.getSystem()
                .displayMetrics.density * 14
            textAlign = Paint.Align.CENTER
        }


        override fun draw(canvas: Canvas) {
            val width = bounds.right.toFloat()
            val height = bounds.bottom.toFloat()

            // canvas.drawCircle(width / 2, height / 2, width / 2, mPaint)

            //  canvas.drawRect(0f, 0f, width, height, mPaint)
            canvas.drawArc(0f, 0f, width, height, 0f, 360f, false, mPaint)
            canvas.save()
            canvas.translate(width / 2f, height / 2f)
            val textCenter = -(mTextPaint.descent() + mTextPaint.ascent()) / 2f
            canvas.drawText(mDrawText, 0f, textCenter, mTextPaint)
            canvas.restore()
        }

        override fun setAlpha(p1: Int) {
            mPaint.alpha = p1
            mTextPaint.alpha = p1
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            mTextPaint.colorFilter = colorFilter
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int {
            return PixelFormat.OPAQUE
        }


    }


}
