package com.dingyi.unluactool.core.project.internal

import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.common.ktx.Paths
import com.dingyi.unluactool.core.event.EventManager
import com.dingyi.unluactool.core.progress.ProgressState
import com.dingyi.unluactool.core.project.ProjectIndexer
import com.dingyi.unluactool.core.project.ProjectInstanceCreator
import com.dingyi.unluactool.core.service.ServiceRegistry
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.core.util.JsonConfigReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.VFS
import java.io.File
import java.util.Collections

class DefaultProjectManager(
    private val serviceRegistry: ServiceRegistry
) : ProjectManager {

    private var projectRootPath: FileObject
    private var allProject = mutableListOf<Project>()
    private val allProjectInstanceCreator = mutableListOf<ProjectInstanceCreator>()

    private var currentProject: Project = EmptyProject
        set(value) {
            field = value
            if (value.isOpened()) {
                return
            }
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

        readGlobalProjectInstanceCreator()

    }

    private fun readGlobalProjectInstanceCreator() {

        val jsonArray = JsonConfigReader.readConfig("project-instance-creator.json")
            .asJsonArray

        jsonArray.forEach {
            val className = it.asString
            addProjectInstanceCreator(
                Class.forName(className).newInstance() as ProjectInstanceCreator
            )
        }
    }

    override suspend fun resolveAllProject(): List<Project> = withContext(Dispatchers.IO) {
        val copyOfProject = projectRootPath.children.mapNotNull {
            runCatching {
                createProject(it)
            }.onFailure {
                it.printStackTrace()
            }.getOrNull()
        }

        allProject = copyOfProject.toMutableList()
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
        return getAllProject().find { it.projectPath.uri == path.uri }.also {
            currentProject = it ?: EmptyProject
        }
    }

    override suspend fun resolveProjectByPath(path: FileObject): Project? {
        resolveAllProject()
        val project = getProjectByPath(path)
        currentProject = project ?: EmptyProject
        return project
    }

    override fun getProjectByName(name: String): Project? {
        return getAllProject().find { it.name == name }.also {
            currentProject = it ?: EmptyProject
        }
    }

    override suspend fun resolveProjectByName(name: String): Project? {
        resolveAllProject()
        return getProjectByName(name).also {
            currentProject = it ?: EmptyProject
        }
    }

    override fun getCurrentProject(): Project {
        return currentProject
    }

    private fun createProject(projectPath: FileObject): Project? {
        for (creator in allProjectInstanceCreator) {
            val result = creator.createProject(projectPath, serviceRegistry)
            if (result != null) {
                return result
            }

        }
        return null
    }

    override fun addProjectInstanceCreator(creator: ProjectInstanceCreator) {
        allProjectInstanceCreator.add(creator)
    }

    override fun removeProjectInstanceCreator(creator: ProjectInstanceCreator) {
        allProjectInstanceCreator.remove(creator)
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

    override fun getProjectPath(attr: String, name: String): FileObject {
        TODO("Not yet implemented")
    }

    override suspend fun open(progressState: ProgressState?) {
        TODO("Not yet implemented")
    }


}