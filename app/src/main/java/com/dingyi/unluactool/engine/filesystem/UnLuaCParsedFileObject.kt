package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.common.ktx.inputStream
import com.dingyi.unluactool.common.ktx.outputStream
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.engine.lasm.assemble.LasmAssembleService
import com.dingyi.unluactool.engine.lasm.data.v1.AbsFunction
import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import com.dingyi.unluactool.engine.lasm.disassemble.LasmDisassembleService
import com.dingyi.unluactool.engine.lasm.dump.v1.LasmDumper
import com.dingyi.unluactool.engine.lasm.dump.v1.LasmUnDumper
import com.dingyi.unluactool.engine.util.StreamOutputProvider
import org.apache.commons.vfs2.FileObject
import unluac.decompile.Output
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class UnLuacParsedFileObject(
    private val proxyFileObject: FileObject
) {

    lateinit var lasmChunk: LASMChunk
        private set

    private val chunkChangeListeners = mutableListOf<ChunkChangeListener>()

    private val lasmDisassembleService by lazy(LazyThreadSafetyMode.NONE) {
        MainApplication.instance.globalServiceRegistry.get<LasmDisassembleService>()
    }

    private val lasmAssembleService by lazy(LazyThreadSafetyMode.NONE) {
        MainApplication.instance.globalServiceRegistry.get<LasmAssembleService>()
    }

    fun init() {

        if (this::lasmChunk.isInitialized) {
            return
        }

        lasmChunk =
            LasmUnDumper().unDump(
                proxyFileObject.inputStream
            )

    }


    fun addChunkChangeListener(listener: ChunkChangeListener) {
        if (!chunkChangeListeners.contains(listener)) {
            chunkChangeListeners.add(listener)
        }
    }

    fun removeChunkChangeListener(listener: ChunkChangeListener) {
        chunkChangeListeners.remove(listener)
    }

    fun clearChunkChangeListener(listener: ChunkChangeListener) {
        chunkChangeListeners.clear()
    }

    fun refresh() {
        lasmChunk =
            LasmUnDumper().unDump(
                proxyFileObject.inputStream
            )

    }

    fun refreshFlush() {
        val outputProvider = StreamOutputProvider(
            proxyFileObject.outputStream
        )
        LasmDumper(
            Output(outputProvider), lasmChunk
        ).dump()
        outputProvider.close()
    }


    fun getAllData(): String {
        return lasmChunk.getAllData()
    }

    fun getAllDataAsStream(): InputStream {
        return wrapDataToStream(getAllData())
    }

    fun resolveFunctionDataByName(path: String): LASMFunction? {
        return lasmChunk.resolveFunction(path)
    }

    fun resolveChildFunctions(path: String): List<LASMFunction>? {
        val func = resolveFunctionDataByName(path)
        return (func as AbsFunction<LASMFunction>?)?.childFunctions
    }


    fun writeAllData(): OutputStream {
        return ParsedObjectOutputStream()
    }

    fun writeData(func: LASMFunction): OutputStream {
        return ParsedObjectOutputStream(func)
    }

    fun wrapDataToStream(string: String): InputStream {
        return ByteArrayInputStream(string.encodeToByteArray())
    }


    inner class ParsedObjectOutputStream(
        private val currentFunction: LASMFunction? = null
    ) : OutputStream() {

        private val outputStream = ByteArrayOutputStream()

        override fun write(b: Int) {
            outputStream.write(b)
        }

        override fun close() {
            super.close()

            val data = outputStream.toByteArray().decodeToString()

            if (currentFunction != null) {
                currentFunction.data = data
            }

            val byteCode = checkNotNull(lasmAssembleService.assembleToObject(lasmChunk))

            val oldChunk = lasmChunk

            lasmChunk = checkNotNull(lasmDisassembleService.disassemble(byteCode))

            chunkChangeListeners.forEach {
                it.onChunkChange(lasmChunk, oldChunk)
            }

            System.gc()

            refreshFlush()

        }

    }

}


fun interface ChunkChangeListener {
    fun onChunkChange(newChunk: LASMChunk, oldChunk: LASMChunk)
}

data class UnLuacFileObjectExtra(
    var chunk: LASMChunk,
    var currentFunction: LASMFunction?,
    var path: String,
    var fileObject: UnLuacParsedFileObject,
    var project: Project,
    val isDecompile:Boolean = false
) {
    fun requireFunction(): LASMFunction = checkNotNull(currentFunction)

}

