package com.dingyi.unluactool.core.project

interface ProjectManagerListener {

    fun projectOpened(project: Project)

    fun projectClosed(project: Project)
}