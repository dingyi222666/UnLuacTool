package com.dingyi.unluactool.core.file

import com.dingyi.unluactool.common.ktx.decodeToBean
import com.dingyi.unluactool.common.ktx.encodeToJson
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.internal.LuaProject
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.VFS


class OpenedFileManager internal constructor() : FileEventListener {

    private val cacheOpenedFile = mutableMapOf<String, MutableList<FileObject>>()

    private val vfsManager = VFS.getManager()
    suspend fun queryAllOpenedFile(project: Project): List<FileObject> =
        withContext(Dispatchers.IO) {
            val publicUri = project.projectPath.publicURIString
            val cacheJsonFile =
                project.getProjectPath(LuaProject.CACHE_DIR_NAME).resolveFile("opened_file.json")

            if (!cacheJsonFile.isFile) {
                return@withContext cacheOpenedFile.getOrPut(publicUri) { mutableListOf() }
            }


            val openedFileObject = cacheJsonFile.content.inputStream.readBytes().decodeToString()
                .decodeToBean<OpenedFileObject>()

            cacheOpenedFile[publicUri] = openedFileObject.openedFiles.map {
                vfsManager.resolveFile(it.uri)
            }.toMutableList()

            cacheOpenedFile.getValue(publicUri)
        }

    fun queryCacheOpenedFile(project: Project): List<FileObject> {
        val publicUri = project.projectPath.publicURIString
        return cacheOpenedFile.getOrPut(publicUri) { mutableListOf() }
    }

    override fun onEvent(event: FileEvent) {
        val projectOpenedFileList = cacheOpenedFile.getValue(event.projectUri)
        val targetUri = event.targetFileUri
        when (event) {
            is OpenFileEvent -> {

                val isOpened = projectOpenedFileList
                    .find { it.publicURIString == targetUri } == null

                if (isOpened) {
                    return
                }

                projectOpenedFileList.add(vfsManager.resolveFile(targetUri))
            }

            is CloseFileEvent -> {
                projectOpenedFileList.removeIf {
                    it.publicURIString == targetUri
                }
            }

            is ChangeFileOrderEvent -> {
                val oldFile = projectOpenedFileList[event.newOrder]
                val newFile = projectOpenedFileList[event.oldOrder]
                projectOpenedFileList[event.newOrder] = newFile
                projectOpenedFileList[event.oldOrder] = oldFile
            }
        }
    }

}


private val gson = Gson()

internal data class OpenedFileObject(
    @SerializedName("opened_files")
    val openedFiles: MutableList<OpenedFile>
) {
    internal data class OpenedFile(
        val uri: String
    )
}