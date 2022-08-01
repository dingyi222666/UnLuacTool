package com.dingyi.unluactool.core.project

import org.apache.commons.vfs2.FileObject
import java.io.File

abstract interface Project {

    fun getName(): String

    fun getProjectFileCount(): Int

    fun getProjectPath(): FileObject

    fun getProjectIconPath(): String?

    fun setProjectIconPath(path: String)

    fun setName(name:String)

}