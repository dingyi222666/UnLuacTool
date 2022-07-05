package com.dingyi.unluactool.core.project

import com.dingyi.unluactool.R
import com.dingyi.unluactool.beans.ProjectInfoBean
import com.dingyi.unluactool.ktx.Paths
import com.dingyi.unluactool.ktx.getString
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.util.Zip4jUtil
import java.io.File

object LuaProjectCreator {


    suspend fun createLuaProjectFromFile(file: File, isZipFile: Boolean) =
        withContext(Dispatchers.IO) {

            val projectName = getProjectName()
            val projectPath = File(Paths.projectDir.value, projectName)
            projectPath.resolve("origin").mkdirs()
            if (!isZipFile) {
                file.copyTo(projectPath.resolve("origin/main.lua"), true)
            } else {
                val zipFile = ZipFile(file)
                kotlin.runCatching {
                    zipFile.fileHeaders.forEach {
                        val fileName = it.fileName
                        if (fileName.endsWith(".lua")) {
                            zipFile.extractFile(it, projectPath.resolve("origin").absolutePath)
                        }
                    }
                    zipFile.close()
                }.onFailure {
                    it.printStackTrace()
                }
            }

            file.delete()

            projectPath.resolve(".project.json")
                .writeText(Gson().toJson(generateProjectInfo(projectPath, projectName)))
        }


    private fun generateProjectInfo(projectPath: File, projectName: String): ProjectInfoBean {
        val luaFileCount = projectPath.walk().map { it.name }.filter { it.endsWith(".lua") }.count()
        return ProjectInfoBean(
            name = projectName,
            fileCountOfLuaFile = luaFileCount,
            path = projectPath.path,
            icon = null
        )
    }

    private fun getProjectName(): String {
        return getString(R.string.main_temporary_project_name) + "_" + getProjectCount()
    }

    private fun getProjectCount() =
        (File(Paths.projectDir.value).listFiles()?.size?.plus(1)) ?: 1

}