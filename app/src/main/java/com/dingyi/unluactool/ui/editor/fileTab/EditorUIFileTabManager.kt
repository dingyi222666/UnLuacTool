package com.dingyi.unluactool.ui.editor.fileTab

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.repository.EditorRepository
import kotlinx.coroutines.launch
import org.apache.commons.vfs2.FileObject

class EditorUIFileTabManager {

    private val _currentSelectOpenedFileTabData = MutableLiveData<OpenedFileTabData>()

    val currentSelectOpenedFileTabData: LiveData<OpenedFileTabData>
        get() = _currentSelectOpenedFileTabData


    val openedFileList = ObservableArrayList<OpenedFileTabData>()

    private var wantRemoveFileExtra: OpenedFileTabData? = null

    lateinit var projectUri: String

    fun initList() {

        openedFileList.clear()

        if (!openedFileList.contains(OpenedFileTabData.EMPTY)) {
            openedFileList.add(0, OpenedFileTabData.EMPTY)
        }

        _currentSelectOpenedFileTabData.value = openedFileList[0]

    }

    private fun putAndSetData(data: OpenedFileTabData) {
        if (data == wantRemoveFileExtra) {
            wantRemoveFileExtra = null
            return
        }
        if (!openedFileList.contains(data)) {
            openedFileList.add(data)
        }
        _currentSelectOpenedFileTabData.value = data
    }

    fun indexOfDataIndex(data: OpenedFileTabData): Int {
        if (data == wantRemoveFileExtra) {
            wantRemoveFileExtra = null
            return 0
        }
        if (!openedFileList.contains(data)) {
            putAndSetData(data)
        }
        return openedFileList.indexOfFirst { it.fileUri == data.fileUri }
    }

    fun setCurrentSelectFileTabData(value: OpenedFileTabData) {
        _currentSelectOpenedFileTabData.value = value
    }

    fun createOpenFileTabData(
        fileObject: UnLuaCFileObject? = null,
        fileUri: String = fileObject?.name?.friendlyURI ?: "",
        functionName: String? = fileObject?.getFunctionName(),
        fullFunctionName: String? = fileObject?.getFullFunctionNameWithPath()
    ): OpenedFileTabData {
        return OpenedFileTabData(
            fileUri = fileUri, functionName = functionName, fullFunctionName = fullFunctionName
        )
    }

    fun openFileObject(fileObject: UnLuaCFileObject) {
        val fileUri = fileObject.name.friendlyURI
        val currentData =
            openedFileList.find { it.fileUri == fileUri } ?: createOpenFileTabData(fileObject)

        putAndSetData(currentData)
    }


    fun openMultiFileTab(fileObjects: List<FileObject>) {
        fileObjects.map {
            val fileObject = it as UnLuaCFileObject
            OpenedFileTabData(
                functionName = fileObject.getFunctionName(),
                fullFunctionName = fileObject.getFullFunctionNameWithPath(),
                fileUri = fileObject.name.friendlyURI,
                projectUri = projectUri
            )
        }.let {
            openedFileList.addAll(it)
        }
    }

    fun removeData(currentFragmentData: OpenedFileTabData) {
        if (currentFragmentData == OpenedFileTabData.EMPTY) {
            return
        }

        val removeIndex = openedFileList.indexOf(currentFragmentData)

        var targetIndex = removeIndex - 1

        if (targetIndex == 0 && openedFileList.size > 2) {
            targetIndex = 2
        }

        wantRemoveFileExtra = currentFragmentData

        val targetData = openedFileList[targetIndex]

        openedFileList.removeAt(removeIndex)

        EditorRepository.closeFile(currentFragmentData.fileUri, currentFragmentData.projectUri)

        setCurrentSelectFileTabData(targetData)

    }

    fun contentChange(targetFileUri: String, checkFileIsSave: Boolean) {
        val target = openedFileList.find { it.fileUri == targetFileUri } ?: return
        target.isNotSaveEditContent.value = !checkFileIsSave
    }

    fun queryOpenedFileTab(fileObject: FileObject): OpenedFileTabData {
        return checkNotNull(openedFileList.find {
            it.fileUri == fileObject.name.friendlyURI
        })

    }
}

data class OpenedFileTabData(
    val fileUri: String,
    val projectUri: String = "",
    val functionName: String? = null,
    val fullFunctionName: String? = null,
    var isNotSaveEditContent: MutableLiveData<Boolean> = MutableLiveData(false)
) {
    companion object {
        val EMPTY = OpenedFileTabData("")
    }
}
