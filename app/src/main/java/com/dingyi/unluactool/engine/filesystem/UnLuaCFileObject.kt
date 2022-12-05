package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileObject
import org.apache.commons.vfs2.provider.local.LocalFile
import org.apache.commons.vfs2.provider.local.LocalFileSystem
import java.io.InputStream
import java.io.OutputStream

class UnLuaCFileObject(
    private val proxyFileObject: FileObject,
    private var parsedFileObject: UnLuacParsedFileObject? = null,
    name: AbstractFileName, fileSystem: UnLuacFileSystem
) :
    AbstractFileObject<UnLuacFileSystem>(name, fileSystem) {

    companion object {
        private val emptyArray = arrayOf<String>()
    }

    override fun doGetContentSize(): Long {
        return proxyFileObject.content.size
    }

    override fun refresh() {

    }


    private fun checkReadContent() {

    }

    override fun doGetInputStream(): InputStream {
        return proxyFileObject.content.inputStream
    }

    override fun doGetType(): FileType {
        return proxyFileObject.type
    }


    override fun doGetOutputStream(bAppend: Boolean): OutputStream {
        return proxyFileObject.content.outputStream
    }

    override fun doListChildren(): Array<String> {

        return when {
            proxyFileObject.isFolder -> {
                proxyFileObject.children.map {
                    it.name.friendlyURI.replace("file:", "unluac:")
                }.toTypedArray()
            }
            else -> emptyArray
        }
    }

    override fun doDelete() {
        super.doDelete()
    }
}