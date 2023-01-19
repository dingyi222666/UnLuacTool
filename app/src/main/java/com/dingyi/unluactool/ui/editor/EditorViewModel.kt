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
import io.github.rosemoe.sora.event.ContentChangeEvent
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
        editorUIFileTabManager.projectUri = requireProject().projectPath.name.friendlyURI
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

    fun contentChangeFile(event: ContentChangeEvent, targetFileUri: String) {
        return EditorRepository.contentChangeFile(
            event,
            targetFileUri,
            requireProject().projectPath.name.friendlyURI
        )
    }

    suspend fun loadFileInCache(fileObject: FileObject): String {
        return EditorRepository.loadFileInCache(fileObject)
    }

    suspend fun saveFile(fileObject: FileObject, content: String? = null) {
        EditorRepository.saveFile(fileObject, content)
    }

    fun bindCoroutineScope(co: CoroutineScope) {
        EditorRepository.getOpenFileTabManager().bindCoroutineScope(co)
        addCloseable {
            EditorRepository.getOpenFileTabManager().close()
        }
    }

    suspend fun queryAllOpenedFileTab(): List<FileObject> {
        return EditorRepository.queryAllOpenedFileTab(requireProject())
            .apply {
                editorUIFileTabManager.openMultiFileTab(this)
            }
    }

    fun queryCacheOpenedFile(): List<FileObject> {
        return EditorRepository.queryCacheOpenedFileTab(requireProject())
    }

    suspend fun saveAllOpenedFileTab() {
        EditorRepository.saveAllOpenedFileTab(requireProject())
    }


    fun checkFileIsSave(fileObject: FileObject): Boolean {
        return EditorRepository.checkFileIsSave(fileObject)
    }

    fun openFileObject(fileObject: UnLuaCFileObject) {
        EditorRepository.openFile(
            fileObject.name.friendlyURI, project.value
                ?.projectPath?.name?.friendlyURI ?: "/???"
        )

        editorUIFileTabManager.openFileObject(fileObject)

    }
}

