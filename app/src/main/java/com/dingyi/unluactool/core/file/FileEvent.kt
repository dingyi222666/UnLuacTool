package com.dingyi.unluactool.core.file

open class FileEvent(
    val targetFileUri: String
)

class OpenFileEvent(uri: String) : FileEvent(uri)

class CloseFileEvent(uri: String) : FileEvent(uri)

class ChangeFileOrder(
    val newOrder: Int,
    val oldOrder: Int,
    uri: String
) : FileEvent(uri)

interface FileEventListener {
    fun onEvent(event: FileEvent)
}