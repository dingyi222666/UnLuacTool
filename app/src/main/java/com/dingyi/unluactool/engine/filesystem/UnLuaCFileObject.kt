package com.dingyi.unluactool.engine.filesystem

import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileObject

class UnLuaCFileObject(name: AbstractFileName, fileSystem: UnLuacFileSystem) :
    AbstractFileObject<UnLuacFileSystem>(name, fileSystem) {
    override fun doGetContentSize(): Long {
        TODO("Not yet implemented")
    }

    override fun doGetType(): FileType {
        TODO("Not yet implemented")
    }

    override fun doListChildren(): Array<String> {
        TODO("Not yet implemented")
    }
}