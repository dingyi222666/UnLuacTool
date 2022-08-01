package com.dingyi.unluactool.core.project

import org.apache.commons.vfs2.FileObject

interface ProjectCreator {

    suspend fun createProject(fileObject: FileObject, fileName: String)

}