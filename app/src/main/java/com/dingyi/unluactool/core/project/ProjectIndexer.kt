package com.dingyi.unluactool.core.project

fun interface ProjectIndexer<T>  {

    suspend fun index(project: Project): T



}