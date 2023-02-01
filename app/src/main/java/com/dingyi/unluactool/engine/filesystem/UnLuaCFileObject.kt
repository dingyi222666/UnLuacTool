package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.common.ktx.inputStream
import com.dingyi.unluactool.common.ktx.outputStream
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.engine.lasm.assemble.LasmAssembleService
import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lua.decompile.DecompileService
import org.apache.commons.vfs2.FileChangeEvent
import org.apache.commons.vfs2.FileListener
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileObject
import unluac.Configuration
import unluac.parse.LFunction
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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

    private val decompileService by lazy(LazyThreadSafetyMode.NONE) {
        MainApplication.instance.globalServiceRegistry
            .get<DecompileService>()
    }

    private val lasmAssembleService by lazy(LazyThreadSafetyMode.NONE) {
        MainApplication.instance.globalServiceRegistry
            .get<LasmAssembleService>()
    }

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
            val newFunction = newChunk.resolveFunction(currentFunctionPath.substringAfter("/"))
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

            if (data?.isDecompile == true) {
                return@func FileObjectType.DECOMPILE_FUNCTION
            }

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
        val fileType = getFileType()
        return if (isNotUnLuacParsedObject()) {
            proxyFileObject.inputStream
        } else if (fileType != FileObjectType.DECOMPILE_FUNCTION) {
            val extra = requireExtra()
            extra.let {
                it.currentFunction?.data ?: it.fileObject.getAllData()
            }.let {
                extra.fileObject.wrapDataToStream(it)
            }
        } else {
            decompileFunction()
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
            FileObjectType.FUNCTION, FileObjectType.DECOMPILE_FUNCTION -> FileType.FILE
            FileObjectType.IMAGINARY -> FileType.IMAGINARY
        }
    }

    override fun doListChildren(): Array<String> {
        return emptyArray
    }

    override fun doGetOutputStream(bAppend: Boolean): OutputStream {
        val fileType = getFileType()
        return if (isNotUnLuacParsedObject()) {
            proxyFileObject.content.getOutputStream(bAppend)
        } else if (fileType != FileObjectType.DECOMPILE_FUNCTION) {
            requireExtra().let {
                if (it.currentFunction == null) it.fileObject.writeAllData()
                else it.fileObject.writeData(it.requireFunction())
            }
        } else {
            // ?
            ByteArrayOutputStream()
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

        if (name.uri != other.name.uri) return false
        if (proxyFileObject.name.uri != other.proxyFileObject.name.uri) return false

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

    override fun doIsWriteable(): Boolean {
        return getFileType() != FileObjectType.DECOMPILE_FUNCTION
    }

    private fun decompileFunction(): InputStream {
        val extra = requireExtra()
        val assembleObject = if (extra.currentFunction == null) {
            lasmAssembleService.assembleToObject(extra.chunk)
        } else {
            lasmAssembleService.assembleToObject(extra.chunk, extra.requireFunction())?.second
        }.let { it as LFunction? }
            ?: error("Unable to decompile function: ${getFunctionFullName()}")

        assembleObject.header.config = Configuration().apply {
            rawstring = true
            mode = Configuration.Mode.DECOMPILE
            variable = Configuration.VariableMode.FINDER
            //strict_scope = true
        }

        var decompiledSource = decompileService.decompileToSource(assembleObject, null).toString()

        if (decompiledSource == "null" || decompiledSource.isEmpty()) {
            assembleObject.header.config = Configuration().apply {
                rawstring = true
                mode = Configuration.Mode.DECOMPILE
                variable = Configuration.VariableMode.NODEBUG
                //strict_scope = true
            }
            decompiledSource = decompileService.decompileToSource(assembleObject, null).toString()
        }


        return extra.fileObject.wrapDataToStream(decompiledSource.toString())

    }


    fun getFunctionFullName(): String? {
        return when (getFileType()) {
            FileObjectType.FILE -> name.baseName
            FileObjectType.FUNCTION, FileObjectType.DECOMPILE_FUNCTION, FileObjectType.FUNCTION_WITH_CHILD -> {
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

            FileObjectType.DECOMPILE_FUNCTION -> {
                val extra = requireExtra()
                extra.currentFunction?.name + ".lua"
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
        if (data?.isDecompile == true) {
            return "${path.replace("_decompile", "")}(.lua)"
        }

        return path
    }

}

enum class FileObjectType {
    FUNCTION, FILE, DIR, FUNCTION_WITH_CHILD, DECOMPILE_FUNCTION, IMAGINARY
}
