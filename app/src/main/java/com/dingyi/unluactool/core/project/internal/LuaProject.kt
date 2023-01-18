package com.dingyi.unluactool.core.project.internal

import com.dingyi.unluactool.common.ktx.encodeToJson
import com.dingyi.unluactool.common.ktx.outputStream
import com.dingyi.unluactool.core.event.EventManager
import com.dingyi.unluactool.core.progress.ProgressState
import com.dingyi.unluactool.core.project.CompositeProjectIndexer
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectIndexer
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.service.ServiceRegistry
import com.dingyi.unluactool.core.service.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSelectInfo
import org.apache.commons.vfs2.FileSelector

internal class LuaProject constructor(
    private val serviceRegistry: ServiceRegistry,
    private var projectInfo: ProjectInfo,
    override val projectPath: FileObject
) : Project {

    private var _fileCount: Int = 0

    private val indexer = CompositeProjectIndexer()

    private var isOpened = false

    override val fileCount: Int
        get() = _fileCount

    override var name: String
        get() = projectInfo.name
        set(value) {
            projectInfo.copy(name = value)
                .apply {
                    projectInfo = this
                }
                .update()
        }


    override var projectIconPath: String?
        get() = projectInfo.iconPath
        set(value) {
            projectInfo.copy(iconPath = value)
                .apply {
                    projectInfo = this
                }
                .update()
        }

    override suspend fun resolveProjectFileCount(): Int = withContext(Dispatchers.IO) {
        _fileCount = projectPath
            .resolveFile(ORIGIN_DIR_NAME)
            .findFiles(LuaFileSelector)
            .size

        fileCount
    }

    override suspend fun getProjectFileList(): List<FileObject> {
        return projectPath
            .resolveFile(ORIGIN_DIR_NAME)
            .findFiles(LuaFileSelector).toList()
    }

    override fun getProjectPath(attr: String): FileObject {
        return when (attr) {
            Project.PROJECT_SRC_NAME -> projectPath.resolveFile(ORIGIN_DIR_NAME)
            CACHE_DIR_NAME -> projectPath.resolveFile(CACHE_DIR_NAME)
            Project.PROJECT_INDEXED_NAME -> projectPath.resolveFile(LASM_DIR_NAME)
            BACKUP_DIR_NAME -> projectPath.resolveFile(BACKUP_DIR_NAME)
            else -> projectPath.resolveFile(PROJECT_CONFIG_JSON)
        }
    }

    override fun getProjectPath(attr: String, name: String): FileObject {
        return getProjectPath(attr).resolveFile(name)
    }

    private fun ProjectInfo.update() {
        //
        getProjectPath(PROJECT_CONFIG_JSON)
            .use { fileObject ->
                if (!fileObject.isFile) {
                    fileObject.close()
                    error("Can't resolve file info")
                }
                fileObject.outputStream
                    .use { it.write(this.encodeToJson().encodeToByteArray()) }

            }
    }

    override suspend fun remove(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            projectPath.deleteAll()
        }.isSuccess
    }

    override suspend fun close() = withContext(Dispatchers.IO) {
        serviceRegistry
            .get<EventManager>()
            .syncPublisher(ProjectManager.projectListenerType)
            .projectClosed(this@LuaProject)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LuaProject

        if (projectPath.name.friendlyURI != other.projectPath.name.friendlyURI) return false
        if (name != other.name) return false
        if (projectIconPath != other.projectIconPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = projectPath.name.friendlyURI.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun <T> getIndexer(): ProjectIndexer<T> {
        return indexer as ProjectIndexer<T>
    }

    override suspend fun open(progressState: ProgressState?) {
        indexer.index(this, progressState)
        isOpened = true
    }

    override fun isOpened() = this.isOpened

    companion object {

        const val ORIGIN_DIR_NAME = "origin"

        const val LASM_DIR_NAME = "lasm"

        const val PROJECT_CONFIG_JSON = ".project.json"

        const val CACHE_DIR_NAME = "cache"

        const val BACKUP_DIR_NAME = "backup"

    }


    object LuaFileSelector : FileSelector {
        override fun includeFile(fileInfo: FileSelectInfo): Boolean {
            return fileInfo.file.run { isFile && name.extension == "lua" }
        }

        override fun traverseDescendents(fileInfo: FileSelectInfo): Boolean {
            return true
        }
    }

    data class ProjectInfo(
        val iconPath: String?,
        val name: String,
        val path: String
    )
}