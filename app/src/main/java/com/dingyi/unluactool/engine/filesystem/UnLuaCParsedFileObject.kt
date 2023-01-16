package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.common.ktx.inputStream
import com.dingyi.unluactool.common.ktx.outputStream
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.engine.lasm.data.v1.AbsFunction
import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import com.dingyi.unluactool.engine.lasm.disassemble.LasmDisassembler2
import com.dingyi.unluactool.engine.lasm.dump.v1.LasmDumper
import com.dingyi.unluactool.engine.lasm.dump.v1.LasmUnDumper
import com.dingyi.unluactool.engine.util.StreamOutputProvider
import org.apache.commons.vfs2.FileObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class UnLuacParsedFileObject(
    private val proxyFileObject: FileObject
) {

    lateinit var lasmChunk: LASMChunk
        private set

    private val chunkChangeListeners = mutableListOf<(LASMChunk) -> Unit>()

    fun init() {

        if (this::lasmChunk.isInitialized) {
            return
        }

        lasmChunk =
            LasmUnDumper().unDump(
                proxyFileObject.inputStream
            )

    }


    fun addChunkChangeListener(listener: (LASMChunk) -> Unit) {
        chunkChangeListeners.add(listener)
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
            unluac.decompile.Output(outputProvider), lasmChunk
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

            val assembler =
                unluac.assemble.Assembler(ByteArrayInputStream(outputStream.toByteArray()))

            val chunk = assembler.chunk

            val mainFunction = chunk.convertToFunction(chunk.main)

            lasmChunk = LasmDisassembler2(mainFunction).decompile()

            chunkChangeListeners.forEach {
                it(lasmChunk)
            }

            refreshFlush()

        }

    }

}


data class UnLuacFileObjectExtra(
    var chunk: LASMChunk,
    var currentFunction: LASMFunction?,
    var path: String,
    var fileObject: UnLuacParsedFileObject,
    var project: Project
) {
    fun requireFunction(): LASMFunction = checkNotNull(currentFunction)

}

