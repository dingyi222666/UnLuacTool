package com.dingyi.unluactool.core.project

import com.dingyi.unluactool.core.project.internal.LuaProjectCreator
import com.dingyi.unluactool.core.project.internal.LuaProjectManager

class ProjectServiceRegistry {

    fun createProjectCreator(): ProjectCreator {
        return LuaProjectCreator()
    }

    fun createProjectManager(): ProjectManager {
        return LuaProjectManager()
    }
}