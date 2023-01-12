package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.engine.lasm.data.v1.AbsFunction
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileObject
import java.io.InputStream
import java.io.OutputStream

class UnLuaCFileObject(
    private val proxyFileObject: FileObject,
    private var data: UnLuacFileObjectExtra? = null,
    name: AbstractFileName, fileSystem: UnLuacFileSystem
) :
    AbstractFileObject<UnLuacFileSystem>(name, fileSystem) {

    companion object {
        private val emptyArray = arrayOf<String>()
    }


    private fun isUnLuacParsedObject(): Boolean = data == null

    private fun requireExtra(): UnLuacFileObjectExtra = checkNotNull(data)

    override fun doGetContentSize(): Long {
        return proxyFileObject.content.size
    }

    override fun refresh() {
        if (isUnLuacParsedObject()) {
            proxyFileObject.refresh()
        } else requireExtra().fileObject.refresh()
    }


    override fun doGetInputStream(): InputStream {
        return if (isUnLuacParsedObject()) {
            requireExtra().let {
                it.currentFunction?.data ?: it.fileObject.getAllData()
            }.let {
                requireExtra().fileObject.wrapDataToStream(it)
            }
        } else {
            proxyFileObject.content.inputStream
        }
    }

    override fun doGetType(): FileType {
        return proxyFileObject.type
    }


    override fun doGetOutputStream(bAppend: Boolean): OutputStream {
        return if (isUnLuacParsedObject()) {
            requireExtra().let {
                if (it.currentFunction == null)
                    it.fileObject.writeAllData()
                else it.fileObject.writeData(it.requireFunction())
            }
        } else {
            proxyFileObject.content.outputStream
        }
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
        proxyFileObject.delete()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnLuaCFileObject

        if (proxyFileObject.publicURIString != other.proxyFileObject.publicURIString) return false

        return true
    }

    override fun hashCode(): Int {
        return proxyFileObject.hashCode()
    }

    fun getFileType(): FileObjectType {
        if (proxyFileObject.isFolder) {
            return FileObjectType.DIR
        }

        val currentFunc = data?.currentFunction ?: return FileObjectType.FILE

        if (currentFunc.childFunctions.isNotEmpty()) {
            return FileObjectType.FUNCTION_WITH_CHILD
        }

        return FileObjectType.FUNCTION

    }


}

enum class FileObjectType {
    FUNCTION, FILE, DIR, FUNCTION_WITH_CHILD
}
