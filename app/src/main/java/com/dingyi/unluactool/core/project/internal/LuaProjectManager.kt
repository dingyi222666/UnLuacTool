package com.dingyi.unluactool.core.project.internal

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.common.ktx.Paths
import com.dingyi.unluactool.core.event.EventManager
import com.dingyi.unluactool.core.project.ProjectIndexer
import com.dingyi.unluactool.core.project.ProjectManagerListener
import com.dingyi.unluactool.core.service.ServiceRegistry
import com.dingyi.unluactool.core.service.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.VFS
import java.io.File
import java.util.Collections

class LuaProjectManager(
    private val serviceRegistry: ServiceRegistry
) : ProjectManager {

    private var projectRootPath: FileObject
    private var allProject = mutableListOf<LuaProject>()

    private var currentProject: Project = EmptyProject
        set(value) {
            field = value
            serviceRegistry
                .get<EventManager>()
                .syncPublisher(ProjectManager.projectListenerType)
                .projectOpened(value)
        }

    init {
        projectRootPath = VFS.getManager().resolveFile(File(Paths.projectDir.value).toURI())

        if (!projectRootPath.isFolder) {
            projectRootPath.createFolder()
        }

    }

    override suspend fun resolveAllProject(): List<Project> = withContext(Dispatchers.IO) {
        val copyOfProject = projectRootPath.children.mapNotNull {
            runCatching {
                LuaProject(serviceRegistry,it)
            }.onFailure {
                it.printStackTrace()
            }.getOrNull()
        }.toMutableList()

        allProject = copyOfProject
        allProject
    }

    override fun getAllProject(): List<Project> {
        return allProject
    }

    override fun getProjectCount(): Int {
        return allProject.size
    }

    override fun setProjectRootPath(fileObject: FileObject) {
        projectRootPath = fileObject
    }

    override fun getProjectByPath(path: FileObject): Project? {
        return getAllProject().first { it.projectPath.uri == path.uri }.apply {
            currentProject = this
        }
    }

    override suspend fun resolveProjectByPath(path: FileObject): Project? {
        resolveAllProject()
        return getProjectByPath(path).apply {
            currentProject = this ?: EmptyProject
        }
    }

    override fun getCurrentProject(): Project {
        return currentProject
    }
}

object EmptyProject : Project {
    override val fileCount: Int
        get() = error("No support")
    override val projectPath: FileObject
        get() = TODO("Not yet implemented")
    override var projectIconPath: String? = ""
    override var name = "1"

    override suspend fun resolveProjectFileCount(): Int = 0

    override suspend fun remove(): Boolean = false
    override suspend fun close() {
        TODO("Not yet implemented")
    }

    override fun <T> getIndexer(): ProjectIndexer<T> {
        TODO("Not yet implemented")
    }

    override suspend fun getProjectFileList() = Collections.emptyList<FileObject>()

    override fun getProjectPath(attr: String): FileObject {
        TODO("Not yet implemented")
    }

    override suspend fun open() {
        TODO("Not yet implemented")
    }

}