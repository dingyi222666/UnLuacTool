package com.dingyi.unluactool.engine.lasm.dump

import com.dingyi.unluactool.common.ktx.toByteArray
import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import unluac.decompile.Output
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class LasmDumper(
    private val output: Output,
    private val chunk: LASMChunk
) {


    companion object {
        val lasmHeader = "unluac".toByteArray() + byteArrayOf(53) + "luaasm".toByteArray()
    }

    fun dump() {
        //header
        dumpByteArray(lasmHeader)

        //name
        dumpString(chunk.name)

        //full name
        dumpString(chunk.fullName)

        //version
        dumpString(chunk.versionData)

        //data
        dumpString(chunk.data)

        //chunk func size
        dumpInt(chunk.childFunctions.size)

        chunk.childFunctions.sortedBy { it.name }.forEach(this::dumpFunction)

    }

    private fun dumpFunction(chunk: LASMFunction) {
        //name
        dumpString(chunk.name)

        //full name
        dumpString(chunk.fullName)

        //data
        dumpString(chunk.data)

        //chunk func size
        dumpInt(chunk.childFunctions.size)


        chunk.childFunctions.sortedBy { it.name }.forEach(this::dumpFunction)
    }


    private fun dumpString(data: String) {


        val byteArrayOutputStream = ByteArrayOutputStream()

        val gzipOutputStream = GZIPOutputStream(byteArrayOutputStream)

        gzipOutputStream.write(data.toByteArray())

        //gzipOutputStream.finish()

        gzipOutputStream.close()

        val dumpBytes = byteArrayOutputStream.toByteArray()

        dumpInt(dumpBytes.size)

        dumpByteArray(dumpBytes)


    }

    private fun dumpInt(length: Int) {
        dumpByteArray(length.toByteArray())
    }


    private fun dumpByteArray(bytearray: ByteArray) {

        bytearray.forEach(output::print)
    }

}