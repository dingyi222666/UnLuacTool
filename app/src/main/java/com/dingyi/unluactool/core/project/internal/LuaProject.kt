package com.dingyi.unluactool.core.project.internal

import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.ktx.decodeToBean
import com.dingyi.unluactool.ktx.encodeToJson
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSelectInfo
import org.apache.commons.vfs2.FileSelector

internal class LuaProject constructor(
    private val projectDir: FileObject
) : Project {

    private var projectInfo = readInfo()


    override fun getName(): String {
        return projectInfo.name
    }


    override fun getProjectFileCount(): Int {
        return projectDir
            .findFiles(object : FileSelector {
                override fun includeFile(fileInfo: FileSelectInfo): Boolean {
                    return fileInfo.file.run { isFile && name.extension == "lua" }
                }


                override fun traverseDescendents(fileInfo: FileSelectInfo): Boolean {
                    return true
                }

            })
            .size
    }

    override fun getProjectPath(): FileObject {
        return projectDir
    }

    override fun getProjectIconPath(): String? {
       return projectInfo.iconPath
    }

    override fun setProjectIconPath(path: String) {
        projectInfo.copy(iconPath = path).update()
    }

    override fun setName(name: String) {
        projectInfo.copy(name = name).update()
    }

    private fun ProjectInfo.update() {
        projectDir.resolveFile(".project.json")
            .use { fileObject ->

                if (!fileObject.isFile) {
                    fileObject.close()
                    error("Can't resolve file info")
                }

                fileObject.content.outputStream
                    .use { it.write(this.encodeToJson().encodeToByteArray()) }

            }

    }

    private fun readInfo(): ProjectInfo {
        return projectDir.resolveFile(".project.json")
            .use { fileObject ->

                if (!fileObject.isFile) {
                    error("Can't resolve file info")
                }

                fileObject.content.inputStream
                    .use { it.readBytes() }
                    .decodeToString()
                    .decodeToBean()

            }

    }

}