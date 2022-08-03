package com.dingyi.unluactool.core.project

interface ProjectManager {
    suspend fun resolveAllProject(): List<Project>

    fun getAllProject(): List<Project>?


    fun getProjectCount(): Int
}