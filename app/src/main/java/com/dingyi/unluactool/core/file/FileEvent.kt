package com.dingyi.unluactool.core.file

import android.app.usage.UsageEvents.Event
import com.dingyi.unluactool.core.event.EventType

abstract class FileEvent(
    val targetFileUri: String,
    val projectUri: String
)

class FileOpenEvent(targetFileUri: String, projectUri: String) :
    FileEvent(targetFileUri, projectUri)

class FileCloseEvent(targetFileUri: String, projectUri: String) :
    FileEvent(targetFileUri, projectUri)

class FileSaveEvent(
    targetFileUri: String, projectUri: String,
    val saveContent: (() -> String?)? = null,
) : FileEvent(targetFileUri, projectUri)

class FileContentChangeEvent(
    val newContent: String,
    val oldContent: String,
    targetFileUri: String,
    projectUri: String
) : FileEvent(targetFileUri, projectUri)


class FileChangeOrderEvent(
    val newOrder: Int,
    val oldOrder: Int,
    targetFileUri: String,
    projectUri: String
) : FileEvent(targetFileUri, projectUri)

interface FileEventListener {
    fun onEvent(event: FileEvent)

    companion object {
        val EVENT_TYPE = EventType.create<FileEventListener>()
    }
}