package com.dingyi.unluactool.engine.lasm.indexer

import com.dingyi.unluactool.core.project.CompositeProjectIndexer
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManagerListener

class LasmIndexerProjectListener:ProjectManagerListener {
    override fun projectOpened(project: Project) {
        val indexer = project.getIndexer<Any>() as CompositeProjectIndexer
        if (!indexer.hasIndexer<LasmIndexer>()) {
            indexer.addIndexer(LasmIndexer())
        }
    }

    override fun projectClosed(project: Project) {

    }
}