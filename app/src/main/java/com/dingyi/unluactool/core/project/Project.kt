package com.dingyi.unluactool.core.project

import android.os.Parcel
import android.os.Parcelable
import com.dingyi.unluactool.core.event.EventType
import com.dingyi.unluactool.core.progress.ProgressState
import org.apache.commons.vfs2.FileObject
import java.io.File

interface Project {

    val fileCount: Int

    val projectPath: FileObject

    var projectIconPath: String?

    var name: String

    suspend fun resolveProjectFileCount(): Int

    suspend fun remove(): Boolean

    suspend fun close()

    fun <T> getIndexer(): ProjectIndexer<T>

    suspend fun getProjectFileList(): List<FileObject>

    fun getProjectPath(attr: String): FileObject
    suspend fun open(progressState: ProgressState? = null)

    companion object {
        const val PROJECT_SRC_NAME = "srcDir"
        const val PROJECT_INDEXED_NAME = "indexedDir"


    }
}