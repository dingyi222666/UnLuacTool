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
    name: AbstractFileName,
    private val fileSystem: UnLuacFileSystem
) : AbstractFileObject<UnLuacFileSystem>(name, fileSystem) {

    companion object {
        private val emptyArray = arrayOf<String>()
        private val emptyFileObjectArray = arrayOf<FileObject>()
    }


    private var currentFileType: FileObjectType? = null

    private fun isUnLuacParsedObject(): Boolean = data == null

    private fun requireExtra(): UnLuacFileObjectExtra = checkNotNull(data)

    override fun doGetContentSize(): Long {
        return proxyFileObject.content.size
    }

    override fun refresh() {
        // proxyFileObject.refresh()
        if (isUnLuacParsedObject()) {
            proxyFileObject.refresh()
        } else requireExtra().fileObject.refresh()

        doGetFileType()

    }

    private fun doGetFileType() {
        val func = func@{
            if (proxyFileObject.isFolder) {
                return@func FileObjectType.DIR
            }

            val currentFunc = data?.currentFunction ?: return@func FileObjectType.FILE

            if (currentFunc.childFunctions.isNotEmpty()) {
                return@func FileObjectType.FUNCTION_WITH_CHILD
            }

            FileObjectType.FUNCTION
        }
        currentFileType = func()
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

    override fun doListChildrenResolved(): Array<FileObject> {
        val fileType = getFileType()
        return when {
            proxyFileObject.isFolder -> {
                proxyFileObject.children.map {
                    val newUri =
                        it.name.friendlyURI.replace(proxyFileObject.name.friendlyURI, "unluac:")
                    fileSystem.resolveFile(newUri)
                }.toTypedArray()
            }

            fileType == FileObjectType.FILE || fileType == FileObjectType.FUNCTION_WITH_CHILD -> {
                val childFunctions = requireExtra().chunk.childFunctions
                childFunctions.map {
                    val newUri = proxyFileObject.name.friendlyURI + "/" + it.name
                    fileSystem.resolveFile(newUri)
                }.toTypedArray()
            }


            else -> emptyFileObjectArray
        }
    }

    override fun doGetType(): FileType {
        return when (getFileType()) {
            FileObjectType.DIR -> FileType.FOLDER
            FileObjectType.FUNCTION, FileObjectType.FILE -> FileType.FILE_OR_FOLDER
            FileObjectType.FUNCTION_WITH_CHILD -> FileType.FILE
        }
    }


    override fun doGetOutputStream(bAppend: Boolean): OutputStream {
        return if (isUnLuacParsedObject()) {
            requireExtra().let {
                if (it.currentFunction == null) it.fileObject.writeAllData()
                else it.fileObject.writeData(it.requireFunction())
            }
        } else {
            proxyFileObject.content.outputStream
        }
    }

    override fun doListChildren(): Array<String> {
        val fileType = getFileType()
        return when {
            proxyFileObject.isFolder -> {
                proxyFileObject.children.map {
                    it.name.friendlyURI.replace(proxyFileObject.name.friendlyURI, "unluac:")
                }.toTypedArray()
            }

            fileType == FileObjectType.FILE || fileType == FileObjectType.FUNCTION_WITH_CHILD -> {
                val childFunctions = requireExtra().chunk.childFunctions
                childFunctions.map {
                    proxyFileObject.name.friendlyURI + "/" + it.name
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
        if (currentFileType == null) {
            doGetFileType()
        }
        return currentFileType as FileObjectType
    }


}

enum class FileObjectType {
    FUNCTION, FILE, DIR, FUNCTION_WITH_CHILD
}
