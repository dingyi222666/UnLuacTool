package com.dingyi.unluactool.core.file

open class FileEvent(
    val targetFileUri: String,
    val projectUri: String
)

class FileOpenEvent(targetFileUri: String, projectUri: String) :
    FileEvent(targetFileUri, projectUri)

class FileCloseEvent(targetFileUri: String, projectUri: String) :
    FileEvent(targetFileUri, projectUri)

class FileSaveEvent(
    targetFileUri: String, projectUri: String,
    val saveContent: (() -> String?)? = null
) : FileEvent(targetFileUri, projectUri)

class FileChangeOrderEvent(
    val newOrder: Int,
    val oldOrder: Int,
    targetFileUri: String,
    projectUri: String
) : FileEvent(targetFileUri, projectUri)


interface FileEventListener {
    fun onEvent(event: FileEvent)
}