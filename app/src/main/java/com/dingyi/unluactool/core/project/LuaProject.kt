package com.dingyi.unluactool.core.project

import com.dingyi.unluactool.beans.ProjectInfoBean
import com.dingyi.unluactool.ktx.decodeToBean
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LuaProject(private val projectPath: File) {

    private val projectInfoBean =
        projectPath.resolve(".project.json").readText().decodeToBean<ProjectInfoBean>()

    fun getName() = projectInfoBean.name

    fun getFileCount() = projectInfoBean.fileCountOfLuaFile

    private suspend fun getRealFileCount() = withContext(Dispatchers.IO) {
        projectPath.walk().map { it.name }.count { it.endsWith(".lua") }
    }

    suspend fun updateProjectInfo() {

        val projectInfoBean =
            projectPath.resolve(".project.json").readText().decodeToBean<ProjectInfoBean>()
        projectInfoBean.fileCountOfLuaFile = getRealFileCount()
        projectPath.resolve(".project.json").writeText(Gson().toJson(projectInfoBean))
    }


    fun resolve(file:File):LuaFile {
        TODO("not implemented")
    }





}