package com.dingyi.unluactool.core.project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LuaProjectResolver(
    private val resolverFile: File
) {


    fun resolveProject(relativeFile: File): LuaProject? {
        val targetPath = resolverFile.resolve(relativeFile)
        return runCatching {
            LuaProject(targetPath)
        }.getOrNull()
    }

    suspend fun resolveAllProject(deep: Int) = withContext(Dispatchers.IO) {
        resolverFile.walk(
        ).maxDepth(deep)
            .filter {
                it != resolverFile
            }.map { LuaProject(it) }
    }
}