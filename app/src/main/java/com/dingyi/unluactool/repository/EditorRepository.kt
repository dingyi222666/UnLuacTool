package com.dingyi.unluactool.repository

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.event.EventManager
import com.dingyi.unluactool.core.file.FileEventListener
import com.dingyi.unluactool.core.file.FileOpenEvent
import com.dingyi.unluactool.core.file.OpenFileManager
import com.dingyi.unluactool.core.file.OpenedFileManager
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.service.get
import org.apache.commons.vfs2.FileObject

object EditorRepository {

    private val globalServiceRegistry by lazy(LazyThreadSafetyMode.NONE) {
        MainApplication.instance
            .globalServiceRegistry
    }

    private val _eventManager by lazy(LazyThreadSafetyMode.NONE) {
        globalServiceRegistry
            .get<EventManager>()
    }

    private val openedFileManager by lazy(LazyThreadSafetyMode.NONE) {
        globalServiceRegistry.get<OpenedFileManager>()
    }

    private val _openFileManager by lazy(LazyThreadSafetyMode.NONE) {
        globalServiceRegistry.get<OpenFileManager>()
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

    fun getOpenFileManager() = _openFileManager

    fun openFileObject(targetFileUri: String, projectUri: String) {
        _eventManager.syncPublisher(FileEventListener.EVENT_TYPE)
            .onEvent(
                FileOpenEvent(
                    targetFileUri = targetFileUri,
                    projectUri = projectUri
                )
            )
    }


    suspend fun queryAllOpenedFile(project: Project): List<FileObject> {
        return openedFileManager.queryAllOpenedFile(project)
    }

    fun queryCacheOpenedFile(project: Project): List<FileObject> {
        return openedFileManager.queryCacheOpenedFile(project)
    }

    suspend fun saveAllOpenedFile(project: Project) {
        openedFileManager.saveAllOpenedFile(project)
    }

    suspend fun openFile(uri: String): String {
        return _openFileManager.openFile(uri)
    }

    suspend fun loadFileInCache(uri: String): String {
        return _openFileManager.loadFileInCache(uri)
    }

    suspend fun saveFile(uri: String, content: String? = null) {
        _openFileManager.saveFile(uri, content)
    }

}