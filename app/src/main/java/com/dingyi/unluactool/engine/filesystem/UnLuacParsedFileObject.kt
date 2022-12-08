package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.engine.lasm.assemble.Assembler
import com.dingyi.unluactool.engine.lasm.data.v1.AbsFunction
import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import com.dingyi.unluactool.engine.lasm.disassemble.LasmDisassembler
import com.dingyi.unluactool.engine.lasm.dump.LasmDumper
import com.dingyi.unluactool.engine.lasm.dump.LasmUnDumper
import com.dingyi.unluactool.engine.util.StreamOutputProvider
import org.apache.commons.vfs2.FileObject
import unluac.decompile.Output
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.StringBufferInputStream
import java.nio.ByteBuffer

class UnLuacParsedFileObject(
    private val proxyFileObject: FileObject
) {

    lateinit var lasmChunk: LASMChunk
        private set

    private lateinit var assembler: Assembler
    fun init() {
        lasmChunk =
            LasmUnDumper().unDump(
                proxyFileObject.content.inputStream
            )

        assembler = Assembler(lasmChunk)
    }


    fun refresh() {
        val outputProvider = StreamOutputProvider(
            proxyFileObject.content.outputStream
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

    fun writeData(func:LASMFunction):OutputStream {
        return ParsedObjectOutputStream(func)
    }

    fun wrapDataToStream(string: String): InputStream {
        return ByteArrayInputStream(string.encodeToByteArray())
    }


    inner class ParsedObjectOutputStream(
        private val currentFunction: LASMFunction? = null
    ) : OutputStream() {


        private val buffer = StringBuilder()

        override fun write(b: Int) {
            buffer.append(b)
        }

        override fun close() {
            super.close()

            if (currentFunction!=null) {
                currentFunction.data = buffer.toString()
                return
            }

            val assembler = unluac.assemble.Assembler(
                ByteArrayInputStream(buffer.toString().encodeToByteArray()),

            )

            val chunk = assembler.chunk

            val mainFunction = chunk.convertToFunction(chunk.main)

            lasmChunk = LasmDisassembler(mainFunction).decompile()

        }

    }

}


data class UnLuacFileObjectExtra(
    var chunk: LASMChunk,
    var currentFunction: LASMFunction?,
    var path: String,
    var fileObject: UnLuacParsedFileObject
) {
    fun requireFunction():LASMFunction = checkNotNull(currentFunction)
}

