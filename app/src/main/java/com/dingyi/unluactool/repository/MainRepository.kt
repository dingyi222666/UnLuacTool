package com.dingyi.unluactool.repository

import android.content.ContentResolver
import android.net.Uri
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.beans.HitokotoBean
import com.dingyi.unluactool.core.project.ProjectCreator
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.network.SimpleHttp
import com.dingyi.unluactool.network.createJsonConvert

object MainRepository {

    suspend fun refreshHitokoto(): String {
        val url = "https://v1.hitokoto.cn?c=a&c=c&c=b"

        val hitokotoBean = SimpleHttp.get(url, createJsonConvert<HitokotoBean>())
        return hitokotoBean?.let {
            it.hitokoto + " - " + it.from
        } ?: "???"
    }

    suspend fun resolveAllProject() =
        MainApplication.instance.globalServiceRegistry
            .get<ProjectManager>()
            .resolveAllProject()
            .onEach {
                it.resolveProjectFileCount()
            }

    suspend fun createProject(contentResolver: ContentResolver, uri: Uri) {
        MainApplication.instance.globalServiceRegistry
            .get<ProjectCreator>()
            .createProject(contentResolver, uri)
    }

}

