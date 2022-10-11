package com.dingyi.unluactool.core.project

import com.dingyi.unluactool.core.progress.ProgressState

fun interface ProjectIndexer<T>  {

    suspend fun index(project: Project,progressState: ProgressState?): T


}