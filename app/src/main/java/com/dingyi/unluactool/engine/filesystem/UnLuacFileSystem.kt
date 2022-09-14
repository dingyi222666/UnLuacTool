package com.dingyi.unluactool.engine.filesystem

import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileSystem

class UnLuacFileSystem(
    rootFileName: FileName,
    rootFile: String,
    fileSystemOptions: FileSystemOptions?
) : AbstractFileSystem(rootFileName, null, fileSystemOptions) {
    override fun createFile(name: AbstractFileName?): FileObject {
        TODO("Not yet implemented")
    }

    override fun addCapabilities(caps: MutableCollection<Capability>?) {
        TODO("Not yet implemented")
    }
}