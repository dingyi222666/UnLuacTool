package com.dingyi.unluactool.ui.editor.fileTab

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import org.apache.commons.vfs2.FileObject

class EditorUIFileTabManager {

    private val _currentSelectOpenedFileTabData = MutableLiveData<OpenedFileTabData>()

    val currentSelectOpenedFileTabData: LiveData<OpenedFileTabData>
        get() = _currentSelectOpenedFileTabData


    val openedFileList = ObservableArrayList<OpenedFileTabData>()

    private var wantRemoveFileExtra: OpenedFileTabData? = null


    fun initList() {

        openedFileList.clear()

        if (!openedFileList.contains(OpenedFileTabData.EMPTY)) {
            openedFileList.add(0, OpenedFileTabData.EMPTY)
        }

        _currentSelectOpenedFileTabData.value = openedFileList[0]

    }

    private fun putAndSetData(data: OpenedFileTabData) {
        if (data == wantRemoveFileExtra) {
            return
        }
        if (!openedFileList.contains(data)) {
            openedFileList.add(data)
        }
        _currentSelectOpenedFileTabData.value = data
    }

    fun indexOfDataIndex(data: OpenedFileTabData): Int {
        if (data == wantRemoveFileExtra) {
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
        fileUri: String = fileObject?.publicURIString ?: "",
        functionName: String? = fileObject?.getFunctionName(),
        fullFunctionName: String? = fileObject?.getFullFunctionNameWithPath()
    ): OpenedFileTabData {
        return OpenedFileTabData(
            fileUri = fileUri, functionName = functionName, fullFunctionName = fullFunctionName
        )
    }

    fun openFileObject(fileObject: UnLuaCFileObject) {
        val fileUri = fileObject.publicURIString
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
                fileUri = fileObject.name.friendlyURI
            )
        }.let {
            openedFileList.addAll(it)
        }
    }


}

data class OpenedFileTabData(
    val fileUri: String, val functionName: String? = null, val fullFunctionName: String? = null
) {
    companion object {
        val EMPTY = OpenedFileTabData("")
    }
}
