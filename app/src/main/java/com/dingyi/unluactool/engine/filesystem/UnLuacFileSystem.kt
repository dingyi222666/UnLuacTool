package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.core.project.Project
import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileSystem
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider

class UnLuacFileSystem(
    rootFileName: FileName,
    rootFile: String,
    fileSystemOptions: FileSystemOptions?
) : AbstractFileSystem(rootFileName, null, fileSystemOptions) {
    override fun createFile(name: AbstractFileName): FileObject {
        TODO("Not yet implemented")
    }

    override fun init() {

    }

    override fun addCapabilities(caps: MutableCollection<Capability>) {
        caps.addAll(UnLuacFileProvider.allCapability);
    }
}