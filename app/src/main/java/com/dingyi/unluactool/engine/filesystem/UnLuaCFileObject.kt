package com.dingyi.unluactool.engine.filesystem

import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileObject
import java.io.InputStream
import java.io.OutputStream

class UnLuaCFileObject(
    internal val proxyFileObject: FileObject,
    private var data: UnLuacFileObjectExtra? = null,
    private val name: AbstractFileName,
    private val fileSystem: UnLuacFileSystem
) : AbstractFileObject<UnLuacFileSystem>(name, fileSystem) {

    companion object {
        private val emptyArray = arrayOf<String>()
        private val emptyFileObjectArray = arrayOf<FileObject>()
    }


    private var currentFileType: FileObjectType? = null

    private fun isNotUnLuacParsedObject(): Boolean = data == null

    private fun requireExtra(): UnLuacFileObjectExtra = checkNotNull(data)

    override fun doGetContentSize(): Long {
        return proxyFileObject.content.size
    }

    override fun refresh() {
        // proxyFileObject.refresh()

        if (isNotUnLuacParsedObject()) {
            proxyFileObject.refresh()
        } else requireExtra().fileObject.refresh()


        doGetFileType()

        if (getFileType() != FileObjectType.DIR) {
            fileSystem.refresh(this)
        }
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
        return if (isNotUnLuacParsedObject()) {
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
                val result = proxyFileObject.children.map {
                    val newUri =
                        it.name.friendlyURI.replace(
                            proxyFileObject.name.friendlyURI,
                            name.friendlyURI
                        )
                    fileSystem.resolveFile(newUri)
                }.toTypedArray()
                result
            }

            fileType == FileObjectType.FILE || fileType == FileObjectType.FUNCTION_WITH_CHILD -> {
                val extra = requireExtra()
                val childFunctions = (extra.currentFunction ?: extra.chunk).childFunctions
                val result = childFunctions.map {
                    val newUri = name.friendlyURI + "/" + it.name
                    fileSystem.resolveFile(newUri)
                }.toTypedArray()
                result
            }


            else -> emptyFileObjectArray
        }
    }

    override fun doGetType(): FileType {
        return when (getFileType()) {
            FileObjectType.DIR, FileObjectType.FUNCTION_WITH_CHILD, FileObjectType.FILE -> FileType.FOLDER
            FileObjectType.FUNCTION -> FileType.FILE
        }
    }


    override fun doGetOutputStream(bAppend: Boolean): OutputStream {
        return if (isNotUnLuacParsedObject()) {
            requireExtra().let {
                if (it.currentFunction == null) it.fileObject.writeAllData()
                else it.fileObject.writeData(it.requireFunction())
            }
        } else {
            proxyFileObject.content.outputStream
        }
    }

    override fun doListChildren(): Array<String> {
        /* val fileType = getFileType()
         val friendlyURI = name.friendlyURI
         val proxyFriendlyURI = proxyFileObject.name.friendlyURI
         return when {
             proxyFileObject.isFolder -> {
                 proxyFileObject.children.map {
                     it.name.friendlyURI.replace(
                         proxyFriendlyURI,
                         friendlyURI
                     )
                 }.toTypedArray()

             }

             fileType == FileObjectType.FILE || fileType == FileObjectType.FUNCTION_WITH_CHILD -> {
                 val childFunctions = requireExtra().chunk.childFunctions
                 childFunctions.map {
                     friendlyURI + "/" + it.name
                 }.toTypedArray()
             }

             else -> emptyArray
         }*/
        return emptyArray
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


    override fun close() {
        super.close()
        data?.fileObject?.refreshFlush()
    }


    fun getFunctionFullName(): String? {
        return when (getFileType()) {
            FileObjectType.FILE -> name.baseName
            FileObjectType.FUNCTION, FileObjectType.FUNCTION_WITH_CHILD -> {
                val extra = requireExtra()
                extra.currentFunction?.fullName
            }

            else -> null
        }
    }

    fun getFunctionName(): String? {
        return when (getFileType()) {
            FileObjectType.FILE -> name.baseName
            FileObjectType.FUNCTION, FileObjectType.FUNCTION_WITH_CHILD -> {
                val extra = requireExtra()
                extra.currentFunction?.name
            }

            else -> null
        }
    }

    fun getFullFunctionNameWithPath(): String? {
        val fullName = getFunctionFullName()
        if (fullName == null) {
            return fullName
        }
        val path =
            name.pathDecoded.substring(requireExtra().project.name.length + 2)
                .replace(".lasm", ".lua(/main)")
        return path
    }

}

enum class FileObjectType {
    FUNCTION, FILE, DIR, FUNCTION_WITH_CHILD
}
