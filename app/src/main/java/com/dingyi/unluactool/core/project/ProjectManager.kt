package com.dingyi.unluactool.core.project

import com.dingyi.unluactool.core.event.EventType
import org.apache.commons.vfs2.FileObject

interface ProjectManager {
    suspend fun resolveAllProject(): List<Project>

    fun getAllProject(): List<Project>?

    fun getProjectCount(): Int

    fun setProjectRootPath(fileObject: FileObject)

    fun getProjectByPath(path: FileObject): Project?

    suspend fun resolveProjectByPath(path: FileObject): Project?


    fun getCurrentProject(): Project

    companion object {
        val projectListenerType = EventType.create<ProjectManagerListener>()
    }
}