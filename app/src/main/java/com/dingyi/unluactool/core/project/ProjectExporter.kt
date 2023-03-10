package com.dingyi.unluactool.core.project

import android.content.Context
import android.net.Uri
import com.dingyi.unluactool.MainApplication
import net.lingala.zip4j.io.outputstream.ZipOutputStream
import org.apache.commons.vfs2.VFS

object ProjectExporter {
    fun exportProject(uri: Uri?, project: Project?) {
        if (uri == null || project == null) return
        val context = MainApplication.instance
        val contentResolver = context.contentResolver

        val fileOutputStream = contentResolver.openOutputStream(uri)
        project.exportProject(ZipOutputStream(fileOutputStream))

    }
}