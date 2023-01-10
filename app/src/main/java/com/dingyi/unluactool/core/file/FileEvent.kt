package com.dingyi.unluactool.core.file

open class FileEvent(
    val targetFileUri: String,
    val projectUri: String
)

class OpenFileEvent(targetFileUri: String, projectUri: String) :
    FileEvent(targetFileUri, projectUri)

class CloseFileEvent(targetFileUri: String, projectUri: String) :
    FileEvent(targetFileUri, projectUri)

class ChangeFileOrderEvent(
    val newOrder: Int,
    val oldOrder: Int,
    targetFileUri: String,
    projectUri: String
) : FileEvent(targetFileUri, projectUri)

interface FileEventListener {
    fun onEvent(event: FileEvent)
}