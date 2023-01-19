package com.dingyi.unluactool.engine.lasm.indexer

import com.dingyi.unluactool.core.project.CompositeProjectIndexer
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManagerListener

class LasmIndexerProjectListener:ProjectManagerListener {
    override fun projectOpened(project: Project) {
        println("projectOpened")
        val indexer = project.getIndexer<Any>() as CompositeProjectIndexer
        if (!indexer.hasIndexer<LasmIndexer>()) {
            println("add indexer")
            indexer.addIndexer(LasmIndexer())
        }
    }



    override fun projectClosed(project: Project) {

    }
}