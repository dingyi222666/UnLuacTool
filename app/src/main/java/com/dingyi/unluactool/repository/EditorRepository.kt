package com.dingyi.unluactool.repository

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.service.get

object EditorRepository {

    suspend fun loadProject(uri:String): Project {
       return MainApplication
            .instance
            .globalServiceRegistry
            .get<ProjectManager>()
            .resolveProjectByPath(
                MainApplication
                    .instance
                    .fileSystemManager
                    .resolveFile(uri)
            ).let { checkNotNull(it) }
    }
}