package com.dingyi.unluactool.core.project

class CompositeProjectIndexer : ProjectIndexer<List<Any>> {

    private val allIndexer = mutableListOf<ProjectIndexer<Any>>()

    override suspend fun index(project: Project): List<Any> =
        allIndexer.map { it.index(project) }.toList()

    fun addIndexer(indexer: ProjectIndexer<Any>) {
        allIndexer.add(indexer)
    }

    fun removeIndexer(indexer: ProjectIndexer<Any>) {
        allIndexer.remove(indexer)
    }

}