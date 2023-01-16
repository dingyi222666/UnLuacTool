package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.common.ktx.inputStream
import com.dingyi.unluactool.common.ktx.outputStream
import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import org.apache.commons.vfs2.FileChangeEvent
import org.apache.commons.vfs2.FileListener
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
    private val fileSystem: UnLuaCFileSystem
) : AbstractFileObject<UnLuaCFileSystem>(name, fileSystem), ChunkChangeListener {

    companion object {
        private val emptyArray = arrayOf<String>()
        private val emptyFileObjectArray = arrayOf<FileObject>()
    }

    private var currentFileType: FileObjectType? = null

    private var isDelete = false

    private fun isNotUnLuacParsedObject(): Boolean = data == null

    private fun requireExtra(): UnLuacFileObjectExtra = checkNotNull(data)


    override fun doAttach() {
        data?.fileObject?.addChunkChangeListener(this)
    }

    override fun doDetach() {
        data?.fileObject?.removeChunkChangeListener(this)
    }

    override fun onChunkChange(newChunk: LASMChunk, oldChunk: LASMChunk) {

        val data = requireExtra()

        val currentFunctionPath = data.currentFunction?.fullName

        fileSystem.fireFileChanged(this)

        if (currentFunctionPath != null) {
            val newFunction = newChunk.resolveFunction(currentFunctionPath)
            if (newFunction == null) {
                isDelete = true
                data.currentFunction = null
                fileSystem.fireFileDeleted(this)
                return
            }
            data.currentFunction = newFunction
        } else {
            data.chunk = newChunk
        }

    }

    override fun doGetContentSize(): Long {
        return proxyFileObject.content.size
    }

    override fun refresh() {
        proxyFileObject.refresh()

        data?.fileObject?.refresh()

        doGetFileType()

        if (exists() && getFileType() != FileObjectType.DIR) {
            fileSystem.refresh(this)
        }
    }

    private fun doGetFileType() {
        val func = func@{

            if (!proxyFileObject.exists() || isDelete) {
                return@func FileObjectType.IMAGINARY
            }

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
            proxyFileObject.inputStream
        } else {
            requireExtra().let {
                it.currentFunction?.data ?: it.fileObject.getAllData()
            }.let {
                requireExtra().fileObject.wrapDataToStream(it)
            }
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
            FileObjectType.IMAGINARY -> FileType.IMAGINARY
        }
    }

    override fun doListChildren(): Array<String> {
        return emptyArray
    }

    override fun doGetOutputStream(bAppend: Boolean): OutputStream {
        return if (isNotUnLuacParsedObject()) {
            proxyFileObject.outputStream
        } else {
            requireExtra().let {
                if (it.currentFunction == null) it.fileObject.writeAllData()
                else it.fileObject.writeData(it.requireFunction())
            }
        }
    }


    override fun doDelete() {
        if (isNotUnLuacParsedObject()) {
            proxyFileObject.delete()
        }
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
    FUNCTION, FILE, DIR, FUNCTION_WITH_CHILD, IMAGINARY
}
