package com.dingyi.unluactool.ui.editor


import android.content.Context
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dingyi.unluactool.core.progress.ProgressState
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.repository.EditorRepository
import com.dingyi.unluactool.ui.dialog.progressDialogWithState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.FileObject

class EditorViewModel : ViewModel() {

    private val _project = MutableLiveData<Project>()

    val project: LiveData<Project>
        get() = _project

    private val _currentSelectEditorFragmentData = MutableLiveData<EditorFragmentData>()

    val currentSelectEditorFragmentData: LiveData<EditorFragmentData>
        get() = _currentSelectEditorFragmentData


    val fragmentDataList = ObservableArrayList<EditorFragmentData>()

    suspend fun loadProject(uri: String): Project {
        val projectValue = EditorRepository.loadProject(uri)
        _project.value = projectValue
        return projectValue
    }

    fun initFragmentDataList() {
        addMainFragmentData()

        _currentSelectEditorFragmentData.value = fragmentDataList[0]
    }

    private fun addMainFragmentData() {
        if (!fragmentDataList.contains(EditorFragmentData.EMPTY)) {
            fragmentDataList.add(0, EditorFragmentData.EMPTY)
        }
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


    fun indexOfEditorFragmentData(data: EditorFragmentData): Int {
        if (!fragmentDataList.contains(data)) {
            putAndSetFragmentData(data)
        }
        return fragmentDataList.indexOfFirst { it.fileUri == data.fileUri }
    }


    private fun putAndSetFragmentData(fragmentData: EditorFragmentData) {
        if (!fragmentDataList.contains(fragmentData)) {
            fragmentDataList.add(fragmentData)
        }
        _currentSelectEditorFragmentData.value = fragmentData
    }

    fun setCurrentSelectEditorFragmentData(value: EditorFragmentData) {
        _currentSelectEditorFragmentData.value = value
    }

    fun requireProject(): Project {
        return checkNotNull(_project.value)
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
                map {
                    val fileObject = it as UnLuaCFileObject
                    EditorFragmentData(
                        functionName = fileObject.getFunctionName(),
                        fullFunctionName = fileObject.getFullFunctionNameWithPath(),
                        fileUri = fileObject.name.friendlyURI
                    )
                }.let {
                    fragmentDataList.addAll(it)
                }
            }
    }

    fun queryCacheOpenedFile(): List<FileObject> {
        return EditorRepository.queryCacheOpenedFile(requireProject())
    }

    suspend fun saveAllOpenedFile() {
        EditorRepository.saveAllOpenedFile(requireProject())
    }


    fun openFileObject(fileObject: UnLuaCFileObject) {

        val fileUri = fileObject.name.friendlyURI

        EditorRepository.openFileObject(
            fileObject.name.friendlyURI, project.value
                ?.projectPath?.name?.friendlyURI ?: "/???"
        )

        val currentData =
            fragmentDataList.find { it.fileUri == fileUri } ?: EditorFragmentData(
                fileUri = fileUri,
                functionName = fileObject.getFunctionName(),
                fullFunctionName = fileObject.getFullFunctionNameWithPath()
            )

        putAndSetFragmentData(currentData)
    }
}

data class EditorFragmentData(
    val fileUri: String,
    val functionName: String? = null,
    val fullFunctionName: String? = null
) {
    companion object {
        val EMPTY = EditorFragmentData("")
    }
}