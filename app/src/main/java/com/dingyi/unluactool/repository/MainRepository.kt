package com.dingyi.unluactool.repository

import com.dingyi.unluactool.beans.HitokotoBean
import com.dingyi.unluactool.core.project.LuaProject
import com.dingyi.unluactool.core.project.LuaProjectResolver
import com.dingyi.unluactool.ktx.Paths
import com.dingyi.unluactool.network.SimpleHttp
import com.dingyi.unluactool.network.createJsonConvert
import java.io.File

object MainRepository {

    suspend fun refreshHitokoto(): String {
        val url = "https://v1.hitokoto.cn?c=a&c=c&c=b"

        val hitokotoBean = SimpleHttp.get(url, createJsonConvert<HitokotoBean>())
        return hitokotoBean?.let {
            it.hitokoto + " - " + it.from
        } ?: "???"
    }

    suspend fun resolveAllProject() =
        LuaProjectResolver(File(Paths.projectDir.value)).resolveAllProject(1).map {
            it.getProjectInfo()
        }.toList()

}

