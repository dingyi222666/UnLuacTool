package com.dingyi.unluactool.core.project

import com.dingyi.unluactool.core.project.internal.LuaProjectCreator

class ProjectServiceRegistry {


    fun createProjectCreator(): ProjectCreator {
        return LuaProjectCreator()
    }

    fun createProjectManager(): ProjectManager {
        TODO("need implement")
    }
}