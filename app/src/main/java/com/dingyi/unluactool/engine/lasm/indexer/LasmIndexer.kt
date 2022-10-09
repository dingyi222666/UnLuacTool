package com.dingyi.unluactool.engine.lasm.indexer

import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectIndexer
import com.dingyi.unluactool.engine.lasm.data.LASMChunk

class LasmIndexer : ProjectIndexer<List<LASMChunk>> {
    override suspend fun index(project: Project): List<LASMChunk> {
        val allProjectFileList = project.getProjectFileList()

        val projectSrcPath = project.getProjectPath(Project.PROJECT_SRC_NAME)



    }
}