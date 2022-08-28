package com.dingyi.unluactool.engine.lasm.dump

import com.dingyi.unluactool.common.ktx.toByteArray
import com.dingyi.unluactool.engine.lasm.data.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.LASMFunction
import unluac.decompile.Output
import java.nio.ByteBuffer
import java.nio.IntBuffer

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


        chunk.childFunctions.forEach(this::dumpFunction)

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


        chunk.childFunctions.forEach(this::dumpFunction)
    }


    private fun dumpString(data: String) {
        dumpInt(data.length)
        dumpByteArray(data.toByteArray())
    }

    private fun dumpInt(length: Int) {
        dumpByteArray(length.toByteArray())
    }


    private fun dumpByteArray(bytearray: ByteArray) {
        bytearray.forEach(output::print)
    }

}