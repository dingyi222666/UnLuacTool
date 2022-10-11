package com.dingyi.unluactool.core.project

class CompositeProjectIndexer : ProjectIndexer<List<Any>> {

    private val allIndexer = mutableListOf<ProjectIndexer<*>>()

    override suspend fun index(project: Project): List<Any> =
        allIndexer.map { it.index(project) as Any }.toList()

    fun <T : Any> addIndexer(indexer: ProjectIndexer<T>) {
        allIndexer.add(indexer)
    }

    fun <T : Any> removeIndexer(indexer: ProjectIndexer<T>) {
        allIndexer.remove(indexer)
    }

    fun hasIndexer(clazz: Class<*>): Boolean {
        return allIndexer.find { it::class.java == clazz } != null
    }


    inline fun <reified T> hasIndexer() = hasIndexer(T::class.java)
}