package com.dingyi.unluactool.ui.editor


import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dingyi.unluactool.core.progress.ProgressState
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.repository.EditorRepository
import com.dingyi.unluactool.ui.dialog.progressDialogWithState
import com.dingyi.unluactool.ui.editor.fileTab.EditorUIFileTabManager
import com.dingyi.unluactool.ui.editor.fileTab.OpenedFileTabData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.VFS

class EditorViewModel : ViewModel() {

    private val _project = MutableLiveData<Project>()

    val project: LiveData<Project>
        get() = _project

    internal val editorUIFileTabManager = EditorUIFileTabManager()

    val vfsManager: FileSystemManager by lazy(LazyThreadSafetyMode.NONE) {
        VFS.getManager()
    }

    val eventManager by lazy(LazyThreadSafetyMode.NONE) {
        EditorRepository.getEventManager()
    }


    suspend fun loadProject(uri: String): Project {
        val projectValue = EditorRepository.loadProject(uri)
        _project.value = projectValue
        return projectValue
    }

    fun initFileTabDataList() {
        editorUIFileTabManager.initList()
    }

    suspend fun openProject(context: Context, lifecycleOwner: LifecycleOwner) =
        withContext(Dispatchers.Main) {
            val (dialog, state) = progressDialogWithState(
                context = context,
                lifecycleOwner = lifecycleOwner
            )
            //launch in new CoroutineScope
            dialog to suspend {
                withContext(Dispatchers.IO) {
                    runCatching {
                        openProject(state)
                    }.onSuccess {
                        withContext(Dispatchers.Main) {
                            dialog.dismiss()
                        }
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }
        }

    private suspend fun openProject(progressState: ProgressState) {
        project.value?.open(progressState)
    }


    fun setCurrentSelectFileTabData(value: OpenedFileTabData) {
        editorUIFileTabManager.setCurrentSelectFileTabData(value)
    }

    fun requireProject(): Project {
        return checkNotNull(_project.value)
    }

    suspend fun openFile(fileObject: FileObject): String {
        return EditorRepository.openFile(fileObject)
    }

    suspend fun loadFileInCache(fileObject: FileObject): String {
        return EditorRepository.loadFileInCache(fileObject)
    }

    suspend fun saveFile(fileObject: FileObject, content: String? = null) {
        EditorRepository.saveFile(fileObject, content)
    }

    fun bindCoroutineScope(co: CoroutineScope) {
        EditorRepository.getOpenFileManager().bindCoroutineScope(co)
        addCloseable {
            EditorRepository.getOpenFileManager().close()
        }
    }

    suspend fun queryAllOpenedFile(): List<FileObject> {
        return EditorRepository.queryAllOpenedFile(requireProject())
            .apply {
               editorUIFileTabManager.openMultiFileTab(this)
            }
    }

    fun queryCacheOpenedFile(): List<FileObject> {
        return EditorRepository.queryCacheOpenedFile(requireProject())
    }

    suspend fun saveAllOpenedFile() {
        EditorRepository.saveAllOpenedFile(requireProject())
    }


    fun openFileObject(fileObject: UnLuaCFileObject) {
        EditorRepository.openFileObject(
            fileObject.name.friendlyURI, project.value
                ?.projectPath?.name?.friendlyURI ?: "/???"
        )

        editorUIFileTabManager.openFileObject(fileObject)

    }
}

