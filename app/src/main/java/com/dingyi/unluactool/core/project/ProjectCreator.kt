package com.dingyi.unluactool.core.project

import android.content.ContentResolver
import android.net.Uri
import org.apache.commons.vfs2.FileObject

interface ProjectCreator {
    suspend fun createProject(contentResolver: ContentResolver, uri: Uri)
}