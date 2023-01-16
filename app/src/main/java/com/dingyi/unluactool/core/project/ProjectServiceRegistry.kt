package com.dingyi.unluactool.core.project

import com.dingyi.unluactool.core.project.internal.LuaProjectCreator
import com.dingyi.unluactool.core.project.internal.DefaultProjectManager
import com.dingyi.unluactool.core.service.ServiceRegistry

class ProjectServiceRegistry {

    fun createProjectCreator(): ProjectCreator {
        return LuaProjectCreator()
    }

    fun createProjectManager(serviceRegistry: ServiceRegistry): ProjectManager {
        return DefaultProjectManager(serviceRegistry)
    }

}