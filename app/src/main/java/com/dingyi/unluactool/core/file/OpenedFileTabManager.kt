package com.dingyi.unluactool.core.file

import com.dingyi.unluactool.common.ktx.decodeToBean
import com.dingyi.unluactool.common.ktx.encodeToJson
import com.dingyi.unluactool.common.ktx.inputStream
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.internal.LuaProject
import com.dingyi.unluactool.ui.editor.fileTab.OpenedFileTabData
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.provider.local.LocalFile
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import kotlin.io.path.outputStream


class OpenedFileTabManager internal constructor() : FileEventListener {

    private val cacheOpenedFile = mutableMapOf<String, MutableList<FileObject>>()

    private val vfsManager = VFS.getManager()
    suspend fun queryAllOpenedFileTab(project: Project): List<FileObject> =
        withContext(Dispatchers.IO) {
            val publicUri = project.projectPath.name.friendlyURI
            val cacheJsonFile =
                project.getProjectPath(LuaProject.CACHE_DIR_NAME).resolveFile("opened_file.json")

            if (!cacheJsonFile.isFile) {
                return@withContext cacheOpenedFile.getOrPut(publicUri) { mutableListOf() }
            }


            val openedFileObject = cacheJsonFile.inputStream.readBytes().decodeToString()
                .decodeToBean<OpenedFileObject>()

            cacheOpenedFile[publicUri] = openedFileObject.openedFiles.map {
                vfsManager.resolveFile(it.uri)
            }.toMutableList()

            cacheOpenedFile.getValue(publicUri)
        }

    suspend fun saveAllOpenedFileTab(project: Project) = withContext(Dispatchers.IO) {
        val publicUri = project.projectPath.name.friendlyURI
        val cacheJsonFile =
            project.getProjectPath(LuaProject.CACHE_DIR_NAME)
                .apply {
                    createFolder()
                }
                .resolveFile("opened_file.json")

        if (!cacheJsonFile.isFile) {
            cacheJsonFile.createFile()
        }

        val openedFileList = cacheOpenedFile.getOrPut(publicUri) { mutableListOf() }
            .map {
                OpenedFileObject.OpenedFile(it.name.friendlyURI)
            }.toMutableList()

        val openedFileObject = OpenedFileObject(openedFileList)

        // 在这里用Path是为了覆盖掉FileContent的默认行为
        cacheJsonFile.path
            .outputStream()
            .bufferedWriter()
            .use {
                it.write(openedFileObject.encodeToJson())
                it.flush()
            }

    }

    fun queryCacheOpenedFileTab(project: Project): List<FileObject> {
        val publicUri = project.projectPath.name.friendlyURI
        return cacheOpenedFile.getOrPut(publicUri) { mutableListOf() }
    }

    override fun onEvent(event: FileEvent) {
        val projectOpenedFileList = cacheOpenedFile.getValue(event.projectUri)
        val targetUri = event.targetFileUri
        when (event) {
            is FileOpenEvent -> {

                val isOpenedFile = projectOpenedFileList
                    .find { it.name.friendlyURI == targetUri } != null

                if (isOpenedFile) {
                    return
                }

                projectOpenedFileList.add(vfsManager.resolveFile(targetUri))
            }

            is FileCloseEvent -> {
                projectOpenedFileList.removeIf {
                    it.name.friendlyURI == targetUri
                }
            }

            is FileChangeOrderEvent -> {
                val oldFile = projectOpenedFileList[event.newOrder]
                val newFile = projectOpenedFileList[event.oldOrder]
                projectOpenedFileList[event.newOrder] = newFile
                projectOpenedFileList[event.oldOrder] = oldFile
            }
        }
    }



}


data class OpenedFileObject(
    @SerializedName("opened_files")
    val openedFiles: MutableList<OpenedFile>
) {
    data class OpenedFile(
        val uri: String
    )
}