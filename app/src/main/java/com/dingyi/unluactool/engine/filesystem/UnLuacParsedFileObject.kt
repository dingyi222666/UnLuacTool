package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.dump.LasmDumper
import com.dingyi.unluactool.engine.lasm.dump.LasmUnDumper
import com.dingyi.unluactool.engine.util.StreamOutputProvider
import org.apache.commons.vfs2.FileObject
import unluac.decompile.Output
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.StringBufferInputStream

internal class UnLuacParsedFileObject(
    private val proxyFileObject: FileObject
) {

    private lateinit var lasmChunk: LASMChunk

     fun init() {
        lasmChunk =
            LasmUnDumper().unDump(
                proxyFileObject.content.inputStream
            )
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

    fun resolveFunctionDataByName(name:String) {

    }

    fun writeAllData():OutputStream {

    }

    fun wrapDataToStream(string: String):InputStream {
        return ByteArrayInputStream(string.encodeToByteArray())
    }



}


