package com.dingyi.unluactool.core.project.internal

import android.os.Parcel
import android.os.Parcelable
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.common.ktx.decodeToBean
import com.dingyi.unluactool.common.ktx.encodeToJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSelectInfo
import org.apache.commons.vfs2.FileSelector

internal class LuaProject constructor(
    override val projectPath: FileObject
) : Project {

    private var projectInfo = readInfo()

    private var _fileCount: Int = 0

    override val fileCount: Int
        get() = _fileCount

    override var name: String
        get() = projectInfo.name
        set(value) {
            projectInfo.copy(name = value)
                .apply {
                    projectInfo = this
                }
                .update()
        }


    override var projectIconPath: String?
        get() = projectInfo.iconPath
        set(value) {
            projectInfo.copy(iconPath = value)
                .apply {
                    projectInfo = this
                }
                .update()
        }



    override suspend fun resolveProjectFileCount(): Int = withContext(Dispatchers.IO) {
        _fileCount = projectPath
            .findFiles(object : FileSelector {
                override fun includeFile(fileInfo: FileSelectInfo): Boolean {
                    return fileInfo.file.run { isFile && name.extension == "lua" }
                }

                override fun traverseDescendents(fileInfo: FileSelectInfo): Boolean {
                    return true
                }

            })
            .size

        fileCount
    }


    private fun ProjectInfo.update() {
        //
        projectPath.resolveFile(PROJECT_CONFIG_JSON)
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
        return projectPath.resolveFile(PROJECT_CONFIG_JSON)
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

    override suspend fun remove(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            projectPath.deleteAll()
        }.isSuccess
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LuaProject

        if (projectPath.publicURIString != other.projectPath.publicURIString) return false
        if (name != other.name) return false
        if (projectIconPath != other.projectIconPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = projectPath.publicURIString.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }


    companion object {

        const val ORIGIN_DIR_NAME = "origin"

        const val LASM_DIR_NAME = "lasm"

        const val PROJECT_CONFIG_JSON = ".project.json"

    }



}