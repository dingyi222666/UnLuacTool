package com.dingyi.unluactool.repository

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.event.EventManager
import com.dingyi.unluactool.core.file.FileEvent
import com.dingyi.unluactool.core.file.FileEventListener
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.service.get

object EditorRepository {

    private val _eventManager by lazy(LazyThreadSafetyMode.NONE) {
        MainApplication.instance
            .globalServiceRegistry
            .get<EventManager>()
    }

    suspend fun loadProject(uri: String): Project {
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

    fun getEventManager() = _eventManager

    fun openFileObject(targetFileUri: String, projectUri: String) {
        _eventManager.syncPublisher(FileEventListener.EVENT_TYPE)
            .onEvent(
                FileEvent(
                    targetFileUri = targetFileUri,
                    projectUri = projectUri
                )
            )
    }

}