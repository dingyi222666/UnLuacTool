package com.dingyi.unluactool.core.project

import com.dingyi.unluactool.core.service.ServiceRegistry
import org.apache.commons.vfs2.FileObject

fun interface ProjectInstanceCreator {

    fun createProject(projectPath: FileObject, serviceRegistry: ServiceRegistry): Project?
}