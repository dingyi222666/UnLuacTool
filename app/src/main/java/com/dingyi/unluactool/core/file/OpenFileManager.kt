package com.dingyi.unluactool.core.file

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.VFS

class OpenFileManager internal constructor() : FileEventListener {

    private val cacheOpenedFile = mutableMapOf<String, CacheOpenFileObject>()

    private val vfsManager = VFS.getManager()

    private lateinit var coroutineScope: CoroutineScope

    suspend fun openFile(uri: String): String = withContext(Dispatchers.IO) {
        val fileObject = vfsManager.resolveFile(uri)
        val fileContent = kotlin.runCatching {
            fileObject.content.inputStream
                .readBytes()
                .decodeToString()
        }.getOrNull()
        val cacheContent = cacheOpenedFile[uri] ?: CacheOpenFileObject(
            uri = uri,
            content = fileContent
        )
        cacheContent.content = fileContent
        cacheOpenedFile[uri] = cacheContent
        cacheContent.content ?: ""
    }

    suspend fun saveFile(uri: String, content: String?) = withContext(Dispatchers.IO) {
        val saveContent = cacheOpenedFile[uri]?.content ?: content ?: return@withContext

        val fileObject = vfsManager.resolveFile(uri)

        fileObject.content
            .outputStream
            .bufferedWriter()
            .use {
                it.write(saveContent)
            }

    }

    suspend fun loadFileInCache(uri: String): String {
        val cacheContent = cacheOpenedFile[uri]
        return cacheContent?.content ?: openFile(uri)
    }

    suspend fun bindCoroutineScope(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope
    }


    fun close() {
        cacheOpenedFile.clear()
    }

    override fun onEvent(event: FileEvent) {

    }
}

class CacheOpenFileObject(
    val uri: String,
    var content: String?
)