package com.dingyi.unluactool.core.project.internal

import com.dingyi.unluactool.common.ktx.decodeToBean
import com.dingyi.unluactool.common.ktx.inputStream
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectInstanceCreator
import com.dingyi.unluactool.core.service.ServiceRegistry
import org.apache.commons.vfs2.FileObject

class LuaProjectInstanceCreator : ProjectInstanceCreator {
    override fun createProject(
        projectPath: FileObject,
        serviceRegistry: ServiceRegistry
    ): Project? {
        val projectInfo = kotlin.runCatching {
            projectPath.resolveFile(LuaProject.PROJECT_CONFIG_JSON)
                .use { fileObject ->
                    if (!fileObject.isFile) {
                        error("Can't resolve file info")
                    }

                    fileObject.inputStream
                        .use { it.readBytes() }
                        .decodeToString()
                        .decodeToBean<LuaProject.ProjectInfo>()
                }
        }.getOrNull()

        if (projectInfo != null) {
            return LuaProject(serviceRegistry, projectInfo, projectPath)
        }

        return null
    }
}