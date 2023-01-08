package com.dingyi.unluactool.core.file

import com.dingyi.unluactool.core.project.Project
import org.apache.commons.vfs2.FileObject

class OpenedFileManager internal constructor() : FileEventListener {

    private val cacheOpenedFile = mutableMapOf<String,List<FileObject>>()

    suspend fun queryAllOpenedFile(project: Project):List<FileObject> {
        val uri = project.projectPath.publicURIString
        TODO("Not yet implemented")
    }

    fun queryCacheOpenedFile(project: Project):List<FileObject> {
        TODO("Not yet implemented")
    }

    override fun onEvent(event: FileEvent) {
        TODO("Not yet implemented")
    }

}