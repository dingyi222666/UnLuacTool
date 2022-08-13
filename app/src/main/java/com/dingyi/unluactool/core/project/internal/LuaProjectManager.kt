package com.dingyi.unluactool.core.project.internal

import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.common.ktx.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.VFS
import java.io.File

class LuaProjectManager : ProjectManager {

    private var projectRootPath: FileObject
    private var allProject = mutableListOf<LuaProject>()


    init {
        projectRootPath = VFS.getManager().resolveFile(File(Paths.projectDir.value).toURI())
    }

    override suspend fun resolveAllProject(): List<Project> = withContext(Dispatchers.IO) {
        val copyOfProject = projectRootPath.children.mapNotNull {
            runCatching {
                LuaProject(it)
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
        return getAllProject().first { it.projectPath.uri == path.uri }
    }

    override suspend fun resolveProjectByPath(path: FileObject): Project? {
        resolveAllProject()
        return getProjectByPath(path)
    }
}