package com.dingyi.unluactool.repository

import android.view.MenuItem
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.event.EventManager
import com.dingyi.unluactool.core.file.FileCloseEvent
import com.dingyi.unluactool.core.file.FileContentChangeEvent
import com.dingyi.unluactool.core.file.FileEventListener
import com.dingyi.unluactool.core.file.FileOpenEvent
import com.dingyi.unluactool.core.file.OpenFileManager
import com.dingyi.unluactool.core.file.OpenedFileTabManager
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.ui.editor.event.MenuEvent
import com.dingyi.unluactool.ui.editor.fileTab.OpenedFileTabData
import io.github.rosemoe.sora.event.ContentChangeEvent
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

    private val openedFileTabManager by lazy(LazyThreadSafetyMode.NONE) {
        globalServiceRegistry.get<OpenedFileTabManager>()
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

    fun getOpenFileTabManager() = _openFileManager

    fun openFile(targetFileUri: String, projectUri: String) {
        _eventManager.syncPublisher(FileEventListener.EVENT_TYPE)
            .onEvent(
                FileOpenEvent(
                    targetFileUri = targetFileUri,
                    projectUri = projectUri
                )
            )
    }

    fun closeFile(targetFileUri: String, projectUri: String) {
        _eventManager.syncPublisher(FileEventListener.EVENT_TYPE)
            .onEvent(
                FileCloseEvent(
                    targetFileUri = targetFileUri,
                    projectUri = projectUri
                )
            )
    }

    fun contentChangeFile(event: ContentChangeEvent, targetFileUri: String, projectUri: String) {
        _eventManager
            .syncPublisher(FileEventListener.EVENT_TYPE)
            .onEvent(
                FileContentChangeEvent(
                    newContent = event.editor.text.toString(),
                    targetFileUri, projectUri
                )
            )
    }


    suspend fun queryAllOpenedFileTab(project: Project): List<FileObject> {
        return openedFileTabManager.queryAllOpenedFileTab(project)
    }

    fun queryCacheOpenedFileTab(project: Project): List<FileObject> {
        return openedFileTabManager.queryCacheOpenedFileTab(project)
    }

    suspend fun saveAllOpenedFileTab(project: Project) {
        openedFileTabManager.saveAllOpenedFileTab(project)
    }

    suspend fun openFile(fileObject: FileObject): String {
        return _openFileManager.openFile(fileObject)
    }

    suspend fun loadFileInCache(fileObject: FileObject): String {
        return _openFileManager.loadFileInCache(fileObject)
    }

    fun checkFileIsSave(fileObject: FileObject): Boolean {
        return _openFileManager.checkFileIsSave(fileObject)
    }

    suspend fun saveFile(fileObject: FileObject, content: String? = null) {
        _openFileManager.saveFile(fileObject, content)
    }

    fun dispatchMenuClickEvent(item: MenuItem) {
        _eventManager
            .syncPublisher(MenuEvent.eventType)
            .onClick(item)
    }
}